// Ghidra decompilation to C

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

//import ghidra.app.script.GatherParamPanel;
import ghidra.app.decompiler.DecompileOptions;
import ghidra.app.script.GhidraScript;
import ghidra.app.util.Option;
import ghidra.app.util.exporter.CppExporter;

public class ghidra_decompile extends ghidra.app.script.GhidraScript {
	private final int iTimeout = 30 * 60; // 30 minutes timeout per function
	
    @Override
    public void run() throws Exception {
        // arguments contain the output filename
        String[] args = getScriptArgs();
        
        CppExporter cppExporter;
		{
			var decompileOptions = new DecompileOptions();
			decompileOptions.setDefaultTimeout(iTimeout);
			System.out.println("timeout set to " + iTimeout);

			// I use the CppExporter constructor that takes arguments but that's just because I want to set the DecompileOptions. For the other parameters, I have taken these defaults from CppExporter.java:
			boolean isCreateHeaderFile = false;
			boolean isCreateCFile = true;
			boolean isUseCppStyleComments = true;
			boolean emitDataTypeDefinitions = true;
			String tagOptions = "";
			boolean excludeMatchingTags = true;
			cppExporter = new CppExporter(decompileOptions, isCreateHeaderFile, isCreateCFile, emitDataTypeDefinitions, excludeMatchingTags, tagOptions);
			
			List<Option> options = new ArrayList<Option>();
			options.add(new Option(CppExporter.CREATE_HEADER_FILE, new Boolean(false)));
			cppExporter.setOptions(options);
			cppExporter.setExporterServiceProvider(state.getTool());
		}
		
		// exporteer to file
        System.out.print("Exporting to: " + Arrays.toString(args) + "...");
        File outputFile = new File(args[0]);
		cppExporter.export(outputFile, currentProgram, null, monitor);
        System.out.println(" done.");
    }
}