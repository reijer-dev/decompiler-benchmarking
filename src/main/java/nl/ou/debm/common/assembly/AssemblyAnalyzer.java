package nl.ou.debm.common.assembly;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.feature3.AsmLineInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static nl.ou.debm.common.feature3.AsmType.*;

public class AssemblyAnalyzer {
    public static AssemblyInfo getInfo(IAssessor.CodeInfo ci) {
        Stream<String> asmLines;
        var result = new AssemblyInfo();
        try {
            asmLines = Files.readAllLines(Paths.get(ci.strAssemblyFilename))
                    .stream()
                    .map(AssemblyHelper::preprocess)
                    .filter(x -> !x.isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        asmLines.forEachOrdered(line -> {
            var info = AssemblyHelper.getLineInfo(line, result.homedRegisters, result.registerMap, ci.compilerConfig.architecture);
            if (info.type == FunctionLabel && !result.homedRegisters.isEmpty())
                result.homedRegisters = new ArrayList<>();
            else if (info.type == RegisterHoming) {
                result.homedRegisters.add(info.value);
            } else if (info.type == RegisterMove) {
                result.registerMap.put(info.value2, info.value);
            }
            info.line = line;
            result.lines.add(info);
        });
        return result;
    }
}
