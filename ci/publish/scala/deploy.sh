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


if [[ $MAVEN_PUBLISH_OS_TYPE == "linux-x86_64-cpu" ]];
then
    MAKE_FLAGS="USE_BLAS=openblas USE_DIST_KVSTORE=1 ENABLE_TESTCOVERAGE=1"
elif [[ $MAVEN_PUBLISH_OS_TYPE == "linux-x86_64-gpu" ]]
then
    MAKE_FLAGS="USE_OPENCV=1 USE_BLAS=openblas USE_CUDA=1 USE_CUDA_PATH=/usr/local/cuda USE_CUDNN=1 SCALA_ON_GPU=1 SCALA_TEST_ON_GPU=1 USE_DIST_KVSTORE=1 ENABLE_TESTCOVERAGE=1"
fi

# Run python to configure keys
python3 ci/publish/scala/buildkey.py

# Updating cache
mkdir -p ~/.gnupg
echo "default-cache-ttl 14400" > ~/.gnupg/gpg-agent.conf
echo "max-cache-ttl 14400" >> ~/.gnupg/gpg-agent.conf
echo "allow-loopback-pinentry" >> ~/.gnupg/gpg-agent.conf
echo "pinentry-mode loopback" >> ~/.gnupg/gpg-agent.conf
export GPG_TTY=$(tty)

cd scala-package
VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec)
cd ..

# echo "\n\n$VERSION\n" | make scalarelease-dryrun $MAKE_FLAGS
make scaladeploy $MAKE_FLAGS CI=1

# Clear all password .xml files, gpg key files, and all imported gpg secret keys
rm -rf ~/.m2/*.xml ~/.m2/key.asc
