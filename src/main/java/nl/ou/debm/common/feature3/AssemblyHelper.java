package nl.ou.debm.common.feature3;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AssemblyHelper {

    private static final ArrayList<String> _x64NonVolatileRegisters = new ArrayList<>(){
        { add("%r12"); }
        { add("%r13"); }
        { add("%r14"); }
        { add("%r15"); }
        { add("%rdi"); }
        { add("%rsi"); }
        { add("%rbx"); }
        { add("%rbp"); }
        { add("%rsp"); }
    };

    private static final Pattern _labelPattern = Pattern.compile("\\s*_?(.+_function_.+):");
    private static final Pattern _pattern = Pattern.compile("(\\S+)\\s+(\\S+),?\\s*(\\S+)?");

    public static String Preprocess(String line){
        if(line.contains("#"))
            return line.substring(0, line.indexOf("#"));
        line = line.trim().toLowerCase().replaceAll("\\s+", " ");
        return line;
    }

    public static AsmLineInfo getX86LineType(String line){
        var labelMatcher = _labelPattern.matcher(line);
        if(labelMatcher.find())
            return new AsmLineInfo(AsmType.Label, labelMatcher.group(1));

        if(line.contains(" ")){
            var matcher = _pattern.matcher(line);
            if(!matcher.find())
                return new AsmLineInfo(AsmType.Other);
            var operation = matcher.group(1);
            var op1 = matcher.group(2).replace(",", "");
            var op2 = matcher.group(3);
            if(op2 == null){
                if(_x64NonVolatileRegisters.contains(op1)) {
                    if (operation.equals("pushl"))
                        return new AsmLineInfo(AsmType.NonVolatileRegisterSave, op1);
                    if (operation.equals("popl"))
                        return new AsmLineInfo(AsmType.NonVolatileRegisterLoad, op1);
                }
                if(operation.equals("calll"))
                    return new AsmLineInfo(AsmType.Call, op1);
                if(operation.equals("pushl") && op1.equals("%ebp"))
                    return new AsmLineInfo(AsmType.SaveBasePointer);
            }else{
                if(operation.equals("subl") && op2.equals("%esp"))
                    return new AsmLineInfo(AsmType.StackAllocation, op1);
                else if(operation.equals("addl") && op2.equals("%esp"))
                    return new AsmLineInfo(AsmType.StackDeallocation, op1);
                if(operation.equals("movl") && op1.equals("%esp") && op2.equals("%ebp"))
                    return new AsmLineInfo(AsmType.BaseToStackPointer);
            }
        }else{
            if(line.equals("retl"))
                return new AsmLineInfo(AsmType.Return);
        }

        return new AsmLineInfo(AsmType.Other);
    }

    public static AsmLineInfo getX64LineType(String line){
        if(line.startsWith(".seh_"))
            return new AsmLineInfo(AsmType.Pseudo);

        var labelMatcher = _labelPattern.matcher(line);
        if(labelMatcher.find())
            return new AsmLineInfo(AsmType.FunctionLabel, labelMatcher.group(1));

        if(line.contains(" ")){
            var matcher = _pattern.matcher(line);
            if(!matcher.find())
                return new AsmLineInfo(AsmType.Other);
            var operation = matcher.group(1);
            var op1 = matcher.group(2);
            if(matcher.groupCount() <= 2){
                if(_x64NonVolatileRegisters.contains(op1)) {
                    if (operation.equals("pushq"))
                        return new AsmLineInfo(AsmType.NonVolatileRegisterSave, op1);
                    if (operation.equals("popq"))
                        return new AsmLineInfo(AsmType.NonVolatileRegisterLoad, op1);
                }
                if(operation.equals("callq"))
                    return new AsmLineInfo(AsmType.Call, op1);
            }else{
                var op2 = matcher.group(3);
                if(operation.equals("subq") && op2.equals("%rsp"))
                    return new AsmLineInfo(AsmType.StackAllocation);
                else if(operation.equals("addq") && op2.equals("%rsp"))
                    return new AsmLineInfo(AsmType.StackDeallocation);
            }
        }else{
            if(line.equals("retq"))
                return new AsmLineInfo(AsmType.Return);
        }

        return new AsmLineInfo(AsmType.Other);
    }

}

