## AppMethodTime

一个测量函数耗时的库(更新readme)


>背景：这次没什么背景就是知识总结，Gradle自定义插件+Transform+javassist 做了个 JakeWharton大神hugo库类似的东西

特别说明：本文已经稍后送上的代码参考以及抄袭了(http://blog.csdn.net/eclipsexys/article/details/50973205)
(https://github.com/HalfStackDeveloper/Savior)，
还有巴掌大神的库(https://github.com/JeasonWong/CostTime) ,十分鸣谢巴掌大神手把手教我改代码
（有用到代码的同学，联系我，有侵必删）<br>

源码下载地址：(https://github.com/zjw-swun/AppMethodTime) 觉得有帮助可以给个star<br>

简单介绍一下：Transform 参与class打包的Android
 gradle plugin的api，javassist 是生成或者修改class字节码的库。借由这2个特性，我们就可以完成类似于JakeWharton大神hugo库类似的东西。

#1.  2种生成自定义插件的方式
1.新建一个BuildSrc module (lib module ，BuildSrc 名字固定)，从src（包括src）开始以下文件或者文件夹都是自己建的（具体步骤就不讲了，CSDN 简书上都有讲）
![QQ图片20170502230318.png](http://upload-images.jianshu.io/upload_images/1857887-3c3cce8ba26e6f22.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

2.比第1种方式名字可以不固定，但是需要带上META那个文件夹，

![QQ图片20170502231259.png](http://upload-images.jianshu.io/upload_images/1857887-e21bd4f63d74cfff.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
该build.gradle 需要加一个task
```
//设置maven deployer
uploadArchives {
    repositories {
        mavenDeployer {
            //设置插件的GAV参数
            pom.groupId = 'com.branch.plugin'
            pom.artifactId = 'dellog'
            pom.version = '1.0.0'
            //文件发布到下面目录
            repository(url: uri('../repo'))
        }
    }
}
```
然后执行``uploadArchives`` 上传
具体不细说看代码吧

核心就是MyTransform类，以及MyInject类
MyInject类里面的代码如下
```
package com.zjw.plugin

import javassist.*

public class MyInject {

    private static ClassPool pool = ClassPool.getDefault()
    //注意这里需要替换你的anroid.jar路径
    static String androidJar = "D:\\Application\\Android\\sdkUpDate\\platforms\\android-24\\android.jar"
    static String myPackageName = "com.zjw.appmethodtime";
    static String myCostTimeAnnotation = "@com.zjw.appmethodtime.CostTime";
    static String CostTime = "CostTime";

    public static void injectDir(String path, String packageName, boolean enabeCostTime) {
        pool.appendClassPath(path)
        // 以下为windows环境下你的相应android.jar路径
        pool.insertClassPath(androidJar)

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

                        //给类添加计时变量
                        CtField startTime = new CtField(CtClass.longType, "sStart", c);
                        startTime.setModifiers(Modifier.STATIC);
                        c.addField(startTime);

                        //给类添加计时变量
                        CtField endTime = new CtField(CtClass.longType, "sEnd", c);
                        endTime.setModifiers(Modifier.STATIC);
                        c.addField(endTime);

                        //遍历类的所有方法
                        CtMethod[] methods = c.getDeclaredMethods();
                        for (CtMethod method : methods) {
                            println("method ====" + method.longName)
                            if (enabeCostTime
                                    && method.getAvailableAnnotations() != null
                                    && method.getAvailableAnnotations().length >= 1
                                    && "${method.getAvailableAnnotations()[0]}".contains(CostTime)
                            ) {
                                insertCostTimeCode(method)
                            } else if (!enabeCostTime) {
                                insertCostTimeCode(method)
                            }
                        }//END   for (CtMethod method : methods)
                        c.writeFile(path)
                        c.detach()
                    }//END if(isMyPackage)
                }
            }
        }
    }

    private static void insertCostTimeCode(CtMethod method) {
        //插入到函数第一句
        StringBuilder startInjectStr = new StringBuilder();
        startInjectStr.append("sStart = System.nanoTime();");
        print("方法第一句插入了：" + startInjectStr.toString() + "语句\n")
        method.insertBefore(startInjectStr.toString())

        //插入到函数最后一句
        StringBuilder endInjectStr = new StringBuilder();
        endInjectStr.append("sEnd = System.nanoTime();\n");
        endInjectStr.append("android.util.Log.e(\"AppMethodTime\",");
        endInjectStr.append("\"" + method.longName + "\"");
        endInjectStr.append("+(sEnd - sStart)/1000000+\" (毫秒)\");");
        print("方法最后一句插入了：" + endInjectStr.toString() + "语句\n")
        method.insertAfter(endInjectStr.toString(), true)
    }
}
```

#2.  插件自定义的配置字段
```
pluginsrc{
    message = 'hello gradle plugin'
    cost = true
}
```
在MyTransform类中
```
 MyInject.injectDir(directoryInput.file.absolutePath,
                        "com" + File.separator + "zjw" + File.separator + "appmethodtime",
                        project.pluginsrc.cost)
```
使用了。如果你想在自定义插件中自己创建的task中在你的app编译生成apk前读取
```
pluginsrc{
    message = 'hello gradle plugin'
    cost = true
}
```
cost 等字段值的话，你得加上
``preBuild.dependsOn appPlugin``

 不说了自定义插件的初级知识应该都在这里了
效果图

![QQ图片20170512022507.png](http://upload-images.jianshu.io/upload_images/1857887-66422b07a91a9485.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
说明：如果函数返回类型是void log中对应`` return is null ``

# 2019/7/6 新增支持修改aar和jar 中class插入字节码
添加aarOrJarPath 配置字段，填入目标jar或者aar路径
执行gradle面板对应项目中other目录appMethodJarOrAar 任务即可在aarOrJarPath 配置的同目录下生成带 Target_前缀的目标jar或者aar文件