/*
 * Copyright (c) 2018-present Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.dionysus.maven.datagenerator

import org.sonatype.dionysus.maven.datagenerator.DataGenerator.Context
import org.sonatype.dionysus.maven.util.ContainerHelper
import org.sonatype.dionysus.maven.util.ExpressionHelper

import com.google.common.collect.ImmutableList
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.PluginParameterExpressionEvaluator

import static com.google.common.base.Preconditions.checkNotNull

/**
 * {@link GenerateDataMojo} task.
 *
 * @since 1.0.0
 */
class GenerateDataTask
{
  private final GenerateDataMojo mojo

  GenerateDataTask(final GenerateDataMojo mojo) {
    this.mojo = checkNotNull(mojo)
  }

  void execute() {
    mojo.with {
      log.info("Executing: ${project}")

      def context = new Context(
          ant: ant,
          project: project,
          projects: ImmutableList.copyOf(projects),
          session: session,
          settings: settings,
          container: new ContainerHelper(container),
          expression: new ExpressionHelper(new PluginParameterExpressionEvaluator(session, mojoExecution))
      )

      // filter applicable generators
      log.info("${generators.size()} generators configured")
      def applicable = generators.findAll { it.apply(context) }
      log.info("${applicable.size()} generators are applicable")

      List<Exception> errors = []
      applicable.each { generator ->
        log.info "Executing: $generator"
        try {
          generator.execute(context)
        }
        catch (Exception e) {
          log.warn("Generator error", e)
          errors << e
        }
      }

      if (!errors.empty) {
        throw new MojoExecutionException("${errors} generators failed with errors")
      }
    }
  }
}
