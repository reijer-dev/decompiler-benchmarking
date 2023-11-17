/*
package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.LLVMIRBaseVisitor;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class LLVMVisitor extends LLVMIRBaseVisitor {
    public List<FoundFunction> functions = new ArrayList<>();
    private FoundFunction lastFunction;
    public HashMap<String,String> markerStringDefinitions = new HashMap<>();
    private Pattern _pattern;

    public LLVMVisitor() {
        _pattern = Pattern.compile("(.+?)\\s*=\\s*.*?constant\s*\\[[0-9]+\\s*x\\s*i8\\]\\s*c\""+FunctionProducer.FunctionMarkerPrefix+"(.+?)\"", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Object visitFuncDef(LLVMIRParser.FuncDefContext ctx) {
        var function = new FoundFunction();
        var functionId = fun
        var instructions = ctx.funcBody().basicBlock(0).instruction();
        var lineIndex = 0;
        for(var instruction : instructions){
            if(instruction.localDefInst() != null){
                if(instruction.localDefInst().valueInstruction() != null){
                    var call = instruction.localDefInst().valueInstruction().callInst();
                    if(call != null){
                        var args = call.args().arg();
                        if(args.size() > 0){
                            var firstArg = args.get(0).value().getText();
                            var callWithMarker = markerStringDefinitions.keySet().stream().filter(firstArg::contains).findFirst();;
                            if(callWithMarker.isPresent()){
                                var marker = new FunctionCodeMarker(markerStringDefinitions.get(callWithMarker.get()), lineIndex);
                                function.addMarker(marker);
                            }
                        }
                    }
                }
            }
            lineIndex++;
        }

        //Return statements do not occur in the basicBlock.instruction, but in the terminator block after that.
        //So lineIndex contains the number of statements before the return statement
        //We need this amount, to figure out whether the end marker is the last statement before an eventual return statement
        function.setNumberOfStatements(lineIndex);
        functions.add(function);
        return super.visitFuncDef(ctx);
    }

    @Override
    public Object visitGlobalDef(LLVMIRParser.GlobalDefContext ctx){
        var matcher = _pattern.matcher(ctx.getText());
        if(matcher.find())
            markerStringDefinitions.put(matcher.group(1), matcher.group(2));
        return super.visitGlobalDef(ctx);
    }
}
*/
