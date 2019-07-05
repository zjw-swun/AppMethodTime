package com.zjw.plugin

import javassist.*
import javassist.bytecode.CodeAttribute
import javassist.bytecode.LocalVariableAttribute
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.http.util.TextUtils
import org.gradle.util.TextUtil

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
    static String LogLevel = "e";
    static String androidJarPath = "";

    // 存储文件列表
    private static ArrayList<String> fileList = new ArrayList<>();


    public static void injectDir(String androidJarPath, String path, String jarsPath, boolean useCostTime, boolean showLog, String aarOrJarPath, String buildType) {
        //path is D:\GitBlit\AppMethodTime\app\build\intermediates\classes\debug
        //gradle 4.5.1 之后 path is D:\Github\AppMethodTime\app\build\intermediates\javac\debug\compileDebugJavaWithJavac\classes

        //this.path = path
        //this.useCostTime = useCostTime
        //this.showLog = showLog
        //this.aarOrJarPath = aarOrJarPath
        this.androidJarPath = androidJarPath
        if (!TextUtils.isEmpty(aarOrJarPath)) {
            if (aarOrJarPath.endsWith(".jar")) {
                modifyJar(new File(aarOrJarPath))
            } else if (aarOrJarPath.endsWith(".aar")) {
                modifyAar(new File(aarOrJarPath))
            }
        } else {
            pool.appendClassPath(path)
            pool.insertClassPath(androidJarPath)
            //编译顺序：先编译lib库再编译主项目
            //所以需要加载依赖的lib jar
            File libJarDir = new File(jarsPath)
            try {
                if (libJarDir.exists() && libJarDir.isDirectory()) {
                    ArrayList<String> arr = getFile(libJarDir);
                    for (String a : arr) {
                        pool.appendClassPath(a);
                    }
                }
            } catch (Exception e) {

            }

            File dir = new File(path)
            if (dir.isDirectory()) {
                dir.eachFileRecurse { File file ->
                    String filePath = file.absolutePath
                    //确保当前文件是class文件，并且不是系统自动生成的class文件以及注解文件
                    if (filter(filePath)) {
                        println("filePath is " + filePath);
                        String classPath = ""
                        if (filePath.contains(File.separator + "release" + File.separator)) {
                            classPath = filePath.split("\\\\release\\\\")[1]
                        } else {
                            classPath = filePath.split("\\\\debug\\\\")[1]
                        }
                        if (filePath.contains(File.separator + "javac" + File.separator)) {
                            classPath = filePath.split("\\\\classes\\\\")[1]
                        }

                        String className = classPath.substring(0, classPath.length() - 6).replace('\\', '.').replace('/', '.')
                        //println("className is " + className);

                        CtClass c = modifyClass(className)
                        if(c!=null) {
                            c.writeFile(path)
                            c.detach()
                        }
                    }
                }
            }
        }
    }

    private static CtClass modifyClass(String className) {
        boolean useCostTime
        boolean showLog
        String path
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
            //println("method ====" + method.longName)
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
        method.addLocalVariable("startTime", CtClass.longType);
        method.addLocalVariable("endTime", CtClass.longType);
        method.addLocalVariable("fullClassName", StringType);
        method.addLocalVariable("className", StringType);
        method.addLocalVariable("methodName", StringType);
        method.addLocalVariable("lineNumber", CtClass.intType);
        method.addLocalVariable("info", StringType);
        if (showLog) {
            println("   long startTime;")
            println("   long endTime;")
            println("   String fullClassName;")
            println("   String className;")
            println("   String methodName;")
            println("   String lineNumber;")
            println("   String info;")
        }

        def lineNumber = method.methodInfo.getLineNumber(0);
        //插入到函数第一句
        StringBuilder startInjectStr = new StringBuilder();
        startInjectStr.append("     startTime = System.nanoTime();\n");
        startInjectStr.append("     fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();\n");
        startInjectStr.append("     className = fullClassName.substring(fullClassName.lastIndexOf(\".\") + 1)+\".java\";\n");
        startInjectStr.append("     methodName = Thread.currentThread().getStackTrace()[2].getMethodName();\n");
        startInjectStr.append("     lineNumber = " + lineNumber + ";\n");
        startInjectStr.append("     info =\"===\"+startTime+\"===  \"+ fullClassName+\": \"+methodName + \" (\" + className + \":\"+ lineNumber + \")\";\n");
        startInjectStr.append("     android.util.Log.${LogLevel}(\"${AppMethodOrder}\",");
        startInjectStr.append("     info +\": ");
        for (int i = 0; i < paramNameList.size(); i++) {
            startInjectStr.append(" <${paramNameList.get(i)}: \"+\$" + (i + 1) + "+\"> ");
        }
        startInjectStr.append(" \"); ")
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
        endInjectStr.append("   endTime = System.nanoTime();\n");
        endInjectStr.append("   android.util.Log.${LogLevel}(\"${AppMethodTime}\",");
        endInjectStr.append("info + \": \" ");
        endInjectStr.append("+(endTime - startTime)*1.0f/1000000+\" (毫秒) return is \"+\$_ +\" ");
        for (int i = 0; i < paramNameList.size(); i++) {
            endInjectStr.append(" <${paramNameList.get(i)}: \"+\$" + (i + 1) + "+\"> ");
        }
        endInjectStr.append(" \"); ");
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
        String hex = DigestUtils.md5Hex(element.getName());
        File targetFile = new File(parentDir, hex + ".jar");
        if (targetFile.exists()) {
            targetFile.delete()
        }
        new FileOutputStream(targetFile).write(array)
        return targetFile
    }

    public
    static File modifyJar(File jarFile) {
        pool.appendClassPath(jarFile.path)
        pool.insertClassPath(androidJarPath)
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
                //println("entryName "+entryName)
                className = entryName.replace("/", ".").replace(".class", "")
                //println("modifyJar className "+className)
                CtClass c = modifyClass(className)
                if (c != null) {
                    modifiedClassBytes = c.toBytecode()
                }
                c.detach()
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
        while (entries.hasMoreElements()) {
            ZipEntry element = entries.nextElement();
            def name = element.getName();
            ZipEntry zipEntry = new ZipEntry(name);

            outputAarStream.putNextEntry(zipEntry);
            if (name.endsWith(".jar")) {
                File innerJar = unzipEntryToTemp(element, zipFile, targetFile.parent);
                def outJar = modifyJar(innerJar);
                outputAarStream.write(IOUtils.toByteArray(new FileInputStream(outJar)))
            } else {
                def stream = zipFile.getInputStream(element)
                byte[] array = IOUtils.toByteArray(stream)
                if (array != null) {
                    outputAarStream.write(array)
                }
            }
            outputAarStream.closeEntry();
        }
        zipFile.close()
        outputAarStream.close()
    }

}