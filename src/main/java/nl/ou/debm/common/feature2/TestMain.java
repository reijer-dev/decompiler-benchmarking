package nl.ou.debm.common.feature2;

import nl.ou.debm.common.task.ProcessTask;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.DataType;
import nl.ou.debm.producer.Struct;

import java.util.ArrayList;
import java.util.function.Consumer;

//een programmatje om dingen gerelateerd aan feature2 te testen tijdens ontwikkeling
public class TestMain {

    public static void main(String[] args) throws Exception
    {
        //testen codemarkers
        var marker = new DataStructureCodeMarker(ETypeCategory.struct, "S", "var");
        marker.setProperty("definition", "struct S { int i; }");
        System.out.println(
                marker.strPrintf()
        );
        System.out.println(
                marker.getTypeCategory()
        );

        Consumer<ProcessTask.ProcessResult> assertSuccessful = (result) -> {
            assert result.exitCode == 0;
            System.out.println(result.consoleOutput);
        };

        //testen ProcessTask
        var retDecVersionTask = new ProcessTask(()->{
            var params = new ArrayList<String>();
            params.add("retdec-decompiler");
            params.add("--version");
            var procBuilder = new ProcessBuilder(params);
            return procBuilder;
        }, assertSuccessful);

        var clangVersionTask = new ProcessTask(()-> {
            var params = new ArrayList<String>();
            params.add("clang");
            params.add("--version");
            var procBuilder = new ProcessBuilder(params);
            return procBuilder;
        }, assertSuccessful);

        var sb = new StringBuilder();
        sb.append("bla");

        System.out.println("eerste keer: " + sb.toString());
        System.out.println("tweede keer: " + sb.toString());

        var sb2 = new StringBuilder();
        sb2.append(sb);

        System.out.println("sb: " + sb.toString());
        System.out.println("sb2: " + sb2.toString());

        /*
        clangVersionTask.run();
        clangVersionTask.await();
        retDecVersionTask.run();
        retDecVersionTask.await();

        System.out.println("tasks finished");
        */
    }
}
