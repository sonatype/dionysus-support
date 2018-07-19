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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Support for {@link DataGenerator} implementations.
 *
 * @since 1.0.0
 */
abstract class DataGeneratorSupport
  implements DataGenerator
{
  protected final Logger log = LoggerFactory.getLogger(getClass())

  @Override
  boolean apply(final Context context) {
    return true
  }

  @Override
  String toString() {
    // if default generator, use simple-name, else use full name
    if (getClass().package.name == DataGeneratorSupport.class.package.name) {
      return getClass().simpleName
    }
    return getClass().name
  }
}
