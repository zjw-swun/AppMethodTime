package com.zjw.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class MyTransform extends Transform {
    Project project

    // 构造函数，我们将Project保存下来备用
    public MyTransform(Project project) {
        this.project = project
    }

    // 设置我们自定义的Transform对应的Task名称
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
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(@NonNull TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        transformInvocation.inputs.each { TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等

                // directoryInput.file =============D:\GitBlit\AppMethodTime\app\build\intermediates\classes\debug
                MyInject.injectDir(directoryInput.file.absolutePath,
                        "com" + File.separator + "zjw" + File.separator + "appmethodtime",
                        project.pluginsrc.cost)
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
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
               // println("jarInput.file.getAbsolutePath() === " + jarInput.file.getAbsolutePath())
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name

                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)

            }
        }
    }
}