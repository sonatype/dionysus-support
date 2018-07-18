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
 * {@link PrepareBranchMojo} task.
 *
 * @since ???
 */
class PrepareBranchTask
{
  private static final String GITHUB_GIT_SSL_PREFIX = 'scm:git:ssh://git@github.com/'

  private final PrepareBranchMojo mojo

  PrepareBranchTask(final PrepareBranchMojo mojo) {
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
      snippet {
        ant.exec(executable: gitExecutable, dir: checkoutDir) {
          arg(value: 'rm')
          arg(value: '-rf')
          arg(value: '.')
        }
      }

      log.info "Preparing initial content"
      new File(checkoutDir, 'index.html').text = """<html>
<head><title>initial</title>
<body>initial</body>
</html>
"""

      log.info 'Adding files'
      ant.exec(executable: gitExecutable, dir: checkoutDir, failonerror: true) {
        arg(value: 'add')
        arg(value: 'index.html')
      }

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

      log.info 'Done'
    }
  }
}
