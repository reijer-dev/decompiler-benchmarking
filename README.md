### Summary
This artifact is the implementation of deb'm as described in the paper, contained in a Linux VM.
It is used to generate all results.
It is created by Jaap van den Bos, Reijer Klaasse and Kesava van Gelder.

### Structure and content
The artifact contains 3 directories:
1) One with the code and executables of deb'm, directly cloned from Github and built with Gradle
2) One containing the examined decompilers that are open-source: RetDec and Ghidra
3) One containing already produced containers, since producing all binaries might take some time.
   The decompilation by RetDec is also done on containers 000 - 003
4) Scripts to
    - Quickly produce an extra container
    - Decompile a container (for example 004)
    - Assess a container to get the scores
    - See the CLI help for the producer and assessor

### Hardware Requirements
RAM: >= 12GB (because the retdec decompiler uses op to 3GB per process, and 4 instances run in parallel)
CPU cores: 4
CPU frequency: >= 1.1 GHz


### Proprietary Software or Data Requirements
No other software or data is needed, neither is a network connection


### Setup
No additional setup is required, because deb'm, JDK, compilers, decompilers are already installed on the VM


### Test Instructions
1. Boot up the VM
2. Open the terminal
3. Type: cd debm
4. To test the producer part, type: ./producer-container.sh (Estimated time: 90 seconds)
   This will produce a full container in the (ad-hoc created) produced_containers directory. Please note that this VM can only produce x64 binaries. For testing purposes, the x86 are present as well in the pre-produced 'containers' directory.
5. To test the decompilation part, type ./decompile-retdec.sh 4
   This will invoke the RetDec decompiler for all non-decompiled 300 binaries in the container_004 directory.
   Estimated time: 90 minutes
6. To test the assessment part, type ./assess-retdec.sh 0
   This will run the deb'm assessor for the decompilation result in container_000, and produce a report.html file
   Estimated time: 30m
Total estimated time: 2 hours


### Replication Instructions
The assess script takes the specified container number, and evaluates all 300 decompiled binaries in it (75 test folders * 4 binaries per folder).
The generated report.html contains all the scores for all metrics used in the paper.
These are sometimes aggregated over architecture or optimization level, to give more overview.
To get this aggregation, assess-(retdec/ghidra).sh can be modified with an extra -ao=a or -ao=o argument.
This -ao argument is documented in the assessor help of deb'm
In the paper, we have specified per plot how the scores are aggregated.

Results from Ghidra can not all be replicated, because, as mentioned in the paper, Ghidra fails to decompile most switches.
Hex-Rays results can not be replicated either, because Ida Pro is licensed.


### Replication with Limited Resources
To limit the amount of resources, there is a decompile-retdec-singlethreaded.sh
This will fit for systems with <= 4GB RAM. Since this will take much longer to decompile, container_000 can be replaced with container_000_pruned to only have 15 test folders instead of 75.
Ghidra results cannot be replicated in an environment with limited resources.


### Examples of Usage
