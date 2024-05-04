#!/bin/bash
#
# run-decompiler-online.sh
#
# script to invoke online decompiler
#
# this script was written by Jaap van den Bos, but it is based Reijer Klaasse's work
# and it uses his web site
#
# (c) 2024 Jaap van den Bos/Reijer Klaasse
# 
# useage: run-decompiler-online.sh <binary> <c-targetfile> <decompilername> <#attempts> <delay_in_s>
#
# is delay is not specified, a 5 sec delay is used
# if attempts are not specified, the number of attempts is set to 10.
# starting with a negative value will render the loop infinite (until success)
#

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
echo -e "$BWhite$On_Green**  run-decompiler-online.sh - Linux edition  **$Color_Off"

# error printing function
function printerror {
	echo -e "$BWhite$On_Red error: $Color_Off $1"
}

# find curl
CURL=$(which curl)
if [ "$CURL" == "" ] ; then
	printerror "curl not found; check installation"
	exit 1
fi

# check parameters
if [ "$1" == "" ] ; then
	printerror "parameter missing (binary file name)"
	exit 2
fi
if [ "$2" == "" ] ; then
	printerror "parameter missing (decompiler output file name)"
	exit 2
fi
if [ "$3" == "" ] ; then
	printerror "parameter missing (decompiler name)"
	exit 3
fi
# check whether binary exists
if [ -f "$1" ] ; then
	:
else
    	printerror "binary '$1' does not exist"
    	exit 4
fi
ATTEMPTS=$4
if [ "$ATTEMPTS" == "" ] ; then
  ATTEMPTS=10
fi
DELAYINS=$5
if [ "$DELAYINS" == "" ] ; then
  DELAYINS=5
fi
ENDPAUSE=$6
if [ "$ENDPAUSE" == "" ] ; then
    ENDPAUSE=0;
fi

##########################################################################
echo "running curl to invoke decompiler ($3)..."
count=$ATTEMPTS
ndone=0
until [ $count == 0 ]
do
  # try
  now=$(date +%s)
  curl --output "$2" -X POST https://rekl.nl/dogbolt/?decompiler=$3 -F file=@$1
  status=$?
  # test result, break if ok
  if [ -s "$2" ] ; then
    break
  fi
  # no joy; decrease counter and wait
  count=$((count-1))
  ndone=$((ndone+1))
  if [ "$status" == "0" ] ; then
      echo "waiting for next try... ($ndone/$ATTEMPTS, delay = $DELAYINS)"
      if [ -d /home/jaap ]; then
        echo -e -n "$Red"
        progressbar $DELAYINS
        echo -e -n "$Color_Off"
      else
        sleep $DELAYINS
      fi
  fi
done
echo -e -n "$Green"
progressbar $now $ENDPAUSE
echo -e -n "$Color_Off"
