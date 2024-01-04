// Decompilatie from Ghidra to C
//
// also exports to xml and html

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import ghidra.app.script.GatherParamPanel;
import ghidra.app.script.GhidraScript;
import ghidra.app.util.Option;
import ghidra.app.util.exporter.*;

public class Decompile extends ghidra.app.script.GhidraScript {
    @Override
    public void run() throws Exception {
        // get file name for output
        String[] args = getScriptArgs();
        
        // export to c
        String strFile = args[0];
        System.out.println("Java says: exporting to: " + strFile + "...");
        File outputFile = new File(strFile);
        CppExporter cppExporter = new CppExporter();
		List<Option> options = new ArrayList<Option>();
		options.add(new Option(CppExporter.CREATE_HEADER_FILE, new Boolean(false)));
		cppExporter.setOptions(options);
		cppExporter.setExporterServiceProvider(state.getTool());
		cppExporter.export(outputFile, currentProgram, null, monitor);

        // done
        System.out.println("Java says: ========= all done. =========");
    }
}
