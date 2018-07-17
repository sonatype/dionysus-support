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

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

/**
 * Publish site content for use with Github pages.
 *
 * Requires site to have been previously built with {@code dionysus:build}.
 *
 * @since ???
 */
@Mojo(name='publish', requiresDirectInvocation = true, aggregator = true)
class PublishMojo
  extends MojoSupport
{
  private static final String GITHUB_GIT_SSL_PREFIX = 'scm:git:ssh://git@github.com/'

  /**
   * Set to avoid push of final changes.
   */
  @Parameter(property = 'dionysus.dryRun', defaultValue = 'false')
  boolean dryRun

  /**
   * GIT executable to use.
   */
  @Parameter(property = 'dionysus.gitExecutable', defaultValue = 'git', required = true)
  String gitExecutable

  /**
   * The branch where content will be published.
   *
   * All content on this branch will be replaced by what is generated.
   */
  @Parameter(property = 'dionysus.gitBranch', defaultValue = 'gh-pages', required = true)
  String gitBranch

  /**
   * Hugo-generated content directory.
   *
   * This needs to match the value configured in Hugo configuration {@code publishdir} parameter.
   */
  @Parameter(property = 'dionysus.hugoPublicDir', defaultValue = '${project.build.directory}/hugo-public', required = true)
  File hugoPublicDir

  /**
   * Maven-generated content directory.
   */
  @Parameter(property = 'dionysus.siteDir', defaultValue = '${project.build.directory}/site')
  File siteDir

  /**
   * Prefix for where Maven-generated will be stored.
   */
  @Parameter(property = 'dionysus.mavenSitePrefix', defaultValue = 'maven')
  String mavenSitePrefix

  /**
   * Directory where a checkout of configured branch to perform changes will be done.
   */
  @Parameter(property = 'dionysus.checkoutDir', defaultValue = '${project.build.directory}/publish-checkout', required = true)
  File checkoutDir

  @Override
  protected void doExecute() {
    def siteUrl = project?.distributionManagement?.site?.url
    log.info "Site URL: $siteUrl"
    assert siteUrl, 'Missing site distribution-management configuration'
    assert siteUrl.startsWith(GITHUB_GIT_SSL_PREFIX), "Only Github URL with prefix '$GITHUB_GIT_SSL_PREFIX' is suppported"

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
