package nl.ou.debm.common.assembly;

import nl.ou.debm.common.feature3.AsmLineInfo;
import nl.ou.debm.common.feature3.AsmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class AssemblyHelper {

    private static final ArrayList<String> _nonVolatileRegisters = new ArrayList<>(){
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

    private static final ArrayList<String> _x64ArgumentRegisters = new ArrayList<>(){
        { add("%rcx"); }
        { add("%rdx"); }
        { add("%r8"); }
        { add("%r9"); }
    };

    private static final Pattern _functionLabelPattern = Pattern.compile("\\s*_?(.+_function_.+):");
    private static final Pattern _labelPattern = Pattern.compile("^(.+):$");
    private static final Pattern _pattern = Pattern.compile("(\\S+)\\s+(\\S+),?\\s*(\\S+)?");

    public static String Preprocess(String line){
        if(line.contains("#"))
            return line.substring(0, line.indexOf("#"));
        line = line.trim().toLowerCase().replaceAll("\\s+", " ");
        return line;
    }

    public static AsmLineInfo getX86LineType(String line, HashMap<String, String> registerMap){
        var labelMatcher = _functionLabelPattern.matcher(line);
        if(labelMatcher.find())
            return new AsmLineInfo(AsmType.FunctionLabel, labelMatcher.group(1));
        if(_labelPattern.matcher(line).find())
            return new AsmLineInfo(AsmType.OtherLabel);

        if(line.contains(" ")){
            var matcher = _pattern.matcher(line);
            if(!matcher.find())
                return new AsmLineInfo(AsmType.Other);
            var operation = matcher.group(1);
            var op1 = matcher.group(2).replace(",", "");
            var op2 = matcher.group(3);
            if(op2 == null){
                if(_nonVolatileRegisters.contains(op1) || getEquivalentRegisters(op1, registerMap).stream().anyMatch(_nonVolatileRegisters::contains)) {
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
                    return new AsmLineInfo(AsmType.StackAllocation, op1.replace("$", ""));
                else if(operation.equals("addl") && op2.equals("%esp"))
                    return new AsmLineInfo(AsmType.StackDeallocation, op1);
                if(operation.equals("movl") && op1.equals("%esp") && op2.equals("%ebp"))
                    return new AsmLineInfo(AsmType.BaseToStackPointer);
                if(operation.equals("movl") && op1.startsWith("%") && op2.startsWith("%"))
                    return new AsmLineInfo(AsmType.RegisterMove, op2, op1);
            }
        }else{
            if(line.equals("retl"))
                return new AsmLineInfo(AsmType.Return);
        }

        return new AsmLineInfo(AsmType.Other);
    }

    public static AsmLineInfo getX64LineType(String line, ArrayList<String> homedRegisters, HashMap<String, String> registerMap){
        if(line.startsWith(".seh_"))
            return new AsmLineInfo(AsmType.Pseudo);

        var labelMatcher = _functionLabelPattern.matcher(line);
        if(labelMatcher.find())
            return new AsmLineInfo(AsmType.FunctionLabel, labelMatcher.group(1));

        if(line.contains(" ")){
            var matcher = _pattern.matcher(line);
            if(!matcher.find())
                return new AsmLineInfo(AsmType.Other);
            var operation = matcher.group(1);
            var op1 = matcher.group(2).replace(",", "");
            var op2 = matcher.group(3);
            if(op2 == null){
                if(_nonVolatileRegisters.contains(op1)) {
                    if (operation.equals("pushq"))
                        return new AsmLineInfo(AsmType.NonVolatileRegisterSave, op1);
                    if (operation.equals("popq"))
                        return new AsmLineInfo(AsmType.NonVolatileRegisterLoad, op1);
                }
                if(operation.equals("callq"))
                    return new AsmLineInfo(AsmType.Call, op1);
            }else{
                if(operation.equals("subq") && op2.equals("%rsp"))
                    return new AsmLineInfo(AsmType.StackAllocation, op1.replace("$", ""));
                if(operation.equals("addq") && op2.equals("%rsp"))
                    return new AsmLineInfo(AsmType.StackDeallocation);
/*                if(operation.equals("movq") && op1.equals("%rcx") && op2.equals("%rsi"))
                    return new AsmLineInfo(AsmType.BaseToStackPointer);*/
                if(operation.equals("movq") && op1.startsWith("%") && op2.startsWith("%"))
                    return new AsmLineInfo(AsmType.RegisterMove, op1, op2);
                if(operation.equals("movq") && (_x64ArgumentRegisters.contains(op1) || getEquivalentRegisters(op1, registerMap).stream().anyMatch(_x64ArgumentRegisters::contains)) && !homedRegisters.contains(op2))
                    return new AsmLineInfo(AsmType.RegisterHoming, op1);
                if(operation.equals("leaq") && op1.contains("str") && op2.startsWith("%")){
                    return new AsmLineInfo(AsmType.LoadStringInRegister, op1, op2);
                }
            }
        }else{
            if(line.equals("retq"))
                return new AsmLineInfo(AsmType.Return);
        }

        return new AsmLineInfo(AsmType.Other);
    }

    private static List<String> getEquivalentRegisters(String register, HashMap<String, String> map){
        var result = new ArrayList<String>();
        while(map.containsKey(register)){
            result.add(map.get(register));
            register = map.get(register);
        }
        return result;
    }

}

