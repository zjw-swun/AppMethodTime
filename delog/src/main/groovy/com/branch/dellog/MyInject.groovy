package com.branch.dellog;

import javassist.*

public class MyInject {

    private static ClassPool pool = ClassPool.getDefault()
    private
    static String androidJar = "D:\\Application\\Android\\sdkUpDate\\platforms\\android-24\\android.jar"

    public static void injectDir(String path, String packageName) {
        pool.appendClassPath(path)
        // 以下为windows环境下你的相应android.jar路径
        pool.insertClassPath(androidJar)


        File dir = new File(path)
        if (dir.isDirectory()) {
            try {
                dir.eachFileRecurse { File file ->

                    String filePath = file.absolutePath
                    //确保当前文件是class文件，并且不是系统自动生成的class文件
                    if (filePath.endsWith(".class")
                            && !filePath.contains('R$')
                            && !filePath.contains('R.class')
                            && !filePath.contains("BuildConfig.class")
                        && !filePath.contains("com.branch.dellog")
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
                            pool.importPackage("com.zjw.appmethodtime")

                            //遍历类的所有方法
                            CtMethod[] methods = c.getDeclaredMethods();
                            for (CtMethod method : methods) {
                                //在每个方法之前插入判断语句，判断类的补丁实例是否存在
                                StringBuilder startInjectStr = new StringBuilder();
                                //  long start = System.nanoTime();
                                // long end = System.nanoTime();
                                // Log.e("AppMethodTime","onCreate "+(end-start)/1000000);
                                startInjectStr.append("long start = System.nanoTime();");
                                print("方法第一句插入了：" + startInjectStr.toString() + "语句")
                                method.insertBefore(startInjectStr.toString())

                                //扫描到return 就插入 如果不是就插入到最后一句
                                StringBuilder endInjectStr = new StringBuilder();
                                endInjectStr.append("long end = System.nanoTime();\n");
                                endInjectStr.append("android.util.Log.e(\"AppMethodTime\",");
                                endInjectStr.append("\"method.longName");
                                endInjectStr.append("(end-start)/1000000 (毫秒)\");");
                                print("方法最后一句插入了：" + endInjectStr.toString() + "语句")
                                method.insertAfter(endInjectStr.toString(), true)
                            }
                        }
                        c.writeFile(path)
                        c.detach()
                    }
                }
            }catch (Throwable e){

            }
        }
    }
}