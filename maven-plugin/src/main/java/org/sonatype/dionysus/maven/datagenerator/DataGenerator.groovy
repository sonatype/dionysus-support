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
package org.sonatype.dionysus.maven.datagenerator

import org.sonatype.dionysus.maven.util.ContainerHelper
import org.sonatype.dionysus.maven.util.ExpressionHelper

import org.apache.maven.execution.MavenSession
import org.apache.maven.project.MavenProject
import org.apache.maven.settings.Settings

/**
 * Data generator.
 *
 * @since 1.0.0
 */
interface DataGenerator
{
  /**
   * Data generation context.
   */
  static class Context
  {
    AntBuilder ant

    MavenProject project

    List<MavenProject> projects

    MavenSession session

    Settings settings

    ContainerHelper container

    ExpressionHelper expression
  }

  /**
   * Check if generator applies.
   */
  boolean apply(Context context)

  /**
   * Execute generator.
   */
  void execute(Context context) throws Exception
}
