package com.zjw.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class MyTransform extends Transform {
    Project project
    String buildType
    boolean isLib
    HashMap<String, String> map = new HashMap<>()
    // 构造函数，我们将Project保存下来备用
    public MyTransform(Project project, String buildType, boolean isLib) {
        this.project = project
        this.buildType = buildType
        this.isLib = isLib
        project.task('appMethodJarOrAar') {
            doLast {
                MyInject.injectDir(getAndroidJarPath(), "", "",map,
                        project.AppMethodTime.useCostTime, project.AppMethodTime.showLog, project.AppMethodTime.aarOrJarPath, buildType)
            }
        }
    }

    // 设置我们自定义的Transform对应的Task名称
    // 将会在对应module的build\intermediates\transforms\目录下的生成MyTrans目录
    // 例如本案例(D:\GitBlit\AppMethodTime\app\build\intermediates\transforms\MyTrans)
    @Override
    public String getName() {
        return "MyTrans"
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
    //这样确保其他类型的文件不会传入
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    // 指定Transform的作用范围
    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        if (isLib) {
            return Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT);
        } else {
            return TransformManager.SCOPE_FULL_PROJECT;
        }
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(@NonNull TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        if (!project.AppMethodTime.enabled) {
            return;
        }
        def androidJarPath = getAndroidJarPath();
        fillJarMap()
        String jarsDir
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        transformInvocation.inputs.each { TransformInput input ->
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件 输出表明 还包括了自建的依赖lib库的jar文件
                // （也就是主项目build.gradle中 dependencies下 compile的东西）
                // println("jarInput.file.getAbsolutePath() === " + jarInput.file.getAbsolutePath())
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                matchJarClassPath(jarName)
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                    // println("jarName substring is " + jarName)
                }
                //生成输出路径
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)

                //AppMethodTime\app\build\intermediates\transforms\MyTrans\debug\jars 拼凑这个目录
                jarsDir = dest.absolutePath.split("jars")[0] + "jars";
                // println("dest === " + dest.absolutePath)

                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }


            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等

                // directoryInput.file =============D:\GitBlit\AppMethodTime\app\build\intermediates\classes\debug
                MyInject.injectDir(androidJarPath, directoryInput.file.absolutePath, jarsDir,map,
                        project.AppMethodTime.useCostTime, project.AppMethodTime.showLog, project.AppMethodTime.aarOrJarPath, buildType)
                // directoryInput.file =============D:\GitBlit\AppMethodTime\app\build\intermediates\classes\debug
                // dest.name =============bb2a44c10a4b1f1ea8a3f7b22453e3a96aa0d55d
                // 获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                //println("directoryInput.file =============" + directoryInput.file);
                // println("dest.name =============" + dest.name);

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

        }
    }

    private void fillJarMap() {
        def librariesRoot = project.rootDir.path + "${File.separator}.idea${File.separator}libraries${File.separator}"
        def home = System.getProperty("user.home") //  /Users/hana
        File libraryFile = new File(librariesRoot)
        File[] resourceFiles
        if (libraryFile != null && libraryFile.isDirectory()) {
            resourceFiles = libraryFile.listFiles()

            def xmlparser = new XmlParser()
            resourceFiles.each {
                file ->
                    def component = xmlparser.parse(file)
                    String relativePath = component.library.CLASSES.root.@url[0] + ""
                    int headIndex = relativePath.indexOf(".gradle")
                    int bottomIndex = relativePath.indexOf(".jar")
                    if (headIndex > 0 && bottomIndex > 0) {
                        String indexPath = relativePath.substring(headIndex, bottomIndex)
                        String resultPath = home + "${File.separator}" + indexPath + ".jar"
                        String name = component.library.@name[0]
                        String fixName = name.replace("Gradle: ", "")
                        map.put(fixName, resultPath)
                        //lifecycle-common-2.1.0.jar
                        println(resultPath)
                        ///Users/hana/.gradle/caches/modules-2/files-2.1/androidx.lifecycle/lifecycle-common/2.1.0/c67e7807d9cd6c329b9d0218b2ec4e505dd340b7/lifecycle-common-2.1.0.jar
                    }

            }
        } else {
            println("libraryFile is empty")
        }
    }

    /**
     * todo 这里可以设置配置指定的jar名称，或者全量
     * todo 将match result 添加到classpath
     * */
    private String matchJarClassPath(String jarName) {
        //1.com.google.code.gson:gson:2.8.5@jar
        //2.com.google.android.material:material:1.1.0@aar
        //3.gradle-3.4.2
        // jarName com.squareup.okio:okio:1.17.2
        // map key com.google.code.gson:gson:2.8.5@jar
        println("jarName:" + jarName)
        String match = ""
        out:
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().contains(jarName)) {
                match = entry.getValue()
                break out
            }
        }
        /*map.find {
            entry ->
                //println(entry.key)
                //println(entry.value)
                String index = entry.key.replace("-", ":")
                        .replace("@jar","")
                .replace("@aar","")
                if (jarName.contains(index)) {
                    match = entry.value
                    println("match! 000" + entry.value)
                    return true
                }
        }*/
        println(jarName + " matched!")
        println("path:" + match)

    }

    private String getAndroidJarPath() {
        def rootDir = project.rootDir
        def localProperties = new File(rootDir, "local.properties")
        def sdkDir = null;
        if (localProperties.exists()) {
            Properties properties = new Properties()
            localProperties.withInputStream { instr ->
                properties.load(instr)
            }
            sdkDir = properties.getProperty('sdk.dir')
        }

        def platformsPath = sdkDir + File.separator + "platforms"

        def platformsFile = new File(platformsPath)

        if (platformsFile.exists() && platformsFile.isDirectory() && platformsFile.list().length >= 1) {
            return platformsPath + File.separator + platformsFile.list().sort()[platformsFile.list().size() - 1] + File.separator + "android.jar"
        }

        return ""
    }

}