package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.Environment;
import nl.ou.debm.common.feature1.SchoolTestResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class SimpleCountTestResult {
    long lowBound;
    long highBound;
    long actual;
}

class SimpleCountTestResults
{
    private HashMap<ETestCategories, SimpleCountTestResult> map = new HashMap<>();

    public SimpleCountTestResult get(ETestCategories testCategory) {
        createIfNotExists(testCategory);
        return map.get(testCategory);
    }

    public void incrementHighbound(ETestCategories testCategory) {
        var testResult = get(testCategory);
        testResult.highBound++;
    }

    public void incrementActual(ETestCategories testCategory) {
        var testResult = get(testCategory);
        testResult.actual++;
    }

    // converts all simple testresults to CountTestResult
    public List<CountTestResult> toCountTestResults(CompilerConfig compilerConfig)
    {
        var ret = new ArrayList<CountTestResult>();
        for (var testCategory : map.keySet())
        {
            var simpleTestResult = map.get(testCategory);
            var countTestResult = new CountTestResult(testCategory, compilerConfig, simpleTestResult.lowBound, simpleTestResult.actual, simpleTestResult.highBound);
            countTestResult.setTargetMode(CountTestResult.ETargetMode.HIGHBOUND);
            //testResult.setTestNumber() // todo is dit nodig?
            ret.add(countTestResult);
        }
        return ret;
    }

    private void createIfNotExists(ETestCategories testCategory) {
        if (map.containsKey(testCategory))
            return;

        var inst =  new SimpleCountTestResult();
        inst.lowBound = 0;
        inst.highBound = 0;
        inst.actual = 0;
        map.put(testCategory, inst);
    }
}

class SingleBinaryAssessor {
    ArrayList<Testcase> testcases_org;
    ArrayList<Testcase> testcases_dec;
    HashMap<Long, Integer> byId_decompiled = new HashMap<>(); //maps codemarker ID to index in array testcases_dec
    SimpleCountTestResults counts = new SimpleCountTestResults();
    public ArrayList<IAssessor.TestResult> testResults = new ArrayList<>();
    CompilerConfig compilerConfig;
    EArchitecture arch;

    public SingleBinaryAssessor(IAssessor.CodeInfo ci)
    {
        compilerConfig = ci.compilerConfig;
        arch = ci.compilerConfig.architecture;

        // Step 1: extract testcases from the C code. The same extraction is run on both the source and decompiled code. The testcases from the source code will be considered the ground truth. We expect to find the same testcases (that is, codemarkers with the same ID) in the decompiled code. But that is another step.
        {
            var visitor = new DataStructureCVisitor(ci.compilerConfig.architecture);
            var tree = ci.cparser_org.compilationUnit();
            visitor.visit(tree);
            testcases_org = visitor.recovered_testcases;
        } {
            var visitor = new DataStructureCVisitor(ci.compilerConfig.architecture);

            // todo these typedefs should be inserted in the code by the decompiler wrapper script. Decompilers are responsible for delivering valid C code.
            String typedefs = "";
            if (ci.strDecompiledCFilename.contains("retdec")) {
                typedefs = """
                    
                """;
            }
            else if (ci.strDecompiledCFilename.contains("hexrays")) {
                System.out.println("hexrays");
                typedefs = """
                    typedef bool BOOL;
                    typedef long LONG;
                    typedef unsigned UINT;
                                    
                    typedef union {
                        char i8;
                        unsigned char u8;
                    } _BYTE;
                                    
                    typedef union {
                        int32_t i32;
                        uint32_t u32;
                    } _DWORD;
                    
                    typedef union {
                        int16_t i16;
                        uint16_t u16;
                    } _WORD;
                    
                    typedef int8_t __int8;
                    typedef int16_t __int16;
                    typedef int32_t __int32;
                    typedef int64_t __int64;
                    """;
            }
            var typedefParser = Parsing.makeParser(typedefs);
            var typedefTree = typedefParser.compilationUnit();
            Parsing.assertNoErrors(typedefParser);
            visitor.visit(typedefTree);

            var tree = ci.cparser_dec.compilationUnit();
            visitor.visit(tree);
            visitor.nameInfo.currentScope().printNames();
            testcases_dec = visitor.recovered_testcases;
        }

        // Step 2: Fill the map byID_decompiled

        for (int i = 0; i< testcases_dec.size(); i++) {
            var testcase = testcases_dec.get(i);
            var id = testcase.codemarker.lngGetID();
            byId_decompiled.put(id, i);
        }

        // Step 3: assess each testcase. First we check if the testcase was actually found by the decompiler.

        for (var testcase_org : testcases_org)
        {
            counts.incrementHighbound(ETestCategories.FEATURE2_ASSESSABLE);
            var id = testcase_org.codemarker.lngGetID();

            if (!byId_decompiled.containsKey(id)) {
                if (Environment.actual == Environment.EEnv.KESAVA) {
                    System.out.println("codemarker ID:" + Long.toHexString(id) + " exists in source but not in decompiled code");
                }
                continue;
            }

            var testcase_dec = testcases_dec.get(byId_decompiled.get(id));
            if (testcase_dec.status != Testcase.Status.ok) {
                if (Environment.actual == Environment.EEnv.KESAVA) {
                    System.out.println("codemarker ID:" + Long.toHexString(id) + " found but the status is " + testcase_dec.status.toString());
                }
                continue;
            }

            // If here, the testcase was found and is assessable.
            counts.incrementActual(ETestCategories.FEATURE2_ASSESSABLE);
            var result = assessTestcase(testcase_org, testcase_dec);
            testResults.add(result);
        }

        testResults.addAll( counts.toCountTestResults(ci.compilerConfig) );
    }

    SchoolTestResult assessTestcase(Testcase testcase_org, Testcase testcase_dec) {
        // update scope counts
        var expectedScope = testcase_org.varInfo.scope;
        if (expectedScope == NameInfo.EScope.global) {
            counts.incrementHighbound(ETestCategories.FEATURE2_GLOBALS_FOUND);
        }
        else {
            counts.incrementHighbound(ETestCategories.FEATURE2_LOCALS_FOUND);
        }

        if (expectedScope == testcase_dec.varInfo.scope) {
            if (expectedScope == NameInfo.EScope.global) {
                counts.incrementActual(ETestCategories.FEATURE2_GLOBALS_FOUND);
            }
            else {
                counts.incrementActual(ETestCategories.FEATURE2_LOCALS_FOUND);
            }
        }

        // assess the recovered data type
        var expected = testcase_org.varInfo.typeInfo.T;
        var actual = testcase_dec.varInfo.typeInfo.T;
        double score;
        ETestCategories testCategory;
        {
            var x = new TypeSimilarityScore(expected, actual, counts, arch);
            score = x.score;
            testCategory = x.testCategory;
        }

        return new SchoolTestResult(testCategory, compilerConfig, score * 10);
    }

    // use a class as a function (the constructor) that returns multiple values (the class members)
    static class TypeSimilarityScore {
        public double score;
        public ETestCategories testCategory;

        private SimpleCountTestResults counts;
        private EArchitecture arch;

        public TypeSimilarityScore(NormalForm.Type expected, NormalForm.Type actual, SimpleCountTestResults counts_, EArchitecture arch_) {
            arch = arch_;
            counts = counts_;
            typeSimilarityScore(expected, actual, true);
        }

        // also updates global counts if updateCounts is true. This is used for recursive calls, where the global counts should not be updated again.
        // All conditions in which the decompiler deserves a score are checked. If no conditions are true, the score remains 0.
        public double typeSimilarityScore(NormalForm.Type expected, NormalForm.Type actual, boolean updateCounts)
        {
            final boolean uc = updateCounts;
            double score = 0;

            // Unions are a special case. If the decompiler emits a union, treat it as if it does not know the type, but it should be either of the union members. The score is an average over the alternatives.
            if (actual instanceof NormalForm.Union actualUnion)
            {
                for (var member : actualUnion.members) {
                    score += typeSimilarityScore(expected, member, updateCounts);
                    updateCounts = false; //to update counts only once (and for the first member of the union, which is a bit arbitrary, but what else can we do...)
                }
                score /= actualUnion.members.size();
                return score;
            }


            if (expected instanceof NormalForm.Pointer expectedPointer)
            {
                testCategory = ETestCategories.FEATURE2_POINTERS_SCORE;
                // First check if the actual type is also a pointer type, but if it's an integer of the right size, that is also partially correct, as pointers are integers.
                if(uc) counts.get(ETestCategories.FEATURE2_POINTERS_FOUND).highBound++;
                if (actual instanceof NormalForm.Pointer actualPointer)
                {
                    if(uc) counts.get(ETestCategories.FEATURE2_POINTERS_FOUND).actual++;
                    score = 0.5 + 0.5 * typeSimilarityScore(expectedPointer.T, actualPointer.T, false);
                }
                else if (actual instanceof NormalForm.Builtin actualBuiltin)
                {
                    if (actualBuiltin.isIntegral() && actualBuiltin.size(arch) == arch.pointerBits()) {
                        score = 0.25;
                    }
                }
            }
            else if (expected instanceof NormalForm.Builtin expectedBuiltin)
            {
                if (expectedBuiltin.isIntegral()) {
                    testCategory = ETestCategories.FEATURE2_INTEGERS_SCORE;
                    if(uc) counts.get(ETestCategories.FEATURE2_INTEGERS_FOUND).highBound++;
                    if (actual instanceof NormalForm.Builtin actualBuiltin)
                    {
                        if (actualBuiltin.isIntegral()) {
                            if(uc) counts.get(ETestCategories.FEATURE2_INTEGERS_FOUND).actual++;
                            score = 0.2;
                            if ( actualBuiltin.unsigned == expectedBuiltin.unsigned )
                                score += 0.3;
                            if ( actualBuiltin.size(arch) == expectedBuiltin.size(arch) )
                                score += 0.5;
                        }
                    }
                }
                else {
                    // it is floating point
                    testCategory = ETestCategories.FEATURE2_FLOATS_SCORE;
                    if(uc) counts.get(ETestCategories.FEATURE2_FLOATS_FOUND).highBound++;
                    if (actual instanceof NormalForm.Builtin actualBuiltin)
                    {
                        if ( !actualBuiltin.isIntegral()) {
                            if(uc) counts.get(ETestCategories.FEATURE2_FLOATS_FOUND).actual++;
                            score = 0.5;
                            if ( actualBuiltin.baseSpecifier.equals(expectedBuiltin.baseSpecifier) )
                                score += 0.5;
                        }
                    }
                }
            }
            else if (expected instanceof NormalForm.Array expectedArray)
            {
                testCategory = ETestCategories.FEATURE2_ARRAYS_SCORE;
                // Like with pointers: first check if the actual type is also an array type.
                if(uc) counts.get(ETestCategories.FEATURE2_ARRAYS_FOUND).highBound++;

                if ( actual instanceof NormalForm.Array
                    || actual instanceof NormalForm.VariableLengthArray)
                {
                    if(uc) counts.get(ETestCategories.FEATURE2_ARRAYS_FOUND).actual++;
                    score = 0.5;

                    NormalForm.Type actualElementType;
                    int actualSize;
                    if (actual instanceof NormalForm.Array actualArray) {
                        actualElementType = actualArray.T;
                        actualSize = actualArray.size(arch);
                    }
                    else {
                        actualElementType = ((NormalForm.VariableLengthArray)actual).T;
                        actualSize = -1;
                    }

                    score += 0.25 * typeSimilarityScore(expectedArray.T, actualElementType, false);

                    if (expectedArray.size(arch) == actualSize)
                        score += 0.25;

                }
                else if (actual instanceof NormalForm.Pointer actualPointer) {
                    // arrays are often used as a pointer to the first element, so the decompiler deserves points if it detects that
                    score = 0.25;
                    score += 0.25 * typeSimilarityScore(expectedArray.T, actualPointer.T, false);
                }
                else if (actual instanceof NormalForm.Struct actualStruct)
                {
                    // if the detected struct contains only members of the same type, that is also correct
                    if (actualStruct.members.isEmpty())
                        return 0;

                    //check if all members are of the same type
                    var firstMemberType = actualStruct.members.get(0);
                    for (var memberType : actualStruct.members) {
                        double similarity = typeSimilarityScore(memberType, firstMemberType, false);
                        if (similarity <= 0.99) {
                            return 0;
                        }
                    }
                    // all members are the same
                    // convert the detected struct to an array and compare to that instead
                    var actualAsArray = new NormalForm.Array(firstMemberType, actualStruct.members.size());
                    score = 0.9 * typeSimilarityScore(expectedArray, actualAsArray, false);
                }
            }
            else if (expected instanceof NormalForm.Struct expectedStruct)
            {
                testCategory = ETestCategories.FEATURE2_STRUCTS_SCORE;
                if(uc) counts.get(ETestCategories.FEATURE2_STRUCTS_FOUND).highBound++;
                if (actual instanceof NormalForm.Struct actualStruct)
                {
                    if(uc) counts.get(ETestCategories.FEATURE2_STRUCTS_FOUND).actual++;
                    score = 0.5;
                    // compare members
                    // Members can only be compared if they have the same offset in the struct. For example, let the expected struct be "struct{ bool b; int i; }" and the actual struct be "struct{ int j; int i; }". The second member of both of those structs is identical, but the offset is different, because int and bool have different sizes. I give 0 score for that because the exact location of bits in a struct is an essential property of structs in C. This method can also be in favor of the decompiler. For example, if the expected struct is "struct{ bool b1,b2,b3,b4; int i; }" and the actual struct is "struct { int i; int j; }", the decompiler receives points for correctly detecting an int at offset 32 (a bool is 8 bits).
                    double membersScore = 0;
                    int expectedMemberOffset = 0;
                    for (var memberExpected : expectedStruct.members)
                    {
                        var memberActual = actualStruct.atOffset(expectedMemberOffset, arch);
                        if (memberActual != null) {
                            membersScore += typeSimilarityScore(memberExpected, memberActual, false);
                        }
                        expectedMemberOffset += memberExpected.size(arch);
                    }
                    // the score for members is averaged
                    score += 0.5 * (membersScore / expectedStruct.members.size());
                }
                else if (actual instanceof NormalForm.Array actualArray) {
                    // if the expected struct contains only members of the same type, detection as an array is correct
                    assert expectedStruct.members.size() > 0 : "generated structs are never empty";

                    var firstMemberType = expectedStruct.members.get(0);
                    for (var memberType : expectedStruct.members) {
                        double similarity = typeSimilarityScore(memberType, firstMemberType, false);
                        if (similarity <= 0.99) {
                            return 0;
                        }
                    }
                    // all members are the same
                    // convert the expected struct to an array and compare to that instead
                    var expectedAsArray = new NormalForm.Array(firstMemberType, expectedStruct.members.size());
                    score = 0.9 * typeSimilarityScore(expectedAsArray, actualArray, false);
                }
            }
            else assert false : "other types are not used in the original code";

            return score;
        }
    }
}

// There are two kinds of testresults returned for each binary: one result per testcase (codemarker) and some extra counts for information, such as the total number of pointers identified as a pointer (regardless of whether the pointer type is otherwise correct).
public class DataStructureAssessor implements IAssessor {
    @Override
    public List<IAssessor.TestResult> GetTestResultsForSingleBinary(IAssessor.CodeInfo ci)
    {
        if (Environment.actual == Environment.EEnv.KESAVA) {
            System.out.println("Running assessor for file " + ci.strDecompiledCFilename);
        }
        return new SingleBinaryAssessor(ci).testResults;
    }
}
