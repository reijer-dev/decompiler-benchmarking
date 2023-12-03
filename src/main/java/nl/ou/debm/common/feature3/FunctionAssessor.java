package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.IAssessor;
import nl.ou.debm.producer.EFeaturePrefix;

public class FunctionAssessor implements IAssessor{

    @Override
    public SingleTestResult GetSingleTestResult(CodeInfo ci) {
        //We skip optimized code, because it confuses our function start and end markers
        if(ci.optimizationLevel == EOptimize.OPTIMIZE)
            return new SingleTestResult(true);

        //Gather original information
        var sourceCVisitor = new CVisitor();
        sourceCVisitor.visit(ci.cparser_org.compilationUnit());

        //Gather decompiled information
        var decompiledCVisitor = new CVisitor();
        decompiledCVisitor.visit(ci.cparser_dec.compilationUnit());

        for (var sourceFunction : sourceCVisitor.functions.values()) {
            var functionName = sourceFunction.getName().substring(EFeaturePrefix.FUNCTIONFEATURE.toString().length() + 1);
            //Skip imperfect functions
            if(sourceFunction.getMarkers().size() != 2)
                continue;

            var startMarker = sourceFunction.getMarkers().get(0);
            var endMarker = sourceFunction.getMarkers().get(1);
            if(!sourceFunction.isMarkerAtStart(startMarker))
                continue;
            if(!sourceFunction.isMarkerAtEnd(endMarker))
                continue;
            if(!startMarker.getFunctionName().equals(functionName))
                continue;

            System.out.println("Looking for function " + sourceFunction.getName());
            //1. Find the decompiled marker
            var decMarker = decompiledCVisitor.markersById.getOrDefault(startMarker.getID(), null);
            if(decMarker == null)
                continue;
            //2. Get the decompiled function
            var decFunction = decompiledCVisitor.functions.get(decMarker.getFunctionId());

            //3. Check start marker
            var decStartMarker = decFunction.getMarkers().get(0);
            if(decStartMarker.getFunctionName().equals(functionName)){
                if(decFunction.isMarkerAtStart(decStartMarker)){
                    System.out.println("Function start found at start!");
                }else{
                    System.out.println("Function start found, but not at the start!");
                }
            }

            var decEndMarker = decFunction.getMarkers().get(decFunction.getMarkers().size()-1);
            if(decEndMarker.getFunctionName().equals(functionName)){
                if(decFunction.isMarkerAtEnd(decEndMarker)){
                    System.out.println("Function end found at end!");
                }else{
                    System.out.println("Function end found, but not at the end!");
                }
            }

            //3. Check for perfect match
            if(decFunction.getMarkers().size() == 2){

            }else{
                System.out.println("Weird amount of markers!");

            }
        }

        //Find the difference
        return new SingleTestResult();
    }
}
