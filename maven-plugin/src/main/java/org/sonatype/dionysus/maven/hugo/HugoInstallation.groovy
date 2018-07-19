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
package org.sonatype.dionysus.maven.hugo

import javax.annotation.Nullable

import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.SystemUtils
import org.apache.maven.project.MavenProject

/**
 * Hugo installation helper.
 *
 * @since 1.0.0
 */
@Slf4j
class HugoInstallation
{
  AntBuilder ant

  MavenProject project

  @Nullable
  File defaultExecutable

  @Memoized
  File getExecutable() {
    assert ant != null
    assert project != null
    File result = defaultExecutable

    // if hugo executable is missing, extract from resources if we can
    if (!result.exists()) {
      // resolve resource
      def resource = getClass().getResource("hugo-${systemType}")
      assert resource != null
      log.debug("Hugo resource: $resource")

      // extract resource to local file
      File buildDir = new File(project.build.directory)
      ant.mkdir(dir: buildDir)
      File hugo = new File(buildDir, 'hugo')
      hugo.bytes = resource.bytes

      // ensure executable
      ant.chmod(perm: 'u+x', file: hugo)
      result = hugo
    }

    // verify installation
    log.info("Hugo executable: $result")
    ant.exec(executable: result, failonerror: true) {
      arg(value: 'version')
    }

    return result
  }

  private static String getSystemType() {
    if (SystemUtils.IS_OS_MAC_OSX) {
      return 'macos'
    }
    else if (SystemUtils.IS_OS_LINUX) {
      return 'linux'
    }

    throw new RuntimeException("Unsupported OS: ${SystemUtils.OS_NAME}")
  }
}
