#!/usr/bin/env bash
if [[ $PLATFORM == 'linux' ]]; then
    set -x
fi

mkdir -p $HOME/.m2
cp settings.xml $HOME/.m2

cd mxnet-build
ln -s ../deps deps

# patch scala build logic
cp ../patch/init_pom.xml scala-package/init/pom.xml
cp ../patch/Makefile Makefile
rm -r scala-package/core/scripts && cp -r ../patch/scala-package_core_scripts scala-package/core/scripts
cp ../patch/scala-package_assembly_linux-x86_64-cpu_pom.xml scala-package/assembly/linux-x86_64-cpu/pom.xml
cp ../patch/scala-package_assembly_linux-x86_64-gpu_pom.xml scala-package/assembly/linux-x86_64-gpu/pom.xml
cp ../patch/scala-package_assembly_linux-x86_64-cpu_assembly.xml scala-package/assembly/linux-x86_64-cpu/src/main/assembly/assembly.xml
cp ../patch/scala-package_assembly_linux-x86_64-gpu_assembly.xml scala-package/assembly/linux-x86_64-gpu/src/main/assembly/assembly.xml
cp ../patch/scala-package_init_pom.xml scala-package/init/pom.xml

$MAKE DEPS_PATH=$DEPS_PATH scalapkg || exit 1;
$MAKE DEPS_PATH=$DEPS_PATH scalaunittest || echo;

# @szha: this is a workaround for travis-ci#6522
set +ex
