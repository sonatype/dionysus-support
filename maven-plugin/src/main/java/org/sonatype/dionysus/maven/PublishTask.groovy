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

import static com.google.common.base.Preconditions.checkNotNull

/**
 * {@link PublishMojo} task.
 *
 * @since ???
 */
class PublishTask
{
  private static final String GITHUB_GIT_SSL_PREFIX = 'scm:git:ssh://git@github.com/'

  private final PublishMojo mojo

  PublishTask(final PublishMojo mojo) {
    this.mojo = checkNotNull(mojo)
  }

  void execute() {
    mojo.with {
      def siteUrl = project?.distributionManagement?.site?.url
      log.info "Site URL: $siteUrl"
      assert siteUrl, 'Missing site distribution-management configuration'
      assert siteUrl.
          startsWith(GITHUB_GIT_SSL_PREFIX), "Only Github URL with prefix '$GITHUB_GIT_SSL_PREFIX' is suppported"

      def gitRepository = siteUrl - GITHUB_GIT_SSL_PREFIX
      log.info "GIT repository: $gitRepository"

      def gitUrl = "git@github.com:$gitRepository"
      log.info "GIT URL: $gitUrl"

      assert hugoPublicDir.exists(), "Missing Hugo-generated content: $hugoPublicDir"
      log.info("Hugo-generated content directory: $hugoPublicDir")

      log.info("Checkout directory: $checkoutDir")
      if (checkoutDir.exists()) {
        ant.delete(dir: checkoutDir)
      }
      ant.mkdir(dir: checkoutDir)

      log.info "Cloning $gitUrl ($gitBranch branch) to: $checkoutDir"
      snippet {
        ant.exec(executable: gitExecutable, dir: checkoutDir) {
          arg(value: 'clone')
          arg(value: '--branch')
          arg(value: gitBranch)
          arg(value: gitUrl)
          arg(value: '.')
        }
      }

      log.info "Removing existing content from $checkoutDir"
      ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true) {
        arg(value: 'rm')
        arg(value: '-r')
        arg(value: '--quiet')
        arg(value: '*')
      }

      log.info "Copying hugo-generated content to: $checkoutDir"
      ant.copy(todir: checkoutDir) {
        fileset(dir: hugoPublicDir) {
          include(name: '**')
        }
      }

      if (siteDir.exists()) {
        log.info("Maven-generated content directorY: $siteDir")
        File mavenGeneratedDir = checkoutDir
        if (mavenSitePrefix) {
          mavenGeneratedDir = new File(checkoutDir, mavenSitePrefix)
          ant.mkdir(dir: mavenGeneratedDir)
        }
        log.info "Copying maven-generated content to: $mavenGeneratedDir"
        ant.copy(todir: mavenGeneratedDir) {
          fileset(dir: siteDir) {
            include(name: '**')
          }
        }
      }

      log.info 'Adding changed files'
      ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true) {
        arg(value: 'add')
        arg(value: '.')
      }

      log.info 'Checking status'
      ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true, outputproperty: 'git-status') {
        arg(value: 'status')
        arg(value: '--short')
      }
      def status = (ant.project.properties.'git-status' as String).trim()

      if (status) {
        snippet { println status }

        log.info 'Committing changes'
        snippet {
          ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true) {
            arg(value: 'commit')
            arg(value: '-m')
            arg(value: 'Site content refresh')
          }
        }

        if (!dryRun) {
          log.info 'Pushing changes'
          snippet {
            ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true) {
              arg(value: 'push')
              arg(value: 'origin')
              arg(value: gitBranch)
            }
          }
        }
      }
      else {
        log.info 'No changes detected'
      }

      log.info 'Done'
    }
  }
}
