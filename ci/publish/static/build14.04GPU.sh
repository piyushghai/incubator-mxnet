#!/usr/bin/env bash
# Build on Ubuntu 14.04 LTS for LINUX GPU
source ci/publish/static/ubuntu_build_base.sh
update-alternatives \
 --install /usr/bin/gcc gcc /usr/bin/gcc-4.8 50 \
 --slave /usr/bin/g++ g++ /usr/bin/g++-4.8
update-alternatives --config gcc
source ci/publish/static/set_environment.sh $mxnet_variant $target
source ci/publish/static/build_dependencies.sh
# Build Backend Dependencies
source ci/publish/static/build_modules.sh
source ci/publish/static/build_lib.sh
# Without preset environment, there will be CUDA module missing
cp -r deps mxnet-build/deps
cp -r config mxnet-build/config
# Python
# cd python
# pip install --user -e .
## Add this line into make file...
LIB_DEP+=$(ROOTDIR)/deps/lib/libopencv_*.a
