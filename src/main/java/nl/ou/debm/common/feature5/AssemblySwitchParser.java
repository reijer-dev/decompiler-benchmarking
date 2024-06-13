package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.feature3.AsmType;

import java.util.*;
import java.util.regex.Pattern;

import static nl.ou.debm.common.feature5.SwitchInfo.SwitchImplementationType.*;

public class AssemblySwitchParser {

    private static Pattern _asciiStringPattern = Pattern.compile("\\.asciz\\s+\"(.+?)\"");
    private static String _jumpTableHint = "ljti";

    public void setIndirectionInfo(Map<Long, SwitchInfo> switchMap, IAssessor.CodeInfo ci) {
        /*if(switchMap.isEmpty())
            return;*/
        //These are needed for the assembly helper, but we don't do anything special with these
        var jumpTableNames = new HashSet<String>();

        String jumpTableCandidate = null;

        //Build mapping between string names and values
        //Gather names of jump tables
        var stringValues = new HashMap<String, String>();
        String currentStringLabel = null;
        for (var line : ci.assemblyInfo.lines) {
            if (line.type == AsmType.StringLabel) {
                currentStringLabel = line.value;
                continue;
            }
            var asciiStringMatcher = _asciiStringPattern.matcher(line.line);
            if (currentStringLabel != null && asciiStringMatcher.find()) {
                if (!stringValues.containsKey(currentStringLabel))
                    stringValues.put(currentStringLabel, asciiStringMatcher.group(1));
                continue;
            }

            if(line.type == AsmType.OtherLabel && line.value.startsWith(_jumpTableHint)) {
                jumpTableCandidate = line.value;
                continue;
            }

            if(jumpTableCandidate != null){
                if(line.line.startsWith(".long"))
                    jumpTableNames.add(jumpTableCandidate);
                jumpTableCandidate = null;
                continue;
            }
        }

        String valueInRcx = null;
        SwitchInfo currentSwitch = null;
        String argumentRegister = ci.compilerConfig.architecture == EArchitecture.X64ARCH ? "%rcx" : "%eax";
        String indirectionsPrefix = EFeaturePrefix.INDIRECTIONSFEATURE + ">>";
        var jumpTableNameSeen = false;

        for (var line : ci.assemblyInfo.lines) {
            if(line.type == AsmType.Other)
                continue;
            
            //We want to find the code marker before the switch statement.
            //First, we remember the string in the argument register
            if (line.type == AsmType.LoadStringInRegister && line.value2.equals(argumentRegister)) {
                valueInRcx = Arrays.stream(line.value.split("\\(")).findFirst().orElse(null);
                continue;
            }

            //If there is a printf, check if it is a switch start. If so, set current switch
            if (line.type == AsmType.Call && line.value.toLowerCase().startsWith("__cm_printf")) {
                assert valueInRcx != null;
                var printfArgument = stringValues.getOrDefault(valueInRcx, null);
                if (printfArgument != null && printfArgument.contains(CodeMarker.STRCODEMARKERGUID) && printfArgument.contains(indirectionsPrefix)) {
                    var marker = new IndirectionsCodeMarker(printfArgument);
                    currentSwitch = switchMap.getOrDefault(marker.lngGetSwitchID(), null);
                }
                continue;
            }

            //We check if a jump table name is used between the code marker and the actual jump
            if(currentSwitch != null && line.type != AsmType.Jump && line.line.contains(_jumpTableHint)){
                jumpTableNameSeen = true;
            }

            //We consider every unconditional first jump after a switch code marker as THE switch jump.
            //Indirection check is simple: if it starts with the dereferencing operator. If not, it is most likely a label name
            if(currentSwitch != null && line.type == AsmType.Jump){
                if(line.value.startsWith("*"))
                    currentSwitch.setImplementationType(jumpTableNameSeen ? JUMP_TABLE : DIRECT_CALCULATED_JUMP);
                else
                    currentSwitch.setImplementationType(IF_STATEMENTS);
                currentSwitch = null;
                jumpTableNameSeen = false;
            }
        }
    }
}
