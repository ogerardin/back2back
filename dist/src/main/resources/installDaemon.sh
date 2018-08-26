#!/usr/bin/env bash

B2BHOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PLIST_FILE=/Library/LaunchDaemons/org.ogerardin.back2back.plist

sed "s#\${B2BHOME}#${B2BHOME}#g" ${B2BHOME}/launchd/org.ogerardin.back2back.plist > ${PLIST_FILE}

launchctl load -w ${PLIST_FILE}
