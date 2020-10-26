package com.zjw.plugin

import javassist.*
import javassist.bytecode.CodeAttribute
import javassist.bytecode.LocalVariableAttribute
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.http.util.TextUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


public class MyInject {

    private static ClassPool pool = ClassPool.getDefault()
    static String CostTime = "CostTime";
    static String AppMethodTime = "AppMethodTime";
    static String AppMethodOrder = "AppMethodOrder";
    static String LogLevel = "i";
    static String androidJarPath = "";
    static boolean useCostTime;
    static boolean showLog;
    static boolean enableOrder;
    static double limitTime = -1;
    static double warringTime = 16;

    // 存储文件列表
    private static ArrayList<String> fileList = new ArrayList<>();


    public static void injectDir(String androidJarPath, String path, String jarsPath, HashMap<String, String> map,
                                 Project project, String aarOrJarPath, String buildType) {
        //path is D:\GitBlit\AppMethodTime\app\build\intermediates\classes\debug
        //gradle 4.5.1 之后 path is D:\Github\AppMethodTime\app\build\intermediates\javac\debug\compileDebugJavaWithJavac\classes
        //this.path = path  /Users/hana/StudioProjects/AppMethodTime/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes
        useCostTime =  (project.extensions.getByName("AppMethodTime") as MyCustomPluginExtension).useCostTime
        showLog =  (project.extensions.getByName("AppMethodTime") as MyCustomPluginExtension).showLog
        enableOrder =  (project.extensions.getByName("AppMethodTime") as MyCustomPluginExtension).enableOrder
        limitTime =  (project.extensions.getByName("AppMethodTime") as MyCustomPluginExtension).limitTimeMilli
        warringTime =  (project.extensions.getByName("AppMethodTime") as MyCustomPluginExtension).warringTimeMilli

        this.androidJarPath = androidJarPath
        File tragetFile = new File(aarOrJarPath)
        if (!TextUtils.isEmpty(aarOrJarPath)) {
            if (!tragetFile.exists()) {
                return
            }
            if (aarOrJarPath.endsWith(".jar")) {
                modifyJar(tragetFile)
            } else if (aarOrJarPath.endsWith(".aar")) {
                modifyAar(tragetFile)
            }
        } else if (".idea" == path) {
            if (map.size() > 0) {
                modifyJar(new File(jarsPath), true)
            }
        } else {
            if (TextUtils.isEmpty(androidJarPath)) {
                return
            }
            ArrayList<ClassPath> classPathArrayList = new ArrayList<>()
            if (path.endsWith(".class")) {
                try {
                    //增量
                    String classesPath = path.split(File.separator + "classes" + File.separator)[0] + File.separator + "classes"
                    classPathArrayList.add(pool.appendClassPath(classesPath))
                } catch (Throwable e) {
                    if (showLog) {
                        e.printStackTrace()
                    }
                }
            } else {
                classPathArrayList.add(pool.appendClassPath(path))
            }
            classPathArrayList.add(pool.appendClassPath(androidJarPath))

            try {
                //加载gradle指定依赖
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    classPathArrayList.add(pool.appendClassPath(entry.value))
                }
                //编译顺序：先编译lib库再编译主项目
                //所以需要加载依赖的lib jar
                File libJarDir = new File(jarsPath)
                if (libJarDir.exists() && libJarDir.isDirectory()) {
                    ArrayList<String> arr = getFile(libJarDir);
                    for (String a : arr) {
                        classPathArrayList.add(pool.appendClassPath(a));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
            }

            File dir = new File(path)
            if (dir.isDirectory()) {
                def root = dir.absolutePath
                dir.eachFileRecurse { File file ->
                    String filePath = file.absolutePath
                    //确保当前文件是class文件，并且不是系统自动生成的class文件以及注解文件
                    if (filter(filePath)) {
                        String classPath = filePath
                        String className = classPath.substring(root.length() + 1, classPath.length() - 6).replaceAll("/", ".")
                        CtClass c = modifyClass(className)
                        if (c != null) {
                            c.writeFile(path)
                            c.detach()
                        }
                    }
                }
            } else {
                //增量时是单个文件
                try {
                    if (filter(path)) {
                        String classesPath = path.split(File.separator + "classes" + File.separator)[0] + File.separator + "classes"
                        String classPath = path
                        ///Users/hana/StudioProjects/AppMethodTime/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/com/zjw/appmethodtime/MyApplication.class
                        String index = File.separator + "classes" + File.separator
                        int startPosition = path.indexOf(index)
                        String className = classPath
                                .substring(startPosition + index.length())
                                .replaceAll("/", ".").replace(".class", "")

                        CtClass c = modifyClass(className)
                        if (c != null) {
                            c.writeFile(classesPath)
                            c.detach()
                        }
                    }
                } catch (Throwable e) {
                    if (showLog) {
                        e.printStackTrace()
                    }
                }
            }
            classPathArrayList.forEach { ClassPath classPath ->
                pool.removeClassPath(classPath)
            }
        }
    }

    private static CtClass modifyClass(String className) {
        // String path
        //开始修改class文件
        CtClass c = pool.getCtClass(className)
        if (c.isFrozen()) {
            c.defrost()
        }
        // pool.importPackage(myPackageName)
        //c.getMethod("setDname", "(Ljava/lang/String;)V") 指定函数名和参数获取函数对象
        //遍历类的所有方法
        CtMethod[] methods = c.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (method.isEmpty() || Modifier.isNative(method.getModifiers())) {
                //空函数体有可能是抽象函数以及接口函数或者native方法
                return null
            }
            if (useCostTime
                    && method.getAvailableAnnotations() != null
                    && method.getAvailableAnnotations().length >= 1
                    && "${method.getAvailableAnnotations()[0]}".contains(CostTime)) {
                insertCostTimeCode(method, c, showLog)
            } else if (!useCostTime) {
                insertCostTimeCode(method, c, showLog)
            }
        }//END   for (CtMethod method : methods)
        // c.writeFile(path)
        // c.detach()
        return c
    }

    private static boolean filter(String filePath) {
        filePath.endsWith(".class") && !filePath.contains('R$') && !filePath.contains('R.class') && !filePath.contains("BuildConfig.class") && !filePath.contains("CostTime") && !filePath.contains("Manifest")
    }

    private static void insertCostTimeCode(CtMethod method, CtClass c, boolean showLog) {
        if (showLog) {
            println("\n==================  InsertCostTimeCode Start =======================")
            println(method.longName + "{")
        }

        ArrayList<String> paramNameList = new ArrayList<>();
        try {
            //获取方法参数名称
            CodeAttribute codeAttribute = method.methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
                    .getAttribute(LocalVariableAttribute.tag);
            String[] paramNames = new String[method.getParameterTypes().length];
            int pos = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
            for (int i = 0; i < paramNames.length; i++) {
                paramNames[i] = attr.variableName(i + pos);
                paramNameList.add(attr.variableName(i + pos));
            }
        } catch (Exception e) {
            if (showLog) {
                e.printStackTrace()
            }
        }

        def StringType = pool.getCtClass("java.lang.String");
        method.addLocalVariable("_startTime", CtClass.longType);
        method.addLocalVariable("_endTime", CtClass.longType);
        method.addLocalVariable("_fullClassName", StringType);
        method.addLocalVariable("_className", StringType);
        method.addLocalVariable("_methodName", StringType);
        method.addLocalVariable("_lineNumber", CtClass.intType);
        method.addLocalVariable("_info", StringType);
        method.addLocalVariable("_limit", StringType);
        method.addLocalVariable("_cost", CtClass.doubleType);
        if (showLog) {
            println("   long   _startTime;")
            println("   long   _endTime;")
            println("   String _fullClassName;")
            println("   String _className;")
            println("   String _methodName;")
            println("   String _lineNumber;")
            println("   String _info;")
            println("   String _limit;")
            println("   double _cost;")
        }

        def lineNumber = method.methodInfo.getLineNumber(0);
        //插入到函数第一句
        StringBuilder startInjectStr = new StringBuilder();
        startInjectStr.append("     _startTime = System.nanoTime();\n");
        startInjectStr.append("     _fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();\n");
        startInjectStr.append("     _className = _fullClassName.substring(_fullClassName.lastIndexOf(\".\") + 1)+\".java\";\n");
        startInjectStr.append("     _methodName = Thread.currentThread().getStackTrace()[2].getMethodName();\n");
        startInjectStr.append("     _lineNumber = " + lineNumber + ";\n");
        startInjectStr.append("     _info = _fullClassName+\": \"+_methodName + \" (\" + _className + \":\"+ _lineNumber + \")\";\n");
        if (enableOrder){
            startInjectStr.append("     android.util.Log.${LogLevel}(\"${AppMethodOrder}\",");
            startInjectStr.append("     _info +\": ");
            for (int i = 0; i < paramNameList.size(); i++) {
                startInjectStr.append(" <${paramNameList.get(i)}: \"+\$" + (i + 1) + "+\"> ");
            }
            startInjectStr.append(" \"); ")
        }
        //startInjectStr.append("\n     Thread.dumpStack();");
        try {
            method.insertBefore(startInjectStr.toString())
        } catch (Exception e) {
            if (showLog) {
                e.printStackTrace()
            }
        }
        //  println("方法第一句插入了：" + startInjectStr.toString() + "语句")
        if (showLog) {
            println(startInjectStr.toString())
            println("   <<<==== original code ====>>>   ")
        }

        //插入到函数最后一句
        StringBuilder endInjectStr = new StringBuilder();
        endInjectStr.append("   _endTime = System.nanoTime();\n");
        endInjectStr.append("   _cost = (_endTime - _startTime)*1.0f/1000000;\n");
        endInjectStr.append("   _limit = _cost >= ${warringTime} ? \" 警告>=${warringTime}毫秒 \" : \"\" ;\n");

        if (limitTime > 0) {
            endInjectStr.append("   if(${limitTime} >= _cost){");

            endInjectStr.append("   android.util.Log.${LogLevel}(\"${AppMethodTime}\",");
            endInjectStr.append("_info + \": \" + _limit + \"\"");
            endInjectStr.append("+_cost+\" (毫秒) return is \"+\$_ +\" ");
            for (int i = 0; i < paramNameList.size(); i++) {
                endInjectStr.append(" <${paramNameList.get(i)}: \"+\$" + (i + 1) + "+\"> ");
            }
            endInjectStr.append(" \"); ");

            endInjectStr.append("}");
        }else {
            endInjectStr.append("   android.util.Log.${LogLevel}(\"${AppMethodTime}\",");
            endInjectStr.append("_info + \": \" + _limit + \"\"");
            endInjectStr.append("+_cost+\" (毫秒) return is \"+\$_ +\" ");
            for (int i = 0; i < paramNameList.size(); i++) {
                endInjectStr.append(" <${paramNameList.get(i)}: \"+\$" + (i + 1) + "+\"> ");
            }
            endInjectStr.append(" \"); ");
        }

        // endInjectStr.append("\n     Thread.dumpStack();");
        try {
            method.insertAfter(endInjectStr.toString())
        } catch (Exception e) {
            if (showLog) {
                e.printStackTrace()
            }
        }
        if (showLog) {
            println(endInjectStr.toString())
            println("}");
            println("==================  InsertCostTimeCode End =======================\n")
        }
    }


    private static ArrayList<String> getFile(File path) throws IOException {
        File[] listFile = path.listFiles();
        for (File a : listFile) {
            if (a.isDirectory()) {
                // 递归调用getFile()方法
                getFile(new File(a.getAbsolutePath()));
            } else if (a.isFile() && a.absolutePath.endsWith(".jar")) {
                this.fileList.add(a.getAbsolutePath());
            }
        }
        return fileList;
    }


    public static File unzipEntryToTemp(ZipEntry element, ZipFile zipFile, String parentDir) {
        def stream = zipFile.getInputStream(element);
        def array = IOUtils.toByteArray(stream);
        //String hex = DigestUtils.md5Hex(element.getName());
        File targetFile = new File(parentDir, "temp.jar");
        if (targetFile.exists()) {
            targetFile.delete()
        }
        def out = new FileOutputStream(targetFile)
        out.write(array)
        out.close()
        stream.close()
        return targetFile
    }

    public static File modifyJar(File jarFile, boolean isOverride) {
        println("modifyJar:" + jarFile.path)
        ClassPath jarClassPath = pool.appendClassPath(jarFile.path)
        ClassPath androidClassPath = pool.insertClassPath(androidJarPath)
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile);
        /** 设置输出到的jar */
        def hexName = "";
        hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8);

        def outputJar = new File(jarFile.parent, "Target_" + jarFile.name)
        if (outputJar.exists()) {
            outputJar.delete()
        }
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
        Enumeration enumeration = file.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
            InputStream inputStream = file.getInputStream(jarEntry);

            String entryName = jarEntry.getName();
            String className

            ZipEntry zipEntry = new ZipEntry(entryName);

            jarOutputStream.putNextEntry(zipEntry);

            byte[] modifiedClassBytes = null;
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
            if (filter(entryName)) {
                className = entryName.replace("/", ".").replace(".class", "")
                CtClass c = modifyClass(className)
                if (c != null) {
                    modifiedClassBytes = c.toBytecode()
                    c.detach()
                }
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes);
            } else {
                jarOutputStream.write(modifiedClassBytes);
            }
            jarOutputStream.closeEntry();
        }
//            Log.info("${hexName} is modified");
        jarOutputStream.close();
        file.close();

        if (isOverride) {
            String sourcesFile = jarFile.path
            jarFile.delete()
            outputJar.renameTo(new File(sourcesFile))
        }

        pool.removeClassPath(jarClassPath)
        pool.removeClassPath(androidClassPath)
        return outputJar;
    }

    public static void modifyAar(File targetFile) {

        ZipFile zipFile = new ZipFile(targetFile);
        Enumeration<ZipEntry> entries = zipFile.entries();

        def outputAar = new File(targetFile.parent, "Target_" + targetFile.name)
        if (outputAar.exists()) {
            outputAar.delete()
        }

        ZipOutputStream outputAarStream = new ZipOutputStream(new FileOutputStream(outputAar))
        FileInputStream fileInputStream = null;
        File innerJar = null;
        File outJar = null;
        while (entries.hasMoreElements()) {
            ZipEntry element = entries.nextElement();
            def name = element.getName();
            ZipEntry zipEntry = new ZipEntry(name);

            outputAarStream.putNextEntry(zipEntry);
            if (name.endsWith(".jar")) {
                innerJar = unzipEntryToTemp(element, zipFile, targetFile.parent);
                outJar = modifyJar(innerJar);
                fileInputStream = new FileInputStream(outJar)
                outputAarStream.write(IOUtils.toByteArray(fileInputStream))
            } else {
                def stream = zipFile.getInputStream(element)
                byte[] array = IOUtils.toByteArray(stream)
                if (array != null) {
                    outputAarStream.write(array)
                }
                stream.close()
            }
            outputAarStream.closeEntry();
        }
        zipFile.close()
        if (fileInputStream != null) {
            fileInputStream.close()
        }
        outputAarStream.close()
        if (innerJar != null) {
            innerJar.delete()
        }
        if (outJar != null) {
            outJar.delete()
        }
    }

}