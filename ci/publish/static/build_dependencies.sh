#!/usr/bin/env bash

# Set up path as temporary working directory
mkdir -p $DEPS_PATH

# Set up shared dependencies:
if [[ $DEBUG -eq 1 ]]; then
    source ci/publish/static/make_shared_dependencies.sh
else
    source ci/publish/static/make_shared_dependencies.sh > /dev/null
fi
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$(dirname $(find $DEPS_PATH -type f -name 'libprotoc*' | grep protobuf | head -n 1)):$DEPS_PATH/lib

# Although .so/.dylib building is explicitly turned off for most libraries, sometimes
# they still get created. So, remove them just to make sure they don't
# interfere, or otherwise we might get libmxnet.so that is not self-contained.
# For CUDA, since we cannot redistribute the shared objects or perform static linking,
# we DO want to keep the shared objects around, hence performing deletion before cuda setup.
set +e
find $DEPS_PATH/{lib,lib64} -maxdepth 1 -type f -name '*.so' -or -name '*.so.*' -or -name '*.dylib' | grep -v 'libproto' | grep -v 'mkl' | grep -v 'iomp' | xargs rm
set -e

if [[ $PLATFORM == 'linux' ]]; then

    if [[ $VARIANT == cu* ]]; then
        # download and install cuda and cudnn, and set paths
        if [[ $DEBUG -eq 1 ]]; then
            source ci/publish/static/setup_gpu_build_tools.sh
        else
            source ci/publish/static/setup_gpu_build_tools.sh > /dev/null
        fi
    fi
fi

set +e
