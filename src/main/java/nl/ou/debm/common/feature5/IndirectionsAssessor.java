package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

    ***************************
    ** Indirections assessor **
    ***************************



    Introduction on terms
    =====================

    Consider this source example:
    <before switch code marker>
    switch (what){
        case -4:
        case 1:
        case 6: <begin case code marker for case 6>
                <possible other code>
                <end case code marker for case 6>
        case 11:<begin case code marker for case 11>
                <possible other code>
                <end case code marker for case 11>
        case 16:<begin case code marker for case 16>
                <possible other code>
                <end case code marker for case 16>
        default:<begin case code marker for default case>
                <possible other code>
                <end case code marker for default case>
    }
    <after switch code marker>

    Consider this decompiler output example:
    <before switch code marker>
    switch (what'){
        case 0:
        case 1:
        case 2: <begin case code marker for case 6>
                <possible other code>
                <end case code marker for case 6>
                goto done;
        default: {
           switch (what') {
              case 3: <begin case code marker for case 11>
                      <possible other code>
                      <end case code marker for case 11>
                      goto done
              case 4: goto lab1;
              default:<begin case code marker for default case>
                      <possible other code>
                      <end case code marker for default case>
                      goto done;
           }
    }
    lab1:
        <begin case code marker for case 16>
        <possible other code>
        <end case code marker for case 16>
        goto done;
    done:
    <after switch code marker>

    ___translation___
    The series -4;1;6;11;16 is changed to 0;1;2;3;4 in the decompiler output. We call this translation of the cases.
    We see this occur when there exist and a and b for which we can find:
    <original case number> = <decompiler output> x a + b
    a should always be 1 and b should be 0 (meaning no translation), but in the example above a=5 and b=-4
    The switch is evidently implemented by calculation what'=(what - b) / a.
    Per switch, we always try calculating the a and b factors.

    ___children/grand children___
    In the decompiler output, we see a default branch starting with the same switch statement expression as
    the first switch statement. That means that case 3 and the default branch could (and should) be put
    immediately in the first switch statement, without the use of the second. We call cases 0...2 children of
    the outer switch and cases 3+default its grand children.

    ___empty cases___
    cases -4/1 or 0/1 are empty. They serve as extra labels for cases 6 and 2 respectively

    ___goto block___
    case 16/4 has his contents not in the case block itself, but after the case. We call the part from lab1: to done:
    a goto-block. It is ugly code, but still reasonably recognizable. In the switch statement itself, we only find
    a single goto statement (decompiler output).



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
       III.    correctness of case ID's             0, .5, 1 [possible -/- .1]
       IV.     correctness of default branch        0,     1
       V.      correctness of case start point      0 ...  3
       VI.     absence of case duplication          0      1
       VII.    absence of goto's                    0,     1
       VIII.   absence of grand children            0,     1 +
                                                    --------
                                                    0 ... 10

    I.   switch present in binary (A-score)
         For every switch in the LLVM, we check if it is in the decompiler output. We score when at least one
         of the switch code markers (before/after/case start/case end) is found.
         If these are not found, the score is set to 0, regardless of any other scores

    II.  correct number of cases (B-score)
         We compare the number of cases in the decompiler output to the number of cases we know from the LLVM.
         When equal, the switch scores 1. If not equal, we evaluate the gap.
         When more cases are found than present in the LLVM, we score 0.
         When fewer cases are found than present in the LLVM, we score:
             - 0.5 if the difference is 1 case;
             - 0.5 if the difference is 10% or less;
             - 0   in any other case.
         The default branch is ignored in this comparison.

    III. correctness of case ID's (C-score)
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
         We translate the decompiler outputs using the a- and b- factors we calculated. If they are not 1.0 and 0.0
         respectively, final scores or 0.5 and 1.0 are lowered to 0.4 and 0.9 respectively. We do this, because
         we think the decompiler should detect the translation and process it itself


    IV.  correctness of default branch (D-score)
         We compare the presence of a default branch in the LLVM to its presence in the decompiler output.
         If present in both or not present in both, we score 1.
         If present in the one and not in the other, we score 0.

         When considering the C-code, we only count true default branches:
         switch(a){
            case 1:
            case 2:
            default:    //  --> true default branch, because no sub switches occur
         }
         switch(b){
            case 1:
            case 2:
            default:    //  --> not a true default branch, because a sub switch occurs
              switch(b){
              case 3:
              case 4:
              }               ____ default in LLVM: the example above does not give this information
         }                   /
                            /                    ____ default in C: based on the example above
                           /                    /
                      default in LLVM    default in C     result
         switch a         present           present          1
         switch a       not present         present          0
         switch b         present        not present         0
         switch b       not present      not present         1

    V:   correctness of case start point (E-score)
         We assess all the branches (default branch included) in the LLVM. (step 1:) For every case, we work out which
         case marker we should encounter in the code, compensating for empty branches like this example:
         switch (a){
           case 0:
           case 1:
           case 2:
              ...code_marker(ID=2)...
         }
         Case 0 should point to a code marker with case ID 2. We know this, because the switch's start code marker
         shows the cases, including being empty or not.
         (step 2:)
         Then we look for this code marker in the list of cases found for this switch in the decompiler output.
         If we do not find it at all, we score this case a 0.0.
         (step 3:)
         If we do find it, we continue by comparing the case ID in the decompiler output to the case ID in the LLVM,
         using translation constants (see above). We compare all cases in the decompiler output, for more cases may
         share the same code marker (such as cases 0...2 in the example above).
         If we don't find an appropriate code marker, we score this case a 0.0.
         (step 4:)
         If we do find the appropriate code marker, we check if this code marker was the first statement in the case.
         If so, we score the case a 1.0. If not, we score the case 0.0.

         We add all the case scores to each other and divide by the number of cases in the LLVM. We then multiply
         with 3. This is the score for this switch.

         We accept code like this:
                 switch (a){
                    case 0: goto lab0
                    case 1: goto lab1
                    case 2: goto lab2
                 }
                 lab0: code marker
                       goto end_of_switch_code
                 lab1: code marker
                       goto end_of_switch_code
                 lab2: code marker
                       goto end_of_switch_code
                 end_of_switch_code: ;
         We do this, because we can reconstruct a direct path; a case has only a single goto statement and the point it
         points to, is the branch itself. The code may be ugly, but it is still readable (enough) and the decompiler will be
         punished in the G-score for the use of goto's.

         We accept branches that are not direct children, because the presence of grand children is scored in the H-score
         (see below for details).

    VI:  absence of case duplication (F-score)
         Case code may not be duplicated in the decompiler output. If multiple branches use the same code,
         that should be emitted only once, but with multiple "case ...:"-references before the case.
         A begin-case code marker may not be emitted more than it was put in the LLVM. Whenever a begin-case
         code marker is emitted more by the decompiler than present in the LLVM, the score is 0. Otherwise,
         we score 1.0.
         We use the begin-case code markers, because end code markers may be duplicated for other reasons (such
         as code duplication while decoding conditional breaks in loops.)

    VII: absence of goto's (G-score)
         There is no need to use any goto in switch code. So, whenever we find a goto in a switch block, we
         score 0 and otherwise 1.
         However, we must discriminate between goto's that are caused by badly functioning switch interpretation
         and goto's that are caused by, for example, included loops.
         consider this example:
         switch (a){
           case 1:
             { for (....) {
                 goto break-multiple-loops; }
             }
             goto end;
           case 2:
             { goto end; }
         }
         The goto-break-multiple-loops is within another statement's code. The two goto end's are not ok.


    VIII:absence of grand children (H-score)
         Consider this example
         switch (a){        --> switch
            case 1:         --> child
            case 2:         --> child
            default:
              switch (a) {  --> "duplicated" switch
              case 3:       --> grand child
              case 4:       --> grand child
              }
         }
         Though it is clear what is happening, it is ugly code. The grand children should be added directly to
         to the first switch, without a default branch and a second switch.
         So, we test for this: if we find such a construction, we score 0.0, otherwise 1.0.



    ad b. Indirections score

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
        Map<Long, CodeMarker.CodeMarkerLLVMInfo> basicLLVMInfo = new HashMap<Long, CodeMarker.CodeMarkerLLVMInfo>();
        ci.llexer_org.reset();
        ci.lparser_org.reset();
        var l_tree = ci.lparser_org.compilationUnit();
        var walker = new ParseTreeWalker();
        var l_listener = new IndirectionLLVMListener(switchMap, ci.lparser_org, basicLLVMInfo);
        walker.walk(l_listener, l_tree);
        Misc.unRedirectErrorStream();

        // step 2: analyze assembly
        new AssemblySwitchParser().setIndirectionInfo(switchMap, ci);  // --> results in assertion error at the moment

        // step 3: analyze decompiler output
        var c_tree = ci.cparser_dec.compilationUnit();
        walker = new ParseTreeWalker();
        var c_listener = new IndirectionCListener(ci, switchMap, basicLLVMInfo);
        walker.walk(c_listener, c_tree);

        // done!
        final List<TestResult> out = c_listener.getTestResults();

        for (var t: out){
            System.out.println(t);
        }

        return out;
    }
}
