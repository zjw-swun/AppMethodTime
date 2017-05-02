package com.branch.dellog

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by Ryze on 2017-2-5.
 */
class DelLogPlugin implements Plugin<Project> {
    void apply(Project project) {
        println "我是自定义插件MyPlugin"

        project.extensions.create('pluginsrc', MyCustomPluginExtension)

        /*  project.task('appPlugin') << {
              project.pluginsrc.cost
          }*/

        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new MyTransform(project))
    }
}
