#!/usr/bin/env bash

B2BHOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PLIST_FILE=/Library/LaunchDaemons/org.ogerardin.back2back.plist

launchctl stop org.ogerardin.back2back
launchctl unload -w ${PLIST_FILE}

rm ${PLIST_FILE}
