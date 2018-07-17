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

import javax.annotation.Nullable

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Generate {@code hugo-data/maven/licenses.json}.
 *
 * @since ???
 */
class MavenLicenses
    extends DataGeneratorSupport
{
  @Override
  boolean apply(final Context context) {
    return context.project.executionRoot && !context.project.model.licenses?.empty
  }

  static class License
  {
    @JsonProperty
    String name

    @JsonProperty
    String url

    @JsonProperty
    String content
  }

  static class Model
  {
    @JsonProperty
    List<License> licenses = []
  }

  @Override
  void execute(final Context context) throws Exception {
    def project = context.project

    // TODO: expose for configuration
    File outputFile = context.expression.file(
        '${project.build.directory}/hugo-data/maven/licenses.json'
    )
    context.ant.mkdir(dir: outputFile.parentFile)

    def model = new Model()
    project.model.licenses.each { license ->
      log.info("License: ${license.name} -> ${license.url}")

      def entry = new License(
          name: license.name,
          url: license.url
      )

      // attempt to fetch license content from url unless offline
      if (license.url && !context.session.offline) {
        entry.content = fetchContent(license.url)
      }

      model.licenses << entry
    }

    def objectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_EMPTY)

    log.info "Writing: $outputFile"
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, model)
  }

  @Nullable
  private String fetchContent(final String urlspec) {
    try {
      log.debug("Attempting to fetch content from: $urlspec")
      def url = new URL(urlspec)
      return url.text
    }
    catch (e) {
      log.warn("Failed to fetch content", e)
    }
    return null
  }
}
