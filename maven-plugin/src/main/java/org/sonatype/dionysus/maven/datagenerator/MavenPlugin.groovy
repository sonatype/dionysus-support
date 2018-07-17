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

import org.sonatype.dionysus.maven.util.JavadocHelper

import org.json.JSONObject
import org.json.XML

/**
 * Generate {@code hugo-data/maven/plugin/${project.artifactId}.json}.
 *
 * @since ???
 */
class MavenPlugin
    extends DataGeneratorSupport
{
  @Override
  boolean apply(final Context context) {
    return context.project.packaging == 'maven-plugin'
  }

  @Override
  void execute(final Context context) throws Exception {
    // TODO: expose for configuration
    File pluginXml = context.expression.file(
        '${project.build.outputDirectory}/META-INF/maven/plugin.xml'
    )
    File outputFile = context.expression.file(
        '${project.build.directory}/hugo-data/maven/plugin/${project.artifactId}.json'
    )

    assert pluginXml.exists()

    // parse document and clean up
    JSONObject document = XML.toJSONObject(pluginXml.text)
    cleanup(document)

    // convert to json
    def json = document.toString(2)

    log.info "Writing: $outputFile"
    context.ant.mkdir(dir: outputFile.parentFile)
    outputFile.text = json
  }

  /**
   * Clean up various text which my have special Javadoc tokens.
   */
  @SuppressWarnings("GroovyAssignabilityCheck")
  private void cleanup(final JSONObject document) {
    document.getJSONObject("plugin").with { plugin ->
      plugin.getJSONObject("mojos").getJSONArray("mojo").each { JSONObject mojo ->
        fixHtml(mojo, 'description')

        mojo.getJSONObject('parameters').getJSONArray('parameter').each { JSONObject parameter ->
          fixHtml(parameter, 'description')
        }
      }
    }
  }

  private void fixHtml(final JSONObject object, final String key) {
    def value = object.getString(key)
    def fixed = JavadocHelper.cleanDescription(value)
    if (fixed) {
      object.put(key, fixed)
    }
  }
}
