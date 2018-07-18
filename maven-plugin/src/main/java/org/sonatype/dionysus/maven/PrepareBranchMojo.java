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
package org.sonatype.dionysus.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Prepare Github pages branch for publishing.
 *
 * @since ???
 */
@Mojo(name="prepare-branch", requiresDirectInvocation = true, aggregator = true)
class PrepareBranchMojo
  extends MojoSupport
{
  /**
   * Set to avoid push of final changes.
   */
  @Parameter(property = "dionysus.dryRun", defaultValue = "false")
  boolean dryRun;

  /**
   * GIT executable to use.
   */
  @Parameter(property = "dionysus.gitExecutable", defaultValue = "git", required = true)
  String gitExecutable;

  /**
   * The branch where content will be published.
   *
   * All content on this branch will be replaced by what is generated.
   */
  @Parameter(property = "dionysus.gitBranch", defaultValue = "gh-pages", required = true)
  String gitBranch;

  /**
   * Directory where a checkout to perform changes will be done.
   */
  @Parameter(property = "dionysus.checkoutDir", defaultValue = "${project.build.directory}/prepare-checkout", required = true)
  File checkoutDir;

  @Override
  protected void doExecute() {
    new PrepareBranchTask(this).execute();
  }
}
