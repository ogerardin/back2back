#!/usr/bin/env bash
#
# Generate the back2back Windows installer using Inno Setup compiler and Wine (for Linux and macOS)
# Prerequisites: wine, wget, unrar
#


if ! wine --version ; then
    echo "Missing prerequisite: wine" 2>&1
    exit
fi

if ! unrar >/dev/null ; then
    echo "Missing prerequisite: unrar" 2>&1
    exit
fi

if ! wget --version >/dev/null ; then
    echo "Missing prerequisite: wget" 2>&1
    exit
fi


EXTRACTDIR=../../../target

pushd ${EXTRACTDIR}

#Get the current directory as seen from Wine
WINE_PWD=$(wine cmd /c cd | tr -d '\r\n')

#Get and extract Inno Unpacker
wget "https://downloads.sourceforge.net/project/innounp/innounp/innounp%200.47/innounp047.rar?r=https%3A%2F%2Fsourceforge.net%2Fprojects%2Finnounp%2Ffiles%2Flatest%2Fdownload&ts=1547509813" -O innounp.rar
unrar x -o+ innounp.rar

#Get and unpack latest version of Inno Setup
wget "http://www.jrsoftware.org/download.php/is-unicode.exe" -O is-unicode.exe
wine innounp.exe -x -y is-unicode.exe
rm -r \{tmp\}
mv \{app\} inno-setup
INNO_SETUP_HOME=${PWD}/inno-setup
echo INNO_SETUP_HOME=${INNO_SETUP_HOME}

#Get and unpack Inno Download Plugin
wget "https://bitbucket.org/mitrich_k/inno-download-plugin/downloads/idpsetup-1.5.1.exe" -O idpsetup.exe
wine innounp.exe -x -y idpsetup.exe
rm -r \{tmp\}
mv \{app\} inno-download-plugin
INNO_DOWNLOAD_PLUGIN_HOME_WINE=$WINE_PWD\\inno-download-plugin
echo INNO_DOWNLOAD_PLUGIN_HOME=${INNO_DOWNLOAD_PLUGIN_HOME_WINE}

#Append Inno Download plugin directory
ISSPPBUILTINS_FILE=${INNO_SETUP_HOME}/ISPPBuiltins.iss
echo ISSPPBUILTINS_FILE=${ISSPPBUILTINS_FILE}
if ! grep -q inno-download-plugin ${ISSPPBUILTINS_FILE} ; then
    echo Updating ${ISSPPBUILTINS_FILE}
    echo \#pragma include __INCLUDE__ + \"\;\" + \"${INNO_DOWNLOAD_PLUGIN_HOME_WINE}\" >> ${ISSPPBUILTINS_FILE}
fi

#Invoke Inno Setup command line compiler (using Wine)
popd
wine ${EXTRACTDIR}/inno-setup/ISCC.exe install.iss
