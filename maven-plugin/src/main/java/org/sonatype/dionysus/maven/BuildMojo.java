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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Build site content.
 *
 * @since ???
 */
@Mojo(name="build", requiresDirectInvocation = true, aggregator = true)
class BuildMojo
  extends MojoSupport
{
  @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
  private List<MavenProject> projects;

  /**
   * Coordinates to theme bundle.
   *
   * This needs to be {@code zip} artifact.
   */
  @Parameter
  ThemeCoordinate theme;

  /**
   * Name of theme.
   *
   * When used with {@link #theme} parameter this will be the directory the theme bundle will be installed.
   */
  @Parameter(property = "dionysus.themeName", defaultValue = "dionysus", required = true)
  String themeName;

  /**
   * Rebuild the theme.  This requires additional setup to function.
   */
  @Parameter(property = "dionysus.rebuildTheme", defaultValue = "false")
  boolean rebuildTheme;

  /**
   * Generate Hugo content.
   */
  @Parameter(property = "dionysus.generateHugo", defaultValue = "true")
  boolean generateHugo;

  /**
   * Hugo executable.
   *
   * This can be overridden to a locally installed Hugo, or by default will extract a binary for the platform.
   *
   * Only supports Mac and Linux.
   */
  @Parameter(property = "dionysus.hugoExecutable", defaultValue = "${project.build.directory}/hugo", required = true)
  File hugoExecutable;

  /**
   * Additional options to pass to the {@code hugo} executable.
   */
  @Parameter
  List<String> hugoOptions = new ArrayList<>();

  /**
   * Location of the Hugo site.
   */
  @Parameter(property = "dionysus.hugoDir", defaultValue = "${project.basedir}/src/site/hugo", required = true)
  File hugoDir;

  /**
   * Generate Maven content.
   *
   * Maven execution will have {@code dionysus} {@code dionysus-generate-maven-site} profiles enabled
   * and include any configured {@link #mavenOptions}.
   */
  @Parameter(property = "dionysus.generateMaven", defaultValue = "true")
  boolean generateMaven;

  /**
   * Additional options to pass to Maven executions.
   *
   * @see #generateMaven
   * @see #generateData
   */
  @Parameter
  List<String> mavenOptions = new ArrayList<>();

  /**
   * The goals which will be executed to generate Maven content.
   */
  @Parameter
  List<String> mavenGoals = new ArrayList<>();

  /**
   * Generate Hugo data with {@code org.sonatype.goodies.dionysus:dionysus-maven-plugin:generate-data}.
   *
   * Maven execution will have {@code dionysus} and {@code dionysus-generate-data} profiles enabled
   * and include any configured {@link #mavenOptions}.
   */
  @Parameter(property = "dionysus.generateData", defaultValue = "true")
  boolean generateData;

  /**
   * Collect generated Hugo data.
   */
  @Parameter(property = "dionysus.collectData", defaultValue = "true")
  boolean collectData;

  /**
   * Where generate Hugo data will be collected to.
   */
  @Parameter(property = "dionysus.hugoData", defaultValue = "${project.basedir}/src/site/hugo/data", required = true)
  File hugoData;

  @Override
  protected void doExecute() {
    new BuildTask(this).execute();
  }
}
