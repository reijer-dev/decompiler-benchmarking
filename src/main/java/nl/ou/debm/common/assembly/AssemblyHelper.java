package nl.ou.debm.common.assembly;

import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.feature3.AsmLineInfo;
import nl.ou.debm.common.feature3.AsmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static nl.ou.debm.common.EArchitecture.X64ARCH;
import static nl.ou.debm.common.EArchitecture.X86ARCH;

public class AssemblyHelper {

    private static final ArrayList<String> _nonVolatileRegisters = new ArrayList<>() {
        {
            add("%r12");
        }

        {
            add("%r13");
        }

        {
            add("%r14");
        }

        {
            add("%r15");
        }

        {
            add("%rdi");
        }

        {
            add("%rsi");
        }

        {
            add("%rbx");
        }

        {
            add("%rbp");
        }

        {
            add("%rsp");
        }
    };

    private static final ArrayList<String> _x64ArgumentRegisters = new ArrayList<>() {
        {
            add("%rcx");
        }

        {
            add("%rdx");
        }

        {
            add("%r8");
        }

        {
            add("%r9");
        }
    };

    private static final Pattern _functionLabelPattern = Pattern.compile("\\s*_?(.+_function_.+):");
    private static final Pattern _labelPattern = Pattern.compile("^(.+):$");
    private static final Pattern _stringLabelPattern = Pattern.compile("^(.+\\.str.*):");
    private static final Pattern _pattern = Pattern.compile("(\\S+)\\s+(\\S+),?\\s*(\\S+)?");

    public static String preprocess(String line) {
        if (line.contains("#")) {

            var before = line.substring(0, line.indexOf("#"));
            var quoteCount = before.length() - before.replace("\"", "").length();
            if(quoteCount % 2 == 0)
                line = before;
        }
        line = line.trim();
        if (!line.startsWith(".asciz"))
            line = line.toLowerCase();
        line = line.replaceAll("\\s+", " ");
        return line;
    }

    public static AsmLineInfo getLineInfo(String line, ArrayList<String> homedRegisters, HashMap<String, String> registerMap, EArchitecture arch) {
        if (arch == X64ARCH && line.startsWith(".seh_"))
            return new AsmLineInfo(line, AsmType.Pseudo);

        if (line.contains(":")) {
            var labelMatcher = _functionLabelPattern.matcher(line);
            if (labelMatcher.find())
                return new AsmLineInfo(line, AsmType.FunctionLabel, labelMatcher.group(1));
            var stringLabelMatcher = _stringLabelPattern.matcher(line);
            if (stringLabelMatcher.find())
                return new AsmLineInfo(line, AsmType.StringLabel, stringLabelMatcher.group(1));

            var otherLabelMatcher = _labelPattern.matcher(line);
            if (otherLabelMatcher.find())
                return new AsmLineInfo(line, AsmType.OtherLabel, otherLabelMatcher.group(1));
        }

        var operationAffix = switch (arch) {
            case X64ARCH -> "q";
            case X86ARCH -> "l";
        };

        var stackPointer = switch (arch) {
            case X64ARCH -> "%rsp";
            case X86ARCH -> "%esp";
        };

        if (line.contains(" ")) {
            var matcher = _pattern.matcher(line);
            if (!matcher.find())
                return new AsmLineInfo(line, AsmType.Other);
            var operation = matcher.group(1);
            var op1 = matcher.group(2).replace(",", "");
            var op2 = matcher.group(3);
            if (op2 == null) {
                if (_nonVolatileRegisters.contains(op1) || getEquivalentRegisters(op1, registerMap).stream().anyMatch(_nonVolatileRegisters::contains)) {
                    if (operation.equals("push" + operationAffix))
                        return new AsmLineInfo(line, AsmType.NonVolatileRegisterSave, op1);
                    if (operation.equals("pop" + operationAffix))
                        return new AsmLineInfo(line, AsmType.NonVolatileRegisterLoad, op1);
                }
                if (operation.equals("call" + operationAffix))
                    return new AsmLineInfo(line, AsmType.Call, op1);
                if (arch == X86ARCH && operation.equals("pushl") && op1.equals("%ebp"))
                    return new AsmLineInfo(line, AsmType.SaveBasePointer);
                if (operation.equals("jmp"))
                    return new AsmLineInfo(line, AsmType.Jump, op1);
                if (operation.equals("jmp" + operationAffix))
                    return new AsmLineInfo(line, AsmType.Jump, op1);
            } else {
                if (operation.equals("sub" + operationAffix) && op2.equals(stackPointer))
                    return new AsmLineInfo(line, AsmType.StackAllocation, op1.replace("$", ""));
                else if (operation.equals("add" + operationAffix) && op2.equals(stackPointer))
                    return new AsmLineInfo(line, AsmType.StackDeallocation, op1);
                if (arch == X86ARCH && operation.equals("movl") && op1.equals(stackPointer) && op2.equals("%ebp"))
                    return new AsmLineInfo(line, AsmType.BaseToStackPointer);
                if (operation.equals("mov" + operationAffix) && op1.startsWith("%") && (op2.startsWith("%") || op2.startsWith("(%")))
                    return new AsmLineInfo(line, AsmType.RegisterMove, op2, op1);
                if (arch == X64ARCH && operation.equals("movq") && (_x64ArgumentRegisters.contains(op1) || getEquivalentRegisters(op1, registerMap).stream().anyMatch(_x64ArgumentRegisters::contains)) && !homedRegisters.contains(op2))
                    return new AsmLineInfo(line, AsmType.RegisterHoming, op1);
                if (operation.equals("lea" + operationAffix) && op1.contains("str") && op2.startsWith("%"))
                    return new AsmLineInfo(line, AsmType.LoadStringInRegister, op1, op2);
                if (operation.equals("mov" + operationAffix) && op1.contains("str") && op2.startsWith("(%"))
                    return new AsmLineInfo(line, AsmType.LoadStringInRegister, op1, op2);
            }
        } else {
            if (line.equals("ret" + operationAffix))
                return new AsmLineInfo(line, AsmType.Return);
        }

        return new AsmLineInfo(line, AsmType.Other);
    }

    private static List<String> getEquivalentRegisters(String register, HashMap<String, String> map) {
        var result = new ArrayList<String>();
        var seenRegisters = new HashSet<String>();
        while (map.containsKey(register)) {
            result.add(map.get(register));
            register = map.get(register);
            if(seenRegisters.contains(register))
                break;
            seenRegisters.add(register);
        }
        return result;
    }

}

