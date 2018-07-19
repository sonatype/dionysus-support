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
package org.sonatype.dionysus.maven.util

import groovy.transform.Memoized
import groovy.util.logging.Slf4j

/**
 * Maven installation helper.
 *
 * @since 1.0.0
 */
@Slf4j
class MavenInstallation
{
  @Memoized
  File getExecutable() {
    String mavenHome = System.getProperty('maven.home')
    assert mavenHome

    File mavenHomeDir = new File(mavenHome)
    assert mavenHomeDir.exists()
    log.info("Maven home directory: $mavenHomeDir")

    File mvn = new File(mavenHomeDir, 'bin/mvn')
    assert mvn.exists()

    log.info("Maven executable: $mvn")
    return mvn
  }
}
