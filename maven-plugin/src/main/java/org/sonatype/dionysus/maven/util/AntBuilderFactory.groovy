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
package org.sonatype.dionysus.maven.util

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import groovy.util.logging.Slf4j
import org.apache.tools.ant.DefaultLogger
import org.apache.tools.ant.Project
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.logging.LoggerManager

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Factory to create Maven-adapted {@link AntBuilder} instances.
 *
 * @since ???
 */
@Named
@Singleton
@Slf4j
class AntBuilderFactory
{
  @Inject
  LoggerManager loggerManager

  AntBuilder create(final Class owner) {
    checkNotNull(owner)

    def project = new Project()
    def logger = loggerManager.getLoggerForComponent(owner.name)

    project.addBuildListener(new MavenAntLoggerAdapter(logger))
    project.init()
    return new AntBuilder(project)
  }

  private static class MavenAntLoggerAdapter
      extends DefaultLogger
  {
    private final Logger logger

    MavenAntLoggerAdapter(final Logger logger) {
      this.logger = checkNotNull(logger)
      emacsMode = true

      // default to INFO enabled
      int level = Project.MSG_INFO

      // if mvn --debug, use DEBUG
      if (logger.debugEnabled) {
        level = Project.MSG_DEBUG
      }
      // if mvn --quiet limit to ERROR
      else if (!logger.warnEnabled) {
        level = Project.MSG_ERR
      }

      messageOutputLevel = level
    }

    @Override
    protected void printMessage(final String message, final PrintStream stream, final int priority) {
      switch (priority) {
        case Project.MSG_ERR:
          logger.error(message)
          break

        case Project.MSG_WARN:
          logger.warn(message)
          break

        case Project.MSG_INFO:
          logger.info(message)
          break

        case Project.MSG_VERBOSE:
        case Project.MSG_DEBUG:
          logger.debug(message)
          break
      }
    }
  }
}
