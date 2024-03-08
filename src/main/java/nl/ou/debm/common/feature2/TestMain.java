package nl.ou.debm.common.feature2;

import nl.ou.debm.common.task.ProcessResult;
import nl.ou.debm.common.task.ProcessTask;

import java.util.ArrayList;
import java.util.function.Consumer;

//een programmatje om dingen gerelateerd aan feature2 te testen tijdens ontwikkeling
public class TestMain {

    public static void main(String[] args) throws Exception
    {
        //testen codemarkers
        var marker = new DataStructureCodeMarker(ETypeCategory.struct);
        marker.setProperty("definition", "struct, { int i; }");
        System.out.println(
                marker.strPrintf()
        );
        System.out.println(
                marker.getTypeCategory()
        );

        Consumer<ProcessResult> assertSuccessful = (result) -> {
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

        clangVersionTask.run();
        clangVersionTask.await();
        retDecVersionTask.run();
        retDecVersionTask.await();
        System.out.println("tasks finished");

        /*
        hoe zou het compileerprocess ongeveer kunnen verlopen. Dit is het compileerproces van 1 "Test", oftewel 1 uniek stuk c-code (bestaande uit 2 c-bestanden) wat gecompileerd wordt tot 4 verschillende programmas, dus dit hele compileerproces moet ook weer herhaald worden

        //beide compileerprocessen tegelijkertijd:
        mainCompilerTask.run();
        externalFunctionsCompilerTask.run();

        //wachten tot ze beide klaar zijn:
        mainCompilerTask.await();
        externalFunctionsCompilerTask.await();

        //omzetten naar 1 LLVM IR-bestand:
        linkLLVMTask.run_and_await();

        //nu kunnen er weer 2 dingen tegelijk, namelijk:
        // - linken naar exe
        // - omzetten van de LLVM IR bitcode naar menselijk leesbare LLVM IR
        linkExecutableTask.run();
        humanReadableLLVMTask.run();
        linkExecutableTask.await();
        humanReadableLLVMTask.await();
         */
    }
}
