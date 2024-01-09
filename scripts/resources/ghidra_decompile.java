// Decompilatie via Ghidra naar C

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

//import ghidra.app.script.GatherParamPanel;
import ghidra.app.script.GhidraScript;
import ghidra.app.util.Option;
import ghidra.app.util.exporter.CppExporter;

public class ghidra_decompile extends ghidra.app.script.GhidraScript {
    @Override
    public void run() throws Exception {
        // haal parameters --> oftewel de uitvoerbestandsnaam
        String[] args = getScriptArgs();
        
        // exporteer naar uitvoerfile
        System.out.print("Exporteren naar: " + Arrays.toString(args) + "...");
        File outputFile = new File(args[0]);
        CppExporter cppExporter = new CppExporter();
		List<Option> options = new ArrayList<Option>();
		options.add(new Option(CppExporter.CREATE_HEADER_FILE, new Boolean(false)));
		cppExporter.setOptions(options);
		cppExporter.setExporterServiceProvider(state.getTool());
		cppExporter.export(outputFile, currentProgram, null, monitor);
        // geef weer
        System.out.println(" klaar.");
    }
}
