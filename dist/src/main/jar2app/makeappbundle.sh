#!/usr/bin/env bash
#
# Prerequisites: wget, unzip, python
#

which python    || { echo "Missing prerequisite: python" 2>&1 ; exit; }
which wget      || { echo "Missing prerequisite: wget" 2>&1 ; exit; }
which unzip     || { echo "Missing prerequisite: unzip" 2>&1 ; exit; }


EXTRACTDIR=../../../target

pushd ${EXTRACTDIR}

wget "https://github.com/Jorl17/jar2app/archive/master.zip" -O jar2app.zip
unzip x jar2app.zip
chmod +x jar2app-master/jar2app.py
JAR2APP_HOME=${PWD}/jar2app-master
echo JAR2APP_HOME=${JAR2APP_HOME}

#Invoke Inno Setup command line compiler (using Wine)
popd
${JAR2APP_HOME}/jar2app.py $*
