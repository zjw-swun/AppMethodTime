package com.branch.dellog

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by Ryze on 2017-2-5.
 */
class DelLogPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {


    project.extensions.create('dellogExtension', DelLogExtension);

    project.afterEvaluate {
      //在gradle 构建完之后执行
      project.logger.error("dellogExtension : " + project.dellogExtension.sourceDir);

      def rootDir = project.projectDir.toString().plus(project.dellogExtension.sourceDir);

      project.logger.error(rootDir);

      DelLogUtil.delLog(new File(rootDir));
    }

    project.task('dellog', {
      project.logger.error("dellogExtension : " + project.dellogExtension.sourceDir);

      def rootDir = project.projectDir.toString().plus(project.dellogExtension.sourceDir);

      project.logger.error(rootDir);

      DelLogUtil.delLog(new File(rootDir));

    })

  }
}
