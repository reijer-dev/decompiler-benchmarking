// Decompilatie from Ghidra to C
//
// also exports to xml and html

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import utility.function.Callback;

//import ghidra.app.script.GatherParamPanel;
import ghidra.app.script.GhidraScript;
import ghidra.app.util.Option;
import ghidra.app.util.exporter.*;
import ghidra.util.task.TimeoutTaskMonitor;


public class Decompile extends ghidra.app.script.GhidraScript {
    @Override
    public void run() throws Exception {
        // get file name for output
        String[] args = getScriptArgs();
        
        // export to c
        String strFile = args[0];
        System.out.println("Java export script says: exporting to: " + strFile + "...");
        File outputFile = new File(strFile);
        CppExporter cppExporter = new CppExporter();
		List<Option> options = new ArrayList<Option>();
		options.add(new Option(CppExporter.CREATE_HEADER_FILE, new Boolean(false)));
		cppExporter.setOptions(options);
		cppExporter.setExporterServiceProvider(state.getTool());
        var newmonitor = TimeoutTaskMonitor.timeoutIn(10, TimeUnit.MINUTES, monitor);
        long start = System.currentTimeMillis();
        cppExporter.export(outputFile, currentProgram, null, newmonitor);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Java export script says: export time elapsed (s): " + (timeElapsed / 1000));


        // done
        System.out.println("Java export script says: ========= all done. =========");
    }
}
