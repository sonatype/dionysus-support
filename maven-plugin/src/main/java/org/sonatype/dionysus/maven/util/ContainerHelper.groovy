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

import org.codehaus.plexus.PlexusContainer

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Maven container abstraction.
 *
 * @since 1.0.0
 */
class ContainerHelper
{
  private final PlexusContainer container

  ContainerHelper(final PlexusContainer container) {
    this.container = checkNotNull(container)
  }

  def <T> T lookup(final Class<T> type) {
    return container.lookup(type)
  }
}
