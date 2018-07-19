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

import org.apache.maven.model.io.ModelWriter
import org.json.XML

/**
 * Generate {@code hugo-data/maven/pom/${project.artifactId}.json}.
 *
 * @since 1.0.0
 */
class MavenPom
    extends DataGeneratorSupport
{
  @Override
  void execute(final Context context) throws Exception {
    def modelWriter = context.container.lookup(ModelWriter.class)
    def project = context.project

    // TODO: expose for configuration
    File outputFile = context.expression.file(
        '${project.build.directory}/hugo-data/maven/pom/${project.artifactId}.json'
    )
    context.ant.mkdir(dir: outputFile.parentFile)

    File tmp = File.createTempFile(project.artifactId, '.xml')
    modelWriter.write(tmp, [:], project.model)

    def parsed = XML.toJSONObject(tmp.text)
    def json = parsed.toString(2)

    log.info "Writing: $outputFile"
    outputFile.text = json
  }
}
