#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -ex

# Setup Environment Variables
# MAVEN_PUBLISH_OS_TYPE: linux-x86_64-cpu|linux-x86_64-gpu|osx-x86_64-cpu
# export MAVEN_PUBLISH_OS_TYPE=linux-x86_64-cpu

bash scala-package/dev/compile-mxnet-backend.sh $MAVEN_PUBLISH_OS_TYPE ./

# Scala steps to deploy
make scalapkg CI=1
make scalaunittest CI=1
make scalaintegrationtest CI=1

# Compile tests for discovery later
export GPG_TTY=$(tty)
make scalatestcompile CI=1
# make scalainstall CI=1
make scaladeploylocal CI=1
