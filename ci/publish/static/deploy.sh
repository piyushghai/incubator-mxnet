#!/usr/bin/env bash

cd mxnet-build
if [[ (! -z $TRAVIS_TAG) || ( $TRAVIS_EVENT_TYPE == 'cron' ) ]]; then
    $MAKE DEPS_PATH=$DEPS_PATH scaladeploy;
fi
