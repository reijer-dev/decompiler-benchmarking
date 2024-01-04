#!/bin/bash
#
# run-ghidra.sh  (c) 2023, 2024 Jaap van den Bos
#
# ghidra wrapper for linux, (c) 2024 Jaap van den Bos
#
# use: run-ghidra.sh <binary-to-be-decompiled> <targetfile-for-c-output>


# before use: make sure these settings are correct for your system:
#
MYHOSTNAME="stardrive"                                          # checks against cat /etc/hostname
GHIDRA_PATH=~/Documenten/ou_informatica/ghidra_11.0_PUBLIC      # where one keeps Ghidra
JAVA_DECOMPILE_SOURCEFILE_PATH=$PWD                             # where one keeps Decompile.java
JAVA_COMPILED_STORAGE_PATH=~/.ghidra/.$GHIDRA                   # where Ghidra stashes some information

# other constants
JAVA_DECOMPILE_SOURCEFILE_FILENAME=Decompile.java
HEADLESS_RELATIVE=/support/analyzeHeadless

# beep setup
BEEP=$(which beep)

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
echo -e "$BWhite$On_Green** Ghidra wrapper - Linux edition  **$Color_Off"

# check host
myhost=$(cat /etc/hostname)
if [ "$myhost" != "$HOSTNAME" ] ; then
	echo -e "$BWhite$On_Red Error: $Color_Off make sure you first set the parameters in this script"
	echo "   script location: $PWD"
	echo "   script name: $0"
	exit 1
fi

# find ghidra
HEADLESS="$GHIDRA_PATH$HEADLESS_RELATIVE"
if [ -f "$HEADLESS" ] ; then
	:
else
	echo -e "$BWhite$On_Red error: $Color_Off could not find: $HEADLESS"
	exit 2
fi

# check parameters
if [ "$1" == "" ] ; then
	echo -e "$BWhite$On_Red error: $Color_Off parameter missing (binary file name)"
	exit 3
fi
if [ "$2" == "" ] ; then
	echo -e "$BWhite$On_Red error: $Color_Off parameter missing (decompiler output file name)"
	exit 4
fi

# check whether binary exists
if [ -f "$1" ] ; then
	:
else
    echo -e "$BWhite$On_Red error: $Color_Off'$1' does not exist "
	exit 5
fi

# actively force recompilation of Java decompilation script
if [ -d $JAVA_COMPILED_STORAGE_PATH ] ; then
	CURFOL=$PWD
	echo -e "$Green Deleting all $JAVA_DECOMPILE_SOURCEFILE_FILENAME.class files... $Color_Off"
	cd $JAVA_COMPILED_STORAGE_PATH
	shopt -s globstar
	for i in **/*$JAVA_DECOMPILE_SOURCEFILE_FILENAME.class; do
		if [ -f "$PWD/$i" ]; then
			echo "  $PWD/$i"
			rm "$PWD/$i"
		fi
	done
	cd $CURFOL
fi

# do the real work
echo -e "$Green Starting Ghidra...$Color_Off"
cd "$GHIDRA_PATH"
C_TMP_OUTPUT="$PWD/ghidra_decompiled.c"
COMMAND=".$HEADLESS_RELATIVE $PWD tmp_ghidra_project -import $1 -scriptPath $JAVA_DECOMPILE_SOURCEFILE_PATH -postscript $JAVA_DECOMPILE_SOURCEFILE_FILENAME $C_TMP_OUTPUT -deleteProject -analysisTimeoutPerFile 60000 -overwrite"
echo -e "$Green$COMMAND$Color_Off"
#.$HEADLESS_RELATIVE $PWD tmp_ghidra_project -import $1 -scriptPath $JAVA_DECOMPILE_SOURCEFILE_PATH -postscript $JAVA_DECOMPILE_SOURCEFILE_FILENAME $C_TMP_OUTPUT -deleteProject -analysisTimeoutPerFile 600
$COMMAND

# check whether result exists
if [ -f "$C_TMP_OUTPUT" ] ; then
	:
else
	echo -e "$BWhite$On_Red error: $Color_Off'$C_TMP_OUTPUT' does not exist; something went wrong during decompilation "
	$BEEP
	$BEEP
	$BEEP
	exit 6
fi

# move result
echo -e " $Green Moving result to correct location... $Color_Off"
mv "$C_TMP_OUTPUT" "$2"
$BEEP
