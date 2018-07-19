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
package org.sonatype.dionysus.maven

import org.sonatype.dionysus.maven.hugo.HugoInstallation
import org.sonatype.dionysus.maven.util.MavenInstallation

import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate

import static com.google.common.base.Preconditions.checkNotNull

/**
 * {@link BuildMojo} taks.
 *
 * @since 1.0.0
 */
class BuildTask
{
  private final BuildMojo mojo

  BuildTask(final BuildMojo mojo) {
    this.mojo = checkNotNull(mojo)
  }

  private def maven = new MavenInstallation()

  private HugoInstallation getHugo() {
    mojo.with {
      return new HugoInstallation(
          ant: ant,
          project: project,
          defaultExecutable: hugoExecutable
      )
    }
  }

  void execute() {
    mojo.with {
      log.info("Hugo directory: $hugoDir")
      assert hugoDir.exists(), "Missing Hugo directory: $hugoDir"

      // resolve the theme, either download and unpack or use existing theme directory
      def themesDir = new File(hugoDir, 'themes')
      def themeDir = new File(themesDir, themeName)

      log.info("Theme: $theme")
      if (theme != null) {
        // TODO: consider resolving missing version from projects DM configuration?
        def coordinate = new DefaultArtifactCoordinate(
            groupId: theme.groupId,
            artifactId: theme.artifactId,
            version: theme.version,
            extension: 'zip'
        )
        def artifact = resolveArtifact(coordinate)
        assert artifact.file.exists()

        ant.mkdir(dir: themeDir)
        ant.unzip(src: artifact.file, dest: themeDir) {
          cutdirsmapper(dirs: 1)
        }
      }

      assert themeDir.exists(), "Missing Hugo theme directory: $themeDir"

      // optionally rebuild theme; may require additional configuration to function properly
      if (rebuildTheme) {
        log.info 'Rebuilding Hugo theme'
        snippet {
          ant.exec(executable: 'yarn', dir: themeDir, failonerror: true) {
            arg(value: 'build')
          }
        }
      }

      // optionally generate hugo data files
      if (generateData) {
        log.info 'Generating data'
        snippet {
          ant.exec(executable: maven.executable, dir: project.basedir, failonerror: true) {
            arg(value: '-Pdionysus')
            arg(value: '-Pdionysus-generate-data')
            mavenOptions.each { option ->
              arg(value: option)
            }
            arg(value: 'org.sonatype.goodies.dionysus:dionysus-maven-plugin:generate-data')
          }
        }
      }

      // optionally collect hugo data files
      if (collectData) {
        log.info 'Collecting data'
        ant.mkdir(dir: hugoData)
        snippet {
          projects.each {
            def moduleDir = new File(it.build.directory)
            def dataDir = new File(moduleDir, 'hugo-data')
            if (dataDir.exists()) {
              ant.copy(todir: hugoData) {
                fileset(dir: dataDir) {
                  include(name: "**")
                }
              }
            }
          }
        }
      }

      // optionally generate hugo site
      if (generateHugo) {
        log.info "Generating Hugo site; options: $hugoOptions"
        snippet {
          ant.exec(executable: hugo.executable, dir: hugoDir, failonerror: true) {
            hugoOptions.each { option ->
              arg(value: option)
            }
          }
        }
      }

      // optionally generate maven site
      if (generateMaven) {
        log.info "Generating Maven site; goals: $mavenGoals"
        assert mavenGoals.size() != 0, 'One or more goals is required for Maven generation'
        snippet {
          ant.exec(executable: maven.executable, dir: project.basedir, failonerror: true) {
            arg(value: '-Pdionysus')
            arg(value: '-Pdionysus-generate-maven-site')
            mavenOptions.each { option ->
              arg(value: option)
            }
            mavenGoals.each { goal ->
              arg(value: goal)
            }
          }
        }
      }

      log.info 'Done'
    }
  }
}
