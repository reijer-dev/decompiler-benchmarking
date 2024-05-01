#!/bin/bash
# wrapper script; the true magic is done somewhere else
#
# (c) 2024 Jaap van den Bos
#
# run-snowman-online.sh <binary> <target-for-decompiled-file>
myFullName=$(readlink -f "$0")
myLocation=$(dirname "$myFullName")
"$myLocation/run-decompiler-online.sh" "$1" "$2" Hex-Rays -1 301
