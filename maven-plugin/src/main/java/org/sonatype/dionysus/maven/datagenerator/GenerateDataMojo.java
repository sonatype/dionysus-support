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
package org.sonatype.dionysus.maven.datagenerator;

import java.util.List;

import org.sonatype.dionysus.maven.MojoSupport;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;

/**
 * Generate Hugo site data from Maven models.
 *
 * @since ???
 */
@Mojo(name = "generate-data", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
class GenerateDataMojo
    extends MojoSupport
{
  @Component
  PlexusContainer container;

  @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
  MojoExecution mojoExecution;

  @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
  List<MavenProject> projects;

  /**
   * List of {@link DataGenerator} to generate data files.
   */
  @Parameter(required = true)
  List<DataGenerator> generators;

  @Override
  protected void doExecute() {
    new GenerateDataTask(this).execute();
  }
}
