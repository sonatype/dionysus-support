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

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.annotation.Nullable

import org.codehaus.plexus.util.StringUtils
import org.w3c.tidy.Tidy

// Some bits extracted and modified from: https://github.com/apache/maven-plugin-tools/blob/master/maven-plugin-tools-generators/src/main/java/org/apache/maven/tools/plugin/generator/GeneratorUtils.java
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Javadoc helper.
 *
 * @since 1.0.0
 */
class JavadocHelper
{
  private static String LINE_SEP = System.getProperty('line.separator')

  /**
   * Clean Javadoc descriptions.
   */
  static String cleanDescription(@Nullable final String description) {
    if (StringUtils.isEmpty(description)) {
      return ''
    }

    // first decode any javadoc tags
    String cleaned = decodeJavadocTags(description)

    // convert all new-lines to breaks
//    cleaned = cleaned.replaceAll(LINE_SEP + LINE_SEP, LINE_SEP)
//    cleaned = cleaned.replaceAll(LINE_SEP, '<br/>')

    // then further clean up with JTidy
    def tidy = new Tidy(
        docType: 'loose',
        XHTML: true,
        xmlOut: true,
        inputEncoding: 'UTF-8',
        outputEncoding: 'UTF-8',
        makeClean: true,
        numEntities: true,
        quoteNbsp: false,
        quiet: true,
        showWarnings: false
    )

    try {
      ByteArrayOutputStream buff = new ByteArrayOutputStream(cleaned.length() + 256)
      tidy.parse(new ByteArrayInputStream(cleaned.getBytes('UTF-8')), buff)
      cleaned = buff.toString('UTF-8')
    }
    catch (UnsupportedEncodingException e) {
      // cannot happen as every JVM must support UTF-8, see also class javadoc for java.nio.charset.Charset
    }

    if (StringUtils.isEmpty(cleaned)) {
      return ''
    }

    // strip the header/body stuff
    int startPos = cleaned.indexOf('<body>' + LINE_SEP) + 6 + LINE_SEP.length()
    int endPos = cleaned.indexOf(LINE_SEP + '</body>')
    return cleaned.substring(startPos, endPos)
  }

  /**
   * Converts Javadoc inline tags to HTML.
   */
  private static String decodeJavadocTags(@Nullable final String description) {
    if (StringUtils.isEmpty(description)) {
      return ''
    }

    StringBuffer decoded = new StringBuffer(description.length() + 1024)

    Matcher matcher = Pattern.compile('\\{@(\\w+)\\s*([^\\}]*)\\}').matcher(description)
    while (matcher.find()) {
      String tag = matcher.group(1)
      String text = matcher.group(2)
      text = StringUtils.replace(text, '&', '&amp;')
      text = StringUtils.replace(text, '<', '&lt;')
      text = StringUtils.replace(text, '>', '&gt;')
      if ('code' == tag) {
        text = "<code>$text</code>"
      }
      else if ('link' == tag || 'linkplain' == tag || 'value' == tag) {
        String pattern = '(([^#\\.\\s]+\\.)*([^#\\.\\s]+))?' + '(#([^\\(\\s]*)(\\([^\\)]*\\))?\\s*(\\S.*)?)?'
        final int label = 7
        final int clazz = 3
        final int member = 5
        final int args = 6
        Matcher link = Pattern.compile(pattern).matcher(text)
        if (link.matches()) {
          text = link.group(label)
          if (StringUtils.isEmpty(text)) {
            text = link.group(clazz)
            if (StringUtils.isEmpty(text)) {
              text = ''
            }
            if (StringUtils.isNotEmpty(link.group(member))) {
              if (StringUtils.isNotEmpty(text)) {
                text += '.'
              }
              text += link.group(member)
              if (StringUtils.isNotEmpty(link.group(args))) {
                text += '()'
              }
            }
          }
        }
        if (!'linkplain'.equals(tag)) {
          text = "<code>$text</code>"
        }
      }
      matcher.appendReplacement(decoded, (text != null) ? matcher.quoteReplacement(text) : '')
    }
    matcher.appendTail(decoded)

    return decoded.toString()
  }
}
