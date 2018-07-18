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
 * Publish site content for use with Github pages.
 *
 * Requires site to have been previously built with {@code dionysus:build}.
 *
 * @since ???
 */
@Mojo(name="publish", requiresDirectInvocation = true, aggregator = true)
class PublishMojo
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
   * Hugo-generated content directory.
   *
   * This needs to match the value configured in Hugo configuration {@code publishdir} parameter.
   */
  @Parameter(property = "dionysus.hugoPublicDir", defaultValue = "${project.build.directory}/hugo-public", required = true)
  File hugoPublicDir;

  /**
   * Maven-generated content directory.
   */
  @Parameter(property = "dionysus.siteDir", defaultValue = "${project.build.directory}/site")
  File siteDir;

  /**
   * Prefix for where Maven-generated will be stored.
   */
  @Parameter(property = "dionysus.mavenSitePrefix", defaultValue = "maven")
  String mavenSitePrefix;

  /**
   * Directory where a checkout of configured branch to perform changes will be done.
   */
  @Parameter(property = "dionysus.checkoutDir", defaultValue = "${project.build.directory}/publish-checkout", required = true)
  File checkoutDir;

  @Override
  protected void doExecute() {
    new PublishTask(this).execute();
  }
}
