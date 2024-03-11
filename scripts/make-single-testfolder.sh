#!/bin/bash
#
# make-single-testfolder.sh
# =========================
#
# usage: make-single-testfolder [n1] [n2] [base folder]
#
# purpose: create a folder that contains one test's binaries, llvm's and c-source AND make sure
# all decompilers are used to make a set of decompiled c-codes for every binary in that source
#
# n1 is the container number - it defaults to 0
# n2 is the test number in the container - it defaults to 0
# <base folder> is the container folder - it defaults to /home/jaap/VAF/containers
#
# (c) 2024 Jaap van den Bos
#

DEFAULT_BASE_FOLDER=/home/jaap/VAF/containers/
SCRIPT_FOLDER=/home/jaap/VAF/decompiler-benchmarking/scripts

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
echo -e "$BWhite$On_Green**  make-single-testfolder.sh  **$Color_Off"

# error printing function
function printerror {
	echo -e "$BWhite$On_Red error: $Color_Off $1"
}
function multinumberstring {
  len=${#1}
  while [ $len -lt $2 ]; do
    printf "0"
    len=$((len+1))
  done
  echo $1
}
function addpathseparator {
  last=$(echo -n "$1" | tail -c 1)
  if [ "$last" == "/" ]; then
    echo "$1"
  else
    echo "$1/"
  fi
}
function getshortscriptname {
  part=$1
  while [ "$part" != "" ] ; do
    if [ "${part:0:5}" == "/run-" ] ; then
      part1=${part:5}
      break
    fi
    part=${part:1}
  done
  if [ "$part1" != "" ] ; then
    while [ "${part1:0:3}" != ".sh" ] ; do
      output=$output${part1:0:1}
      part1=${part1:1}
    done
    echo $output
  fi
}

# first parameter given?
if [ "$1" == "" ] ; then
  SOURCE_CONTAINER=0;
else
  SOURCE_CONTAINER=$1
fi
SOURCE_CONTAINER=$(multinumberstring $SOURCE_CONTAINER 3)
# second parameter given?
if [ "$2" == "" ] ; then
  SOURCE_TEST=0;
else
  SOURCE_TEST=$2
fi
SOURCE_TEST=$(multinumberstring $SOURCE_TEST 3)
# third parameter given?>
if [ "$3" = "" ] ; then
  CONTAINERS_BASE=$DEFAULT_BASE_FOLDER
else
  CONTAINERS_BASE=$3
fi
CONTAINERS_BASE=$(addpathseparator $CONTAINERS_BASE)

# compose full source and test existence
SUB_PATH=container_$SOURCE_CONTAINER/test_$SOURCE_TEST/
FULL_SOURCE_FOLDER=$CONTAINERS_BASE$SUB_PATH
if [ -d "$FULL_SOURCE_FOLDER" ] ; then
  echo -e "Sources taken from: $FULL_SOURCE_FOLDER"
else
  printerror "$FULL_SOURCE_FOLDER does not exist."
  exit 1
fi

# make test set filename
TESTSETFOLDERPREFIX=testset_
max=0;
for file in $(ls -d $CONTAINERS_BASE$TESTSETFOLDERPREFIX[0-9][0-9][0-9]/ 2>/dev/null); do
  last=$(echo -n "$file" | tail -c 4)
  code=${last:0:3}
  if [ "$code" -gt "$max" ] ; then
    max=$code
  fi
done
newindex=$((10#$max))
newindex=$((newindex+1))
newindex=$(multinumberstring $newindex 3)
TESTSUBFOLDER=$TESTSETFOLDERPREFIX$newindex
TESTFOLDER=$CONTAINERS_BASE$TESTSUBFOLDER
mkdir "$TESTFOLDER"
if [ -d "$TESTFOLDER" ] ; then
  echo -e "New test set folder: $TESTFOLDER"
else
  printerror "$TESTFOLDER could not be created."
  exit 0
fi

# copy c-source. binaries & llvm's
echo -n "Copying sources..."
cp $FULL_SOURCE_FOLDER*.c $TESTFOLDER
cp $FULL_SOURCE_FOLDER*.exe $TESTFOLDER
cp $FULL_SOURCE_FOLDER*.ll* $TESTFOLDER
echo "done."

# loop over all compiler scripts and
# use for all sources
for script in $(ls $SCRIPT_FOLDER/run*.sh); do
  shortscriptname=$(getshortscriptname $script)
  if [ "$shortscriptname" != "decompiler-online" ] ; then
    for source in $(ls $TESTFOLDER/*.exe) ; do
      decompilerdest=$source---$shortscriptname.c
      echo -e "$Green$On_Yellow script: $Black$On_Yellow$script$Color_Off"
      echo -e "$Green binary: $White$source$Color_Off"
      echo -e "$Green dest:   $White$decompilerdest$Color_Off"
      $script $source $decompilerdest
    done
  fi
done
