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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class AssemblySwitchParser {

    private static Pattern _asciiStringPattern = Pattern.compile("\\.asciz\\s+\".+?\"");

    public HashMap<Long, Boolean> getSwitchesInfo(IAssessor.CodeInfo ci) {
        var result = new HashMap<Long, Boolean>();
        List<String> asmLines = null;
        try {
            asmLines = Files.readAllLines(Paths.get(ci.strAssemblyFilename))
                    .stream()
                    .map(AssemblyHelper::Preprocess)
                    .filter(x -> !x.isEmpty())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //These are needed for the assembly helper, but we don't do anything special with these
        var homedRegisters = new ArrayList<String>();
        var registerMap = new HashMap<String, String>();
        var inSwitchBlock = false;

        //Build mapping between string names and values
        var stringValues = new HashMap<String, String>();
        String currentStringLabel = null;
        for (var line : asmLines) {
            var info = ci.compilerConfig.architecture == EArchitecture.X64ARCH ? AssemblyHelper.getX64LineType(line, homedRegisters, registerMap) : AssemblyHelper.getX86LineType(line, registerMap);
            if (info.type == AsmType.OtherLabel)
                currentStringLabel = info.value.contains("str.") ? info.value : null;
            var asciiStringMatcher = _asciiStringPattern.matcher(line);
            if (asciiStringMatcher.find()) {
                if (!stringValues.containsKey(currentStringLabel))
                    stringValues.put(currentStringLabel, asciiStringMatcher.group(1));
            }
        }

        String valueInRcx = null;
        Long currentSwitchId = null;
        for (var line : asmLines) {
            var info = ci.compilerConfig.architecture == EArchitecture.X64ARCH ? AssemblyHelper.getX64LineType(line, homedRegisters, registerMap) : AssemblyHelper.getX86LineType(line, registerMap);
            if (info.type == AsmType.LoadStringInRegister && info.value2.equals("rcx")) {
                valueInRcx = info.value;
                continue;
            }
            if (info.type == AsmType.Call && info.value.startsWith("__CM_printf")) {
                assert valueInRcx != null;
                var printfArgument = stringValues.getOrDefault(valueInRcx, null);
                if (printfArgument != null && printfArgument.contains(CodeMarker.STRCODEMARKERGUID) && printfArgument.contains(">>" + EFeaturePrefix.INDIRECTIONSFEATURE)) {
                    var marker = new IndirectionsCodeMarker(printfArgument);
                    currentSwitchId = marker.lngGetSwitchID();
                }
                continue;
            }
            //if jump, check if it is an indirection. After that, clear current switchId
        }

        return result;
    }
}
