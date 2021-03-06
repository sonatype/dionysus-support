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

import groovy.util.logging.Slf4j
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Maven expression helper.
 *
 * @since 1.0.0
 */
@Slf4j
class ExpressionHelper
{
  private final ExpressionEvaluator evaluator

  ExpressionHelper(final ExpressionEvaluator evaluator) {
    this.evaluator = checkNotNull(evaluator)
  }

  Object get(final String expression) {
    log.debug("Evaluate: $expression")
    def result = evaluator.evaluate(expression)
    log.debug("Result: $result")
    return result
  }

  File file(final String expression) {
    String value = get(expression)
    if (value != null) {
      return new File(value)
    }
    return null
  }
}
