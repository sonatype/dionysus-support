<!--

    Copyright (c) 2018-present Sonatype, Inc. All rights reserved.

    This program is licensed to you under the Apache License Version 2.0,
    and you may not use this file except in compliance with the Apache License Version 2.0.
    You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

    Unless required by applicable law or agreed to in writing,
    software distributed under the Apache License Version 2.0 is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.

-->
# Dionysus Support

![license](https://img.shields.io/github/license/sonatype/dionysus-support.svg)

![maven-central](https://img.shields.io/maven-central/v/org.sonatype.goodies.dionysus/dionysus-support.svg)

Support for using Dionysus [Hugo](https://gohugo.io) theme. 

## Requirements

* [Apache Maven](https://maven.apache.org/) 3.3+ (prefer to use included `mvnw`)
* JDK 7+ (10 is **NOT** supported)
* [NodeJS](https://nodejs.org/en/download/); version 8+
* [Yarn](https://yarnpkg.com/en/docs/install); ensure `yarn global bin` location is on `$PATH` 

## Hugo

[Maven plugin](maven-plugin) includes binary releases of [Hugo](https://gohugo.io), which are licensed under ASL-2.

## Site

To keep things simple for production and development, build will write files into the `src/site/hugo` directory.

### Building

    ./build site_build
    
### Publishing

    ./build site_build && ./build site_publish

or more simply:

    ./build site_deploy
