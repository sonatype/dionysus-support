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

import org.sonatype.dionysus.maven.util.AntBuilderFactory

import groovy.transform.Memoized
import org.apache.maven.artifact.Artifact
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.DefaultProjectBuildingRequest
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Support for {@link Mojo} implementations.
 *
 * @since ???
 */
abstract class MojoSupport
  extends AbstractMojo
{
  public final Logger log = LoggerFactory.getLogger(getClass())

  @Component
  AntBuilderFactory antFactory

  @Parameter(defaultValue = '${project}', readonly = true)
  MavenProject project

  @Parameter(defaultValue = '${session}', readonly = true)
  MavenSession session

  @Parameter(defaultValue = '${settings}', readonly = true)
  Settings settings

  @Component
  ArtifactResolver artifactResolver

  // helper to run a task surrounded by snippet markers
  def snippet = { Closure task ->
    println '----8<----'
    task.run()
    println '---->8----'
  }

  @Memoized
  AntBuilder getAnt() {
    log.debug("Creating AntBuilder for: ${getClass()}")
    return antFactory.create(getClass())
  }

  @Override
  void execute() throws MojoExecutionException, MojoFailureException {
    try {
      doExecute()
    }
    catch (MojoExecutionException|MojoFailureException e) {
      throw e
    }
    catch (Exception e) {
      throw new MojoExecutionException('Error', e)
    }
  }

  protected abstract void doExecute()

  Artifact resolveArtifact(final ArtifactCoordinate coordinate) {
    log.debug("Resolving artifact: $coordinate")

    def request = new DefaultProjectBuildingRequest(session.projectBuildingRequest)
    request.setRemoteRepositories(project.remoteArtifactRepositories)

    def result = artifactResolver.resolveArtifact(request, coordinate)
    log.debug("Resolved: ${result.artifact} -> ${result.artifact.file}")
    return result.artifact
  }
}
