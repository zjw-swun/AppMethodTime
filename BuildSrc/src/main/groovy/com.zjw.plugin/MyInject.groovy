package com.zjw.plugin

import javassist.*

public class MyInject {

    private static ClassPool pool = ClassPool.getDefault()
    //注意这里需要替换你的anroid.jar路径
    static String androidJar = "D:\\Application\\Android\\sdkUpDate\\platforms\\android-24\\android.jar"
    static String myPackageName = "com.zjw.appmethodtime";
    static String myCostTimeAnnotation = "@com.zjw.appmethodtime.CostTime";
    static String CostTime = "CostTime";
    static String AppMethodTime = "AppMethodTime";
    static String AppMethodOrder = "AppMethodOrder";
    static String LogLevel = "e";

    // 存储文件列表
    private static ArrayList<String> fileList = new ArrayList<>();


    public static void injectDir(String path,String jarsPath, String packageName, boolean enabeCostTime) {
        //path is D:\GitBlit\AppMethodTime\app\build\intermediates\classes\debug
        pool.appendClassPath(path)
        // 以下为windows环境下你的相应android.jar路径
        pool.insertClassPath(androidJar)
        //因为com.zjw.mylibrary.Bean 是lib库的代码 class只会在该目录下 编译顺序：先编译lib库再编译主项目
        //不加以下代码 当javasist插入到 public void onEventMain(Bean event)该方法时找不到 Bean类
        //原因就是path 和 androidJar 路径下找不到第三方和本地库中所引用的类

        //加载第三方和本地库的jar
        File libJarDir = new File(jarsPath)
        try {
            if (libJarDir.exists() && libJarDir.isDirectory()) {
                ArrayList<String> arr = getFile(libJarDir);
                for (String a : arr) {
                    pool.appendClassPath(a);
                }
            }
        }catch (Exception e){

        }

        println("enabeCostTime is " + enabeCostTime)

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                //确保当前文件是class文件，并且不是系统自动生成的class文件以及注解文件
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")
                        && !filePath.contains("CostTime")
                ) {
                    // 判断当前目录是否是在我们的应用包里面
                    int index = filePath.indexOf(packageName);
                    boolean isMyPackage = index != -1;
                    if (isMyPackage) {
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end).replace('\\', '.').replace('/', '.')
                        //开始修改class文件
                        CtClass c = pool.getCtClass(className)

                        if (c.isFrozen()) {
                            c.defrost()
                        }
                        pool.importPackage(myPackageName)

                       /* //给类添加计时变量
                        CtField startTime = new CtField(CtClass.longType, "sStart", c);
                        startTime.setModifiers(Modifier.STATIC);
                        c.addField(startTime);

                        //给类添加计时变量
                        CtField endTime = new CtField(CtClass.longType, "sEnd", c);
                        endTime.setModifiers(Modifier.STATIC);
                        c.addField(endTime);
*/
                        //遍历类的所有方法
                        CtMethod[] methods = c.getDeclaredMethods();
                        for (CtMethod method : methods) {
                            println("method ====" + method.longName)
                            if (enabeCostTime
                                    && method.getAvailableAnnotations() != null
                                    && method.getAvailableAnnotations().length >= 1
                                    && "${method.getAvailableAnnotations()[0]}".contains(CostTime)
                            ) {
                                insertCostTimeCode(method,c)
                                println("enabeCostTime true ")
                            } else if (!enabeCostTime) {
                                println("enabeCostTime false ")
                                insertCostTimeCode(method,c)
                            }
                        }//END   for (CtMethod method : methods)
                        c.writeFile(path)
                        c.detach()
                    }//END if(isMyPackage)
                }
            }
        }
    }

    private static void insertCostTimeCode(CtMethod method,CtClass c) {
        println("insertCostTimeCode  method "+method.longName)

        method.addLocalVariable("sStart",CtClass.longType);
        method.addLocalVariable("sEnd",CtClass.longType);

        def StringType = pool.getCtClass("java.lang.String");
        method.addLocalVariable("fullClassName",StringType);
        method.addLocalVariable("className",StringType);
        method.addLocalVariable("methodName",StringType);
        method.addLocalVariable("lineNumber",CtClass.intType);
        method.addLocalVariable("info",StringType);

        def lineNumber = method.methodInfo.getLineNumber(0);
        //插入到函数第一句
        StringBuilder startInjectStr = new StringBuilder();
        startInjectStr.append(" sStart = System.nanoTime();\n");
        startInjectStr.append(" fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();\n");
        startInjectStr.append(" className = fullClassName.substring(fullClassName.lastIndexOf(\".\") + 1)+\".java\";\n");
        startInjectStr.append(" methodName = Thread.currentThread().getStackTrace()[2].getMethodName();\n");
        //startInjectStr.append(" lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();\n");
        startInjectStr.append(" lineNumber = "+lineNumber+";\n");
        startInjectStr.append(" info = fullClassName+\": \"+methodName + \" (\" + className + \":\"+ lineNumber + \")\";\n");

        startInjectStr.append("android.util.Log.${LogLevel}(\"${AppMethodOrder}\",");
        startInjectStr.append("info + \": \" ");
        startInjectStr.append("\"\"); ");
        method.insertBefore(startInjectStr.toString())
        print("方法第一句插入了：" + startInjectStr.toString() + "语句\n")

        //插入到函数最后一句
        StringBuilder endInjectStr = new StringBuilder();
        endInjectStr.append(" sEnd = System.nanoTime();\n");
        endInjectStr.append("android.util.Log.${LogLevel}(\"${AppMethodTime}\",");
        endInjectStr.append("info + \": \" ");
        endInjectStr.append("+(sEnd - sStart)*1.0f/1000000+\" (毫秒)\");");
        method.insertAfter(endInjectStr.toString())
        print("方法最后一句插入了：" + endInjectStr.toString() + "语句\n")
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

}