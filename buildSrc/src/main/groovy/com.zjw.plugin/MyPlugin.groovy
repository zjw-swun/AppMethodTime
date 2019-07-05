package com.zjw.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle


public class MyPlugin implements Plugin<Project> {

    void apply(Project project) {

        //获得启动任务字符串
        Gradle gradle = project.getGradle()
        String tskReqStr = gradle.getStartParameter().getTaskRequests().toString()
        String buildType = ""
        if (tskReqStr.contains("Debug")) {
            buildType = "Debug"
        } else if (tskReqStr.contains("Release")) {
            buildType = "Release"
        }
        //注册build.gradle中
        project.extensions.create('AppMethodTime', MyCustomPluginExtension)

        /*  project.task('appPlugin') << {
              project.pluginsrc.cost
          }*/

        if (project.plugins.hasPlugin(AppPlugin)) {
            def android = project.extensions.findByType(AppExtension)
            android.registerTransform(new MyTransform(project, buildType, false))
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            def android = project.extensions.findByType(LibraryExtension)
            android.registerTransform(new MyTransform(project, buildType, true))
        }
    }
}