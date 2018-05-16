#!/usr/bin/env bash

cp ${INSTALL_PATH}/org.ogerardin.back2back.plist /Library/LaunchDaemons

# -w flag permanently adds the plist to the Launch Daemon
launchctl load -w /Library/LaunchDaemons/org.ogerardin.back2back.plist
