package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.assembly.AssemblyHelper;
import nl.ou.debm.common.feature3.AsmType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class AssemblySwitchParser {

    private static Pattern _asciiStringPattern = Pattern.compile("\\.asciz\\s+\"(.+?)\"");

    public void setIndirectionInfo(HashMap<Long, SwitchInfo> switchMap, IAssessor.CodeInfo ci) {
        if(switchMap.isEmpty())
            return;
        List<String> asmLines = null;
        try {
            asmLines = Files.readAllLines(Paths.get(ci.strAssemblyFilename))
                    .stream()
                    .map(AssemblyHelper::preprocess)
                    .filter(x -> !x.isEmpty())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //These are needed for the assembly helper, but we don't do anything special with these
        var homedRegisters = new ArrayList<String>();
        var registerMap = new HashMap<String, String>();

        //Build mapping between string names and values
        var stringValues = new HashMap<String, String>();
        String currentStringLabel = null;
        for (var line : asmLines) {
            var info = AssemblyHelper.getLineInfo(line, homedRegisters, registerMap, ci.compilerConfig.architecture);
            if (info.type == AsmType.StringLabel) {
                currentStringLabel = info.value;
                continue;
            }
            var asciiStringMatcher = _asciiStringPattern.matcher(line);
            if (currentStringLabel != null && asciiStringMatcher.find()) {
                if (!stringValues.containsKey(currentStringLabel))
                    stringValues.put(currentStringLabel, asciiStringMatcher.group(1));
                continue;
            }
        }

        String valueInRcx = null;
        SwitchInfo currentSwitch = null;
        String argumentRegister = ci.compilerConfig.architecture == EArchitecture.X64ARCH ? "%rcx" : "%eax";
        String indirectionsPrefix = EFeaturePrefix.INDIRECTIONSFEATURE + ">>";

        for (var line : asmLines) {
            var info = AssemblyHelper.getLineInfo(line, homedRegisters, registerMap, ci.compilerConfig.architecture);

            //We want to find the code marker before the switch statement.
            //First, we remember the string in the argument register
            if (info.type == AsmType.LoadStringInRegister && info.value2.equals(argumentRegister)) {
                valueInRcx = Arrays.stream(info.value.split("\\(")).findFirst().orElse(null);
                continue;
            }

            //If there is a printf, check if it is a switch start. If so, set current switch
            if (info.type == AsmType.Call && info.value.toLowerCase().startsWith("__cm_printf")) {
                assert valueInRcx != null;
                var printfArgument = stringValues.getOrDefault(valueInRcx, null);
                if (printfArgument != null && printfArgument.contains(CodeMarker.STRCODEMARKERGUID) && printfArgument.contains(indirectionsPrefix)) {
                    var marker = new IndirectionsCodeMarker(printfArgument);
                    currentSwitch = switchMap.getOrDefault(marker.lngGetSwitchID(), null);
                }
                continue;
            }

            //We consider every unconditional first jump after a switch code marker as THE switch jump.
            //Indirection check is simple: if it starts with the dereferencing operator. If not, it is most likely a label name
            if(currentSwitch != null && info.type == AsmType.Jump){
                currentSwitch.setImplementedAsIndirection(info.value.startsWith("*"));
                currentSwitch = null;
            }
        }
    }
}
