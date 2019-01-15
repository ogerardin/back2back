#!/usr/bin/env bash

# Generate the Inno Setup back2back Windows installer using Wine
# Prerequisites: wine, wget, unrar

EXTRACTDIR=../../../target

pushd ${EXTRACTDIR}
#Get latest version of Inno Setup
wget "http://www.jrsoftware.org/download.php/is-unicode.exe" -O is-unicode.exe

#Get and extract Inno Unpacker
wget "https://downloads.sourceforge.net/project/innounp/innounp/innounp%200.47/innounp047.rar?r=https%3A%2F%2Fsourceforge.net%2Fprojects%2Finnounp%2Ffiles%2Flatest%2Fdownload&ts=1547509813" -O innounp.rar
unrar x -o+ innounp.rar

#Unpack Inno Setup installer (using Wine)
wine innounp.exe -x -y is-unicode.exe

popd
#Invoke Inno Setup command line compiler (using Wine)
wine ${EXTRACTDIR}/\{app\}/ISCC.exe install.iss
