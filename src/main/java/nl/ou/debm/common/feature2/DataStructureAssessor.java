package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.Environment;
import nl.ou.debm.common.feature1.SchoolTestResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//todo hier nog naar kijken. ik heb een soort CountTestResult nodig maar dan eentje die niet aanneemt dat de bovengrens ook de optimale waarde is
class MyCountTestResult extends CountTestResult {
    long target;

    public void setTarget(long target_) { target = target_; }

    @Override
    public Double dblGetTarget() {
        return (double)target;
    }
}

public class DataStructureAssessor implements IAssessor {

    // DataStructureCVisitor also has a Testcase class which is a less processed format. Here, the testcases will be processed further.
    ArrayList<Testcase> testcases_src;
    ArrayList<Testcase> testcases_dec;

    HashMap<Long, Integer> byId_decompiled = new HashMap<>(); //maps codemarker ID to index in array testcases_dec


    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci)
    {
        if (Environment.actual== Environment.EEnv.KESAVA) {
            System.out.println("running DataStructureAssessor for file " + ci.strDecompiledCFilename);
        }
        var ret = new ArrayList<TestResult>();

        // Step 1: extract testcases from the C code. The same extraction is run on both the source and decompiled code. The testcases from the source code will be considered the ground truth. We expect to find the same testcases (that is, codemarkers with the same ID) in the decompiled code. But that is another step.
        {
            var visitor = new DataStructureCVisitor(ci.compilerConfig.architecture);
            visitor.visit(ci.cparser_org.compilationUnit());
            testcases_src = visitor.recovered_testcases;
//            if (Environment.actual== Environment.EEnv.KESAVA) {
//                System.out.println("globale scope amalgamation:"); //todo
//                visitor.nameInfo.currentScope().printNames();
//            }
        } {
            var visitor = new DataStructureCVisitor(ci.compilerConfig.architecture);
            var tree = ci.cparser_dec.compilationUnit();
            var treeShower = new TestMain.CTreeShower();
            try {
                var onderdelen = ci.strDecompiledCFilename.split("binary");
                var filename = onderdelen[onderdelen.length - 1];
                if (Environment.actual== Environment.EEnv.KESAVA) {
                    Files.write(Paths.get("C:/bestanden/tree_" + filename + ".txt"), treeShower.show(tree).getBytes());
                }
            } catch (Exception e) {System.out.println("failed to write tree"); throw new RuntimeException(e); }
            visitor.visit(tree);
            testcases_dec = visitor.recovered_testcases;
        }

        // Step 2: Fill the map byID_decompiled

        for (int i = 0; i< testcases_dec.size(); i++) {
            var testcase = testcases_dec.get(i);
            var id = testcase.codemarker.lngGetID();
            byId_decompiled.put(id, i);
        }

        // Step 3: assess each testcase. First we check if the testcase was actually found by the decompiler.

        // We also keep some counts:
        long testcases_found = 0;
        long variables_identified = 0; //out of found testcases, how many can be assessed because the variable was identified?

        for (var testcase_src : testcases_src)
        {
            var result = new SchoolTestResult(); //todo is this the best kind of testresult to use here?
            result.setWhichTest(ETestCategories.FEATURE2_TODO); //todo set this right. if this is not set, the assessor crashes with nullpointer exception
            var id = testcase_src.codemarker.lngGetID();

            if (!byId_decompiled.containsKey(id)) {
                if (Environment.actual== Environment.EEnv.KESAVA) {
                    System.out.println("warning: codemarker id " + Long.toHexString(id) + " exists in source but not in decompiled code");
                }
                // testcase was not found by the decompiler. We assign the worst possible score.
                result.setScore(0);
                ret.add(result);
                continue;
            }
            testcases_found++;

            var testcase_dec = testcases_dec.get(byId_decompiled.get(id));
            if (testcase_dec.status != Testcase.Status.ok) {
                if (Environment.actual== Environment.EEnv.KESAVA) {
                    System.out.println("warning: codemarker id " + Long.toHexString(id) + " found but the status is not ok");
                }
                result.setScore(0);
                ret.add(result);
                continue;
            }
            variables_identified++;

            // If here, the testcase was found and is assessable.
            //todo probably needs more parameters so that it can access information about the decompiled code that the CVisitor knows about, such as which typedefs exist
            double score = 1; //assessSingleTestcase(testcase_src, testcase_dec);
            result.setScore(score);
            ret.add(result);
        }

        // create some general results not tied to a specific testcase

        var result_testcases_found = new MyCountTestResult();
        result_testcases_found.setLowBound(0);
        result_testcases_found.setHighBound(testcases_src.size());
        result_testcases_found.setTarget(testcases_src.size());
        result_testcases_found.setActualValue(testcases_found);
        result_testcases_found.setWhichTest(ETestCategories.FEATURE2_TODO);

        var result_variables_identified = new MyCountTestResult();
        result_variables_identified.setLowBound(0);
        result_variables_identified.setHighBound(testcases_src.size());
        result_variables_identified.setTarget(testcases_src.size());
        result_variables_identified.setActualValue(variables_identified);
        result_variables_identified.setWhichTest(ETestCategories.FEATURE2_TODO);

        ret.add(result_testcases_found);
        ret.add(result_variables_identified);

        return ret;
    }
}
