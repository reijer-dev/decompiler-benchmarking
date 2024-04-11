package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DataStructureProducer implements IFeature, IStatementGenerator, IStructGenerator, IGlobalVariableGenerator
{
    private int structCount = 0;
    private int variableCount = 0;
    private int instance_id; //to generate unique names for datatype instances
    final CGenerator generator;
    public final String external_functions_filename = "datastructure_feature_external_functions.c";

    private HashSet<Function> ptr_accepting = new HashSet<>();

    // todo simple helper to make testing faster
    void addFunction(String body) {
        var f = new Function(DataType.void_t, "datastructure_test_function" + (instance_id++));
        f.addStatement(body);
        generator.addFunction(f);
    }

    public DataStructureProducer(CGenerator generator){
        this.generator = generator;

        //custom codemarker function, used by class DataStructureCodeMarker
        {
            var f = new Function(DataType.void_t, "DataStructureCodeMarker");
            f.addParameter(new FunctionParameter("metadata", DataType.char_t.toPtrType()));
            f.addParameter(new FunctionParameter("variable_address", DataType.void_t.toPtrType()));
            //Doing something with the input appears to help decompilers recognize the number of parameters of this function.
            f.addStatement("printf(metadata);");
            f.addStatement("printf(variable_address);");
            generator.addFunction(f, "DataStructureCodeMarker.c"); //in its own file to prevent inlining
        }

        addFunction("""
            int i = 10;
            """ + (new DataStructureCodeMarker(ETestCategories.FEATURE2_LOCAL_BUILTIN, "int", "i")) + """
            {
                unsigned i;
                """ + (new DataStructureCodeMarker(ETestCategories.FEATURE2_LOCAL_BUILTIN, "unsigned", "i")) + """
            }
            """ + (new DataStructureCodeMarker(ETestCategories.FEATURE2_LOCAL_BUILTIN, "int", "i")) + """
            """
        );

        /*
        //todo gepruts om een beetje een idee te krijgen
        var struct_type = new Struct("mijn_struct");
        struct_type.addProperty(new Variable("i", DataType.make_primitive("int", "0")));
        struct_type.addProperty(new Variable("f", DataType.make_primitive("float", "0.5")));
        struct_type.addProperty(new Variable("next", DataType.ptrType(struct_type.getNameForUse())));
        generator.addStruct(struct_type);

        for (int i=0; i<2; i++) {
            //create testfunction
            var f = new Function(DataType.void_t, "functie_voor_datastructuren" + (instance_id++));
            f.addParameter(new FunctionParameter("ptr", DataType.ptrTypeOf(struct_type)));

            for (var prop : struct_type.getProperties()) {
                if (prop.getType().bIsPrimitive())
                    f.addStatement("ptr->" + prop.getName() + " = 0;");
            }

            var marker = new DataStructureCodeMarker(ETestCategories.FEATURE2_LOCAL_PTR, struct_type.getNameForUse(), "ptr");
            var strMarker = marker.strPrintf();

            //test the difference between real printf and a custom printf function
            if (i == 0) {
                strMarker = strMarker.replace("DataStructureCodeMarker", "printf");
            }
            f.addStatement(strMarker);

            ptr_accepting.add(f);
            generator.addFunction(f, external_functions_filename);
        }
         */
    }

    public DataStructureProducer(){
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
                String instance_name = " my_instance" + (instance_id++);
                ret.add(outparam_type.getNameForUse() + instance_name + ";");
                ret.add(func.getName() + "(" + instance_name + ");"); //function call with the instance as parameter
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
