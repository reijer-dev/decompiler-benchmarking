#!/bin/bash
# the perfect decompiler - linux edition; (c) 2023 Jaap van den Bos
#
# adapted from: theperfectdecompiler.bat

# add some colours
Color_Off='\033[0m'       # Text Reset
BWhite='\033[1;37m'       # White
On_Red='\033[41m'         # Red
On_Green='\033[42m'       # Green

# whoami?
echo -e "$On_Green The Perfect Decompiler - Linux edition$Color_Off"

# help
function printhelp {
	echo "  Usage: theperfectdecompiler.sh <binary_to_be_'decompiled'> <target>"
}

# check for two parameters
if [ "$1" == "" ]; then
    echo -e "$BWhite$On_Red Error:$Color_Off missing parameter 1, binary source"
    printhelp
    exit 1
fi
if [ "$2" == "" ]; then
    echo -e "$BWhite$On_Red Error:$Color_Off missing parameter 2, destination for decompiled binary"
    printhelp
    exit 2
fi

# check whether binary exists
if [ -f "$1" ] ; then
	:
else
    echo -e "$BWhite$On_Red Error:$Color_Off '$1' does not exist"
	exit 3
fi

# try to determine c-source
C_source=${1%/*}/source.c
if [ -f "$C_source" ] ; then
	:
else
    echo -e "$BWhite$On_Red Error:$Color_Off '$C_source' does not exist"
	exit 4
fi

# copy source to dest
cp "$C_source" "$2" --force

# report being done
echo "Copied original source for binary successfully."


