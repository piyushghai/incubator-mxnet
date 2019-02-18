#!/usr/bin/env bash

DIR=$PWD/tools/dependencies

if [[ $PLATFORM == 'linux' ]]; then
    set -x
fi

if [[ ! $PLATFORM == 'darwin' ]]; then
    source $DIR/openblas.sh
fi
source $DIR/libz.sh
source $DIR/libturbojpeg.sh
source $DIR/libpng.sh
source $DIR/libtiff.sh
source $DIR/openssl.sh
source $DIR/curl.sh
source $DIR/eigen.sh
source $DIR/opencv.sh
source $DIR/protobuf.sh
source $DIR/cityhash.sh
source $DIR/zmq.sh
source $DIR/lz4.sh
