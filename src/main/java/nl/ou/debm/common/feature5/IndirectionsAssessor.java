package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.Misc;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

    Indirections assessor

    What do we measure?
    ===================

    a. Switch quality score
    b. Indirections score


    ad a. Switch quality score
    --------------------------

    We only score switches that we can recognize as switches produced by the indirections producer

    We present three statistics: average score for all switches,
                                 average score for switches implemented using indirection
                                 average score for switches implemented otherwise


    We score each switch 0...10, according to this scheme:
       I.      switch present in binary             0,     1
       II.     correct number of cases              0, .5, 1
       III.    correctness of case ID's             0, .5, 1
       IV.     correctness of default branch        0,     1
       V.      correctness of case start point      0 ...  3
       VI.     absence of case duplication          0      1
       VII.    absence of goto's                    0,  1, 2 +
                                                    --------
                                                    0 ... 10

    I.   switch present in binary
         For every switch in the LLVM, we check if it is in the decompiler output. We score when at least one
         of the switch code markers (before/after/case start/case end) is found.
         If these are not found, the score is set to 0, regardless of any other scores

    II.  correct number of cases
         We compare the number of cases in the decompiler output to the number of cases we know from the LLVM.
         When equal, the switch scores 1. If not equal, we evaluate the gap.
         When more cases are found than present in the LLVM, we score 0.
         When fewer cases are found than present in the LLVM, we score:
             - 0.5 if the difference is 1 case;
             - 0.5 if the difference is 10% or less;
             - 0   in any other case.
         The default branch is ignored in this comparison.

         We only count cases that can be found as direct children of the switch:
         switch (a){
            case 1:         --> counted
            case 2:         --> counted
            default:
              switch (a) {
              case 3:       --> not counted
              case 4:       --> not counted
              }
         }

    III. correctness of case ID's
         We compare all the case ID's from the LLVM data to the decompiler output.
         We make compare two lists:
         LLVM    decompiler output
         0       0
         1       1
         2
                 3
         4       4
         5       5
         6
         This would yield a rough score of 4/7 --> 4 pairs correct, 3 pairs incorrect.
         Rough score 1 yields final score 1.0.
         Rough score .7 and above yields final score 0.5
         Otherwise, 0 final score is 0.0.
         The default branch is ignored in the comparison.

    IV.  correctness of default branch
         We compare the presence of a default branch in the LLVM to its presence in the decompiler output.
         If present in both or not present in both, we score 1.
         If present in the one and not in the other, we score 0.

    V:   correctness of case start point
         We assess all the branches (default branch included) in the LLVM. For every case present in the LLVM,
         we determine whether it is present in the decompiler output. If so, we check whether the branch
         starts with the case start code marker. If so we, we check the caseID in the code marker to the
         case ID in the decompiler output. When equal, we increase a counter.
         In short; the counter is only increased if a case meets all three criteria above.
         Final score is (3 * counter) / number_of_branches_in_LLVM.
         We need to take special care of grouped cases:
         switch (){
           case 0:
           case 1:
           case 2:
              ...code_marker(ID=2)...
         }
         Suppose we consider case 0. We ignore the case labels for 1 and 2. We then recognize a code marker.
         From the LLVM we can determine whether case 0 and case 2 really should be grouped together. If they
         should be grouped together, we accept. If case 2 is suppose to have different branch code, we do not
         score.

    VI:  absence of case duplication
         Case code may not be duplicated in the decompiler output. If multiple branches use the same code,
         that should be emitted only once, but with multiple "case ...:"-references before the case.
         A begin-case code marker may not be emitted more than it was put in the LLVM. Whenever a begin-case
         code marker is emitted more by the decompiler than present in the LLVM, the score is 0. Otherwise,
         we score 1.0.
         We use the begin-case code markers, because end code markers may be duplicated for other reasons (such
         as code duplication while decoding conditional breaks in loops)

    VII: absence of goto's
         There is no need to use any goto in switch code.
         We score 2 if no goto's are found within the switch statement.
         We score 1 if 1 goto is found within the switch statement.
         More goto's means scoring 0.
         We allow for goto's contained within compound statements within the switch statement. These compounds
         are considered not to be part of the switch code itself, but of the code within cases, for example a
         nested loop that may use goto to break out of multiple loops.


    ad b. Indirections score
    ------------------------

    We only score switches that we can recognize as switches produced by the indirections producer

    We present two statistics: indirections found for calculated destinations
                               indirections found for jump table destinations

    When a switch is implemented using indirection, we add the number of correctly identified branches to a total.
    In the end, we divide this total number by the number of branches that should have been identified.

    A correctly identified branch meets the criteria of a.V (see above)
    The number of branches that should have been identified is calculated from the LLVM info.

    We ignore default branches, as they maybe do not use the actual indirect jump, but will probably be caught
    differently.








  ##############

  NEW

  ##############

  Indirection scores

  Raw indirection score
  ---------------------

  We examine all switches present in the LLVM and combine this info with the info from the assembly.
  We select all switches that are implemented using indirection.
  We make a list of all the branches (defaults excluded) that should be found.
  This is the absolute max score for this metric.

  We then look for code markers. Every switch branch (from the LLVM) whose start-of-case code marker
  is somehow present in the decompiler output, scores a point.

  The rationale behind this score, is that, in measuring indirection correctness, the most important
  thing is that the destination of an indirection is found /at all/. So, checking separately is worth it.

  The metrics are returned: 1 for jump tables, 1 for calculations

  We call it 'raw', because we don't care about the neatness or readability. So, if the case code marker
  is not at the start of a switch case (or maybe not in a switch case at all), we still count it.

  But we also correct it for empty cases:
   {
    case 1:
    case 2:
    case 3: codemarker(); break;
    case 4:
    case 5: codemarker(); break;
   }
   cases 1, 2, 4 may be found in the code, but share code markers with 3 and 5.

   another problem is that sometimes branches are offset:
   original: 10 11 12 13 (numbers in code markers), in code: 0 1 2 3
   We also correct for this

   A third problem is lousy C code, but correctly found indirection, resulting in code like this:
   switch (a) {
   ...
     case 1: goto lab
   ...
   }
   goto skip
   lab: <case code, including marker>
        goto skip
   skip: ...

   This shows that the indirection is found, but it's ugly code. However, for the raw statistics,
   we accept this.



 */

public class IndirectionsAssessor implements IAssessor {
    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        // step 1: analyze LLVM
        Misc.redirectErrorStream();     // some errors crop up, but they are from the producer, so we just leave them
        Map<Long, SwitchInfo> switchMap = new HashMap<>();
        ci.llexer_org.reset();
        ci.lparser_org.reset();
        var l_tree = ci.lparser_org.compilationUnit();
        var walker = new ParseTreeWalker();
        var l_listener = new IndirectionLLVMListener(switchMap, ci.lparser_org);
        walker.walk(l_listener, l_tree);
        Misc.unRedirectErrorStream();

        // step 2: analyze assembly
        new AssemblySwitchParser().setIndirectionInfo(switchMap, ci);  // --> results in assertion error at the moment

        // step 3: analyze decompiler output
        var c_tree = ci.cparser_dec.compilationUnit();
        walker = new ParseTreeWalker();
        var c_listener = new IndirectionCListener(ci, switchMap);
        walker.walk(c_listener, c_tree);

        // done!
        final List<TestResult> out = c_listener.getTestResults();

        for (var t: out){
            System.out.println(t);
        }

        return out;
    }
}
