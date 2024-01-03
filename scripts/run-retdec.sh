#!/bin/bash
#
# run-retdec.sh
#
# retdec wrapper for linux, (c) 2024 Jaap van den Bos
#
# run-retdec <binary> <c-targetfile>

# add colour
Color_Off='\033[0m'       # Text Reset

# Regular Colors
Black='\033[0;30m'        # Black
Red='\033[0;31m'          # Red
Green='\033[0;32m'        # Green
Yellow='\033[0;33m'       # Yellow
Blue='\033[0;34m'         # Blue
Purple='\033[0;35m'       # Purple
Cyan='\033[0;36m'         # Cyan
White='\033[0;37m'        # White

# Bold
BBlack='\033[1;30m'       # Black
BRed='\033[1;31m'         # Red
BGreen='\033[1;32m'       # Green
BYellow='\033[1;33m'      # Yellow
BBlue='\033[1;34m'        # Blue
BPurple='\033[1;35m'      # Purple
BCyan='\033[1;36m'        # Cyan
BWhite='\033[1;37m'       # White

# Background
On_Black='\033[40m'       # Black
On_Red='\033[41m'         # Red
On_Green='\033[42m'       # Green
On_Yellow='\033[43m'      # Yellow
On_Blue='\033[44m'        # Blue
On_Purple='\033[45m'      # Purple
On_Cyan='\033[46m'        # Cyan
On_White='\033[47m'       # White

# whoami
echo -e "$BWhite$On_Green** retdec wrapper - Linux edition  **$Color_Off"

# find retdec
RETDECPATH=$(which retdec-decompiler)
if [ "$RETDECPATH" == "" ] ; then
	echo -e "$BWhite$On_Red error: $Color_Off'retdec-decompiler' not found in default path"
	exit 1
fi

# check parameters
if [ "$1" == "" ] ; then
	echo -e "$BWhite$On_Red error: $Color_Off parameter missing (binary file name)"
	exit 2
fi
if [ "$2" == "" ] ; then
	echo -e "$BWhite$On_Red error: $Color_Off parameter missing (decompiler output file name)"
	exit 2
fi
# check whether binary exists
if [ -f "$1" ] ; then
	:
else
    echo -e "$BWhite$On_Red error: '$1' does not exist $Color_Off"
	exit 3
fi

##########################################################################
# decompile
echo Preparing...
TEMPDIR=$(mktemp -d)
CURPATH=$PWD
cd $TEMPDIR
cp "$1" $TEMPDIR
FILEONLY=$(basename "$1")
echo Decompiling...
/home/jaap/Documenten/ou_informatica/Retdec/bin/retdec-decompiler $FILEONLY
echo Copying...
cp "$FILEONLY.c" $2
echo Cleaning up...
rm -rf "$TEMPDIR"

