package nl.ou.debm.common.feature2;

import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

//todo: wordt het feit dat dit een IStatementGenerator en andere soorten generators is gebruikt? ik wil er namelijk met name een IFunctionGenerator van maken
public class DataStructuresFeature implements IFeature, IStatementGenerator, IStructGenerator, IGlobalVariableGenerator {
    private int structCount = 0;
    private int variableCount = 0;
    final CGenerator generator;
    public final String external_functions_filename = "datastructure_feature_external_functions.c";

    private HashSet<Function> ptr_accepting = new HashSet<>();

    public DataStructuresFeature(CGenerator generator){
        this.generator = generator;
        //todo gepruts om een beetje een idee te krijgen
        //create testfunction
        var f = new Function(DataType.void_t, "functie_voor_datastructuren");

        var struct_type = new Struct("mijn_struct");
        struct_type.addProperty(new Variable("i", DataType.make_primitive("int", "0")));
        struct_type.addProperty(new Variable("f", DataType.make_primitive("float", "0.5")));
        struct_type.addProperty(new Variable("next", DataType.ptrType(struct_type.getNameForUse())));
//        generator.addStruct(struct_type);

        f.addParameter(new FunctionParameter("ptr", DataType.ptrTypeOf(struct_type)));

        for (var prop : struct_type.getProperties()) {
            if (prop.getType().bIsPrimitive())
                f.addStatement("ptr->" + prop.getName() + " = 0;");
        }

        ptr_accepting.add(f);
        f.setExternalFileName(external_functions_filename);
//        generator.addFunction(f, external_functions_filename);
    }

    public DataStructuresFeature(){
        this.generator = null;
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f) {
        return getNewStatements(currentDepth, f, null);
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        var ret = new ArrayList<String>();
        if (
                ptr_accepting.size() > 0
            && prefs.expression != EStatementPref.NOT_WANTED
        ) {
            ptr_accepting.forEach((func) -> {
                var outparam_type = func.getParameters().get(0).getType();
                ret.add(outparam_type.getNameForUse() + " my_instance;");
                ret.add(func.getName() + "(my_instance);");
            });
            ptr_accepting.clear();
        }
        return ret;
        /*
        // check preferences object
        if (prefs==null){
            prefs = new StatementPrefs(null);
        }

        // this is a stub, so forget about preferences and just return something
        // TODO: expand stub
        var list = new ArrayList<String>();
        list.add("int " + getPrefix() + "_" + variableCount++ + " = 3;\n");
        return list;
         */
    }

    @Override
    public boolean isSatisfied() {
        return ptr_accepting.isEmpty();
    }

    @Override
    public String getPrefix(){ return EFeaturePrefix.DATASTRUCTUREFEATURE.toString(); }

    @Override
    public List<String> getIncludes() {
        return null;
    }

    @Override
    public Struct getNewStruct() {
        var result = new Struct("struct_" + structCount++);
        result.addProperty(new Variable("a", generator.getDataType()));
        return result;
    }

    @Override
    public Variable getNewGlobalVariable(DataType type) {
        if(type == null)
            type = generator.getDataType();
        return new Variable("var_" + variableCount++, type);
    }
}
