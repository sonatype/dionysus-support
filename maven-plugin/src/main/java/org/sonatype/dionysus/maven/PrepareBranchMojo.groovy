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
 * Prepare Github pages branch for publishing.
 *
 * @since ???
 */
@Mojo(name='prepare-branch', requiresDirectInvocation = true, aggregator = true)
class PrepareBranchMojo
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
   * Directory where a checkout to perform changes will be done.
   */
  @Parameter(property = 'dionysus.checkoutDir', defaultValue = '${project.build.directory}/prepare-checkout', required = true)
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

    log.info("Checkout directory: $checkoutDir")
    if (checkoutDir.exists()) {
      ant.delete(dir: checkoutDir)
    }
    ant.mkdir(dir: checkoutDir)

    log.info "Cloning $gitUrl to: $checkoutDir"
    snippet {
      ant.exec(executable: gitExecutable, dir: checkoutDir) {
        arg(value: 'clone')
        arg(value: gitUrl)
        arg(value: '.')
      }
    }

    log.info "Creating $gitBranch"
    snippet {
      ant.exec(executable: gitExecutable, dir: checkoutDir) {
        arg(value: 'checkout')
        arg(value: '--orphan')
        arg(value: gitBranch)
      }
    }

    log.info "Removing existing content from $checkoutDir"
    ant.delete() {
      fileset(dir: checkoutDir) {
        include(name: '**')
        exclude(name: '.git/**')
      }
    }

    log.info "Preparing initial content"
    new File(checkoutDir, 'index.html').text = """<html>
<head><title>initial</title>
<body>initial</body>
</html>
"""

    /*
    git clone git@github.com:jdillon/dionysus-example.git gh-pages
    cd gh-pages
    git co --orphan gh-pages
    rm -rf * .gitignore
    touch index.html
    git add index.html
    git ci -a -m "initial"
    git push origin gh-pages
   */

    log.info 'Adding changed files'
    ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true) {
      arg(value: 'add')
      arg(value: 'index.html')
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
          arg(value: 'Initial site content')
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
