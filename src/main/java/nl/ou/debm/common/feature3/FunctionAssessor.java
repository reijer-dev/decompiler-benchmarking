package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.IAssessor;
import nl.ou.debm.producer.EFeaturePrefix;

import java.util.concurrent.atomic.AtomicReference;

public class FunctionAssessor implements IAssessor{

    @Override
    public SingleTestResult GetSingleTestResult(Codeinfo ci) {
        //We skip optimized code, because it confuses our function start and end markers
        if(ci.optimizationLevel == EOptimize.OPTIMIZE)
            return new SingleTestResult(true);

        var result = new SingleTestResult();
        //We increase this on every check, and increase dblActualValue on every check pass
        result.dblHighBound = 0;

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

            AtomicReference<FoundFunction> decompiledFunction = new AtomicReference<>();
            AtomicReference<FunctionCodeMarker> decompiledMarker = new AtomicReference<>();

            CheckChainBuilder.Check(result, () -> {
                System.out.println("Looking for function " + sourceFunction.getName());
                //1. Find the decompiled marker
                decompiledMarker.set(decompiledCVisitor.markersById.getOrDefault(startMarker.getID(), null));
                return decompiledMarker.get() != null;
            }).WhenPassed(() -> {
                decompiledFunction.set(decompiledCVisitor.functions.get(decompiledMarker.get().getFunctionId()));
                //3. Check start marker
                var decStartMarker = decompiledFunction.get().getMarkers().get(0);
                if(decStartMarker.getFunctionName().equals(functionName)){
                    if(decompiledFunction.get().isMarkerAtStart(decStartMarker)){
                        System.out.println("Function start found at start!");
                        return true;
                    }else{
                        System.out.println("Function start found, but not at the start!");
                        return false;
                    }
                }
                return false;
            }).Then(() -> {
                //3. Check end marker
                var decEndMarker = decompiledFunction.get().getMarkers().get(decompiledFunction.get().getMarkers().size()-1);
                if(decEndMarker.getFunctionName().equals(functionName)){
                    if(decompiledFunction.get().isMarkerAtEnd(decEndMarker)){
                        System.out.println("Function end found at end!");
                        return true;
                    }else{
                        System.out.println("Function end found, but not at the end!");
                        return false;
                    }
                }
                return false;
            }).Then(() -> {
                //3. Check for perfect match
                if(decompiledFunction.get().getMarkers().size() == 2){
                    return true;
                }else{
                    System.out.println("Weird amount of markers!");
                    return false;
                }
            });
        }

        //Find the difference
        return result;
    }


}

@FunctionalInterface
interface ICheck {
    boolean Check();
}

class CheckChainBuilder {
    public boolean checkPassed;
    public IAssessor.SingleTestResult singleTestResult;
    public static CheckChainBuilder Check(IAssessor.SingleTestResult result, ICheck action){
        var newInstance = new CheckChainBuilder();
        newInstance.singleTestResult = result;
        newInstance.checkPassed = true;
        return newInstance.WhenPassed(action);
    }

    public CheckChainBuilder WhenPassed(ICheck action){
        if(!checkPassed)
            return this;

        singleTestResult.dblHighBound++;
        this.checkPassed = action.Check();
        if(this.checkPassed)
            singleTestResult.dblActualValue++;
        return this;
    }

    public CheckChainBuilder WhenFailed(ICheck action){
        if(checkPassed)
            return this;

        singleTestResult.dblHighBound++;
        this.checkPassed = action.Check();
        if(this.checkPassed)
            singleTestResult.dblActualValue++;
        return this;
    }

    public CheckChainBuilder Then(ICheck action){
        singleTestResult.dblHighBound++;
        this.checkPassed = action.Check();
        if(this.checkPassed)
            singleTestResult.dblActualValue++;
        return this;
    }
}