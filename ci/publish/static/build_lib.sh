#!/usr/bin/env bash
if [[ $PLATFORM == 'linux' ]]; then
    set -x
fi

MXNET_COMMIT=$(git rev-parse HEAD)

>&2 echo "Now building mxnet..."
cp $MAKE_CONFIG config.mk

if [[ ! -f $HOME/.mxnet/mxnet/$MXNET_COMMIT/libmxnet.a ]] || [[ ! -f $HOME/.mxnet/mxnet/$MXNET_COMMIT/libmxnet.so ]]; then
    $MAKE DEPS_PATH=$DEPS_PATH || exit 1;
    mkdir -p $HOME/.mxnet/mxnet/$MXNET_COMMIT
    cp lib/libmxnet.a $HOME/.mxnet/mxnet/$MXNET_COMMIT/
    cp lib/libmxnet.so $HOME/.mxnet/mxnet/$MXNET_COMMIT/
else
    mkdir -p lib
    cp $HOME/.mxnet/mxnet/$MXNET_COMMIT/libmxnet.a lib
    cp $HOME/.mxnet/mxnet/$MXNET_COMMIT/libmxnet.so lib
fi

# copy lapack dependencies
if [[ $PLATFORM == 'linux' ]]; then
    cp -L /usr/lib/gcc/x86_64-linux-gnu/4.8/libgfortran.so lib/libgfortran.so.3
    cp -L /usr/lib/x86_64-linux-gnu/libquadmath.so.0 lib/libquadmath.so.0
fi

if [[ $VARIANT == *mkl ]]; then
    >&2 echo "Copying MKL license."
    cp 3rdparty/mkldnn/LICENSE ./MKLML_LICENSE
    rm lib/libmkldnn.{so,dylib}
    rm lib/libmkldnn.0.*.dylib
    rm lib/libmkldnn.so.0.*
fi

# Print the linked objects on libmxnet.so
>&2 echo "Checking linked objects on libmxnet.so..."
if [[ ! -z $(command -v readelf) ]]; then
    readelf -d lib/libmxnet.so
    strip --strip-unneeded lib/libmxnet.so
elif [[ ! -z $(command -v otool) ]]; then
    otool -L lib/libmxnet.so
    strip -u -r -x lib/libmxnet.so
else
    >&2 echo "Not available"
fi

echo "Libraries in lib path"
ls -al lib

ln -s staticdeps/ deps
