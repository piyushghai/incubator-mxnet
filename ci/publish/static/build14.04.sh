#!/usr/bin/env bash
# Build on Ubuntu 14.04 LTS for LINUX CPU
source scripts/ubuntu_build_base.sh
update-alternatives \
 --install /usr/bin/gcc gcc /usr/bin/gcc-4.8 50 \
 --slave /usr/bin/g++ g++ /usr/bin/g++-4.8
update-alternatives --config gcc
source scripts/set_environment.sh $mxnet_variant $target
source scripts/build_dependencies.sh
# Build Backend Dependencies
source scripts/build_modules.sh
source scripts/build_lib.sh
cp -r deps mxnet-build/deps
cp -r config mxnet-build/config
# Python
cd mxnet-build
cd python
pip install --user -e .
# Scala
set +x
cd mxnet-build
sudo bash ci/docker/install/ubuntu_scala.sh
## Add this line into make file...
LIB_DEP+=$(ROOTDIR)/deps/lib/libopencv_*.a
make scalapkg
make scalaunittest
make scalaintegrationtest
