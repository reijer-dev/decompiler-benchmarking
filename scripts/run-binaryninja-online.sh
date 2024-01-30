#!/bin/bash
# wrapper script; the true magic is done somewhere else
#
# (c) 2024 Jaap van den Bos
#
# run-binaryninja-online.sh <binary> <target-for-decompiled-file>
./run-decompiler-online.sh "$1" "$2" BinaryNinja -1 61s
