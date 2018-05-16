#!/usr/bin/env bash

# -w flag permanently adds the plist to the Launch Daemon
launchctl unload -w /Library/LaunchDaemons/org.ogerardin.back2back.plist

rm /Library/LaunchDaemons/org.ogerardin.back2back.plist
