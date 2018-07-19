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

import groovy.json.JsonOutput

/**
 * Generate {@code hugo-data/maven/modules.json}.
 *
 * @since 1.0.0
 */
class MavenModules
    extends DataGeneratorSupport
{
  @Override
  boolean apply(final Context context) {
    return context.project.executionRoot
  }

  @Override
  void execute(final Context context) throws Exception {
    // TODO: expose for configuration
    File outputFile = context.expression.file(
        '${project.build.directory}/hugo-data/maven/modules.json'
    )
    context.ant.mkdir(dir: outputFile.parentFile)

    log.info("${context.projects.size()} modules:")
    def modules = []
    context.projects.each { module ->
      log.info "  $module"
      modules << [
          groupId   : module.groupId,
          artifactId: module.artifactId,
          version   : module.version,
          packaging : module.packaging
      ]
    }

    log.info "Writing: $outputFile"
    outputFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(modules))
  }
}
