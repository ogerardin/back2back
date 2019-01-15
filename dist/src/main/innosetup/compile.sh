#!/usr/bin/env bash

EXTRACTDIR=../../../target

pushd ${EXTRACTDIR}
wget "http://www.jrsoftware.org/download.php/is-unicode.exe" -O is-unicode.exe
wget "https://downloads.sourceforge.net/project/innounp/innounp/innounp%200.47/innounp047.rar?r=https%3A%2F%2Fsourceforge.net%2Fprojects%2Finnounp%2Ffiles%2Flatest%2Fdownload&ts=1547509813" -O innounp.rar
unrar x -o+ innounp.rar
wine innounp.exe -x -y is-unicode.exe

popd
wine ${EXTRACTDIR}/\{app\}/ISCC.exe install.iss
