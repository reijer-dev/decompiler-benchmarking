package nl.ou.debm.devtools.explorer;


import nl.ou.debm.common.*;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

class Constants {
    public static String temp_dir = Environment.decompilerPath + "temp\\";
    public static String mainStartMarker = "8270329";
}

public class Main {
    public static void main(String[] args) throws Exception
    {
        new Controller();
    }
}


//Every "decompiler" here is assumed to be an executable or script that accepts two parameters: the program to be decompiled and the location of the decompiled C. Decompilers that work differently will need a wrapper script of this form.
class Decompiler {
    String path;
    String name;
    SingleInstanceTask decompilationTask = new SingleInstanceTask();
}

class Controller {
    GUI gui;
    List<Decompiler> decompilers = new ArrayList<>();
    SingleInstanceTask compilationTask_binary = new SingleInstanceTask();
    SingleInstanceTask compilationTask_LLVM_IR = new SingleInstanceTask();
    SingleInstanceTask compilationTask_assembly = new SingleInstanceTask();
    static String compilerPath = "";

    public Controller() throws Exception {
        //Find the decompilers
        File dir = new File(Environment.decompilerPath);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) continue;
            var d = new Decompiler();
            d.path = file.getPath();
            d.name = file.getName();
            decompilers.add(d);
        }

        System.out.println("Decompilers found:");
        decompilers.forEach(elt -> System.out.println(elt.name + " at " + elt.path));
        System.out.println("done");

        //ensure that the temp directory exists (if so this will do nothing)
        Files.createDirectories(Path.of(Constants.temp_dir));

        SwingUtilities.invokeLater(() -> {
            try {
                gui = new GUI(this);
                compile(); //start compilation and decompilation immediately at startup
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    //Define the names of the used files
    static String source_filePath() { return Constants.temp_dir + "source.c"; }
    static String compiled_binary_filePath() { return Constants.temp_dir + "compiled.exe"; }
    static String compiled_assembly_filePath() { return Constants.temp_dir + "compiled.s"; }
    static String compiled_LLVM_IR_filePath() { return Constants.temp_dir + "compiled.ll"; }
    static String decompiled_C_FilePath(Decompiler d) {
        return Constants.temp_dir + d.name + "-result.c";
    }

    public void compile() throws Exception {
        var flags = gui.compilerGUIElements.source_field_flags.getText().split(" ");
        var c_code = gui.compilerGUIElements.source_codeArea.getText();
        var index = c_code.indexOf("int main()");
        if(index >= 0){
            if(!c_code.contains("#include <stdio.h>"))
                c_code = "#include <stdio.h>" + System.lineSeparator() + System.lineSeparator() + c_code;

            var mainStartMarkerStatement = "printf(\"" +
                    Constants.mainStartMarker +
                    "\");";
            if(!c_code.contains(mainStartMarkerStatement)) {
                //Find beginning of main function body
                index += "int main()".length();
                while(c_code.charAt(index) != '{')
                    index++;
                //Append start marker
                c_code = c_code.substring(0, index + 1) +
                        System.lineSeparator() +
                        mainStartMarkerStatement +
                        System.lineSeparator() +
                        c_code.substring(index + 1);
            }
        }
        gui.compilerGUIElements.source_codeArea.setText(c_code);
        compile(c_code, new ArrayList<String>(List.of(flags)));
    }

    //This also starts decompilation tasks after compilation is done.
    public void compile(String code, List<String> extra_compilerParams) throws Exception {
        System.out.println("new compilation");

        //set the compiler path if not set already. Clang is used.
        if (compilerPath == "") {
            compilerPath = Misc.strGetExternalSoftwareLocation(ECompiler.CLANG.strProgramName());
        }

        // All tasks are ended, because a new compilation means that everything has to be done again (including decompilation). This is not necessary for correctness. It's just to prevent, for example, unnecessary decompilation processes from using up CPU time until they are eventually cancelled anyway, because setInstance also cancels any current tasks.
        compilationTask_binary.cancel();
        compilationTask_assembly.cancel();
        compilationTask_LLVM_IR.cancel();
        decompilers.forEach(d -> {
            try {
                d.decompilationTask.cancel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        //write source code to file
        //This is safe to do at this point because any compilation processes that were still running have been destroyed, so no processes are currently using this file.
        try {
            IOElements.writeToFile(code, source_filePath());
            System.out.println("source code written to " + source_filePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        compilationTask_binary.setInstance(new ProcessTask(() -> {
            //update the GUI
            gui.compilerGUIElements.source_label_status.setText("compiling");

            //define commandline parameters
            var compilerParams = new ArrayList<String>();
            compilerParams.add(compilerPath);
            compilerParams.add(source_filePath());
            compilerParams.add("-o"); compilerParams.add(compiled_binary_filePath());
            compilerParams.addAll(extra_compilerParams);

            //create compilation process
            var procBuilder = new ProcessBuilder(compilerParams);
            System.out.println("defined process: " + compilerParams.toString());
            procBuilder.redirectErrorStream(true);
            return procBuilder;
        }, (result) -> {
            //This is the callback.
            try {
                on_compilation_done(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        //Compile again to obtain LLVM IR. Decompilation takes place only after the task that compiles the binary is done. This task is just to show LLVM IR in the GUI.
        compilationTask_LLVM_IR.setInstance(new ProcessTask(() -> {
            //update the GUI
            gui.compilerGUIElements.LLVM_IR_label_status.setText("compiling");

            //define commandline parameters
            var compilerParams = new ArrayList<String>();
            compilerParams.add(compilerPath);
            compilerParams.add(source_filePath());
            compilerParams.add("-o"); compilerParams.add(compiled_LLVM_IR_filePath());
            compilerParams.add("-S"); compilerParams.add("-emit-llvm");
            compilerParams.addAll(extra_compilerParams);

            //create compilation process
            var procBuilder = new ProcessBuilder(compilerParams);
            System.out.println("defined process: " + compilerParams.toString());
            procBuilder.redirectErrorStream(true);
            return procBuilder;
        }, (result) -> {
            //This is the callback.
            try {
                on_compilation_LLVM_IR_done(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        //Compile again to obtain assembly code.
        compilationTask_assembly.setInstance(new ProcessTask(() -> {
            //update the GUI
            gui.compilerGUIElements.assembly_label_status.setText("compiling");

            //define commandline parameters
            var compilerParams = new ArrayList<String>();
            compilerParams.add(compilerPath);
            compilerParams.add(source_filePath());
            compilerParams.add("-o"); compilerParams.add(compiled_assembly_filePath());
            compilerParams.add("-S");
            compilerParams.addAll(extra_compilerParams);

            //create compilation process
            var procBuilder = new ProcessBuilder(compilerParams);
            System.out.println("defined process: " + compilerParams.toString());
            procBuilder.redirectErrorStream(true);
            return procBuilder;
        }, (result) -> {
            //This is the callback.
            try {
                on_compilation_assembly_done(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    //Starts decompilation and updates the GUI
    public void on_compilation_done(ProcessResult compilation_result) throws Exception {
        System.out.println("on_compilation_done");
        if (compilation_result.exitCode != 0) {
            System.out.println("The compiler terminated abnormally.");
            gui.compilerGUIElements.source_label_status.setText("ready (with errors)");
            //I put the compilation errors in the LLVM IR code area. This does not really make sense but I need somewhere to put it without overwriting the C source code that the user wrote. If regular compilation fails, LLVM IR compilation will probably also fail and vice versa, so it's not really a problem to ignore console output for LLVM IR compilation errors. Quick and dirty
            gui.compilerGUIElements.LLVM_IR_codeArea.setText(compilation_result.consoleOutput);
            return;
        }

        if ( ! IOElements.bFileExists(compiled_binary_filePath())) {
            //todo throw exception?
            System.out.println("Error: compiled program does not exist.");
            return;
        }

        //If here there were no errors.

        //Update the GUI
        gui.compilerGUIElements.source_label_status.setText("ready");

        //Start decompilation tasks
        decompilers.forEach(decompiler ->
        {
            //todo waarom is hier try-catch nodig en in de methode compile niet? volgens mij is de vorm exact hetzelfde...
            try {
                decompiler.decompilationTask.setInstance(new ProcessTask(() -> {
                    //update the GUI
                    gui.decompilerGUIElements.get(decompiler.name).label_status.setText("decompiling");

                    //define commandline parameters
                    var decompilerParams = new ArrayList<String>();
                    decompilerParams.add(decompiler.path);
                    decompilerParams.add(compiled_binary_filePath());
                    decompilerParams.add(decompiled_C_FilePath(decompiler));

                    //create decompilation process
                    var procBuilder = new ProcessBuilder(decompilerParams);
                    System.out.println("defined process: " + decompilerParams.toString());
                    procBuilder.redirectErrorStream(true);
                    return procBuilder;
                }, (result) -> {
                    //This is the callback.
                    try {
                        on_decompilation_done(decompiler, result);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    //Updates the GUI with decompiled code
    public void on_decompilation_done(Decompiler d, ProcessResult result) throws Exception {
        System.out.println("on_decompilation_done");
        if (result.exitCode != 0) {
            System.out.println("The decompiler terminated abnormally.");
            gui.decompilerGUIElements.get(d.name).label_status.setText("ready (with errors)");
            gui.decompilerGUIElements.get(d.name).codeArea.setText(result.consoleOutput);
            return;
        }
        if ( ! IOElements.bFileExists(decompiled_C_FilePath(d))) {
            System.out.println("error: Decompiled code file does not exist");
            return;
        }
        String decompiled_code = Files.readString(Path.of(decompiled_C_FilePath(d)));
        gui.decompilerGUIElements.get(d.name).label_status.setText("ready");
        gui.decompilerGUIElements.get(d.name).codeArea.setText(decompiled_code);
        highlightString(gui.decompilerGUIElements.get(d.name).codeArea, decompiled_code, Constants.mainStartMarker);
    }

    public void on_compilation_LLVM_IR_done(ProcessResult result) throws Exception {
        System.out.println("on_compilation_LLVM_IR_done");
        if (result.exitCode != 0) {
            System.out.println("The compiler (LLVM IR) terminated abnormally.");
            //I do not show the console output in the LLVM IR code area because it's already in use for binary compilation errors.
            gui.compilerGUIElements.LLVM_IR_label_status.setText("ready (with errors)");
            return;
        }
        if ( ! IOElements.bFileExists(compiled_LLVM_IR_filePath())) {
            System.out.println("error: LLVM IR code file does not exist");
            return;
        }
        String compiled_LLVM_IR = Files.readString(Path.of(compiled_LLVM_IR_filePath()));
        gui.compilerGUIElements.LLVM_IR_codeArea.setText(compiled_LLVM_IR);
        gui.compilerGUIElements.LLVM_IR_label_status.setText("ready");
        highlightString(gui.compilerGUIElements.LLVM_IR_codeArea, compiled_LLVM_IR, Constants.mainStartMarker);
    }

    public void on_compilation_assembly_done(ProcessResult result) throws Exception {
        System.out.println("on_compilation_assembly_done");
        if (result.exitCode != 0) {
            System.out.println("The compiler (assembly) terminated abnormally.");
            gui.compilerGUIElements.assembly_codeArea.setText(result.consoleOutput);
            gui.compilerGUIElements.assembly_label_status.setText("ready (with errors)");
            return;
        }
        if ( ! IOElements.bFileExists(compiled_assembly_filePath())) {
            System.out.println("error: assembly code file does not exist");
            return;
        }
        String compiled_assembly = Files.readString(Path.of(compiled_assembly_filePath()));
        gui.compilerGUIElements.assembly_codeArea.setText(compiled_assembly);
        gui.compilerGUIElements.assembly_label_status.setText("ready");
        highlightString(gui.compilerGUIElements.assembly_codeArea, compiled_assembly, Constants.mainStartMarker);
    }

    private void highlightString(JTextArea textArea, String hayStack, String needle){
        //Find main start marker and highlight it
        var needlePosition = hayStack.indexOf(needle);
        if(needlePosition > -1) {
            var painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
            try {
                textArea.getHighlighter().addHighlight(needlePosition, needlePosition + needle.length(), painter);
                textArea.setCaretPosition(needlePosition);
            }catch (Exception ex){}
        }
    }
}

class DecompilerGUIElements {
    JLabel label_name;
    JLabel label_status;
    JTextArea codeArea;
}

class CompilerGUIElements {
    JButton compileButton;

    JTextArea source_codeArea;
    JLabel source_label_status;
    JTextField source_field_flags;
    JLabel source_label_flags;

    JTextArea assembly_codeArea;
    JLabel assembly_label_name;
    JLabel assembly_label_status;

    JTextArea LLVM_IR_codeArea;
    JLabel LLVM_IR_label_name;
    JLabel LLVM_IR_label_status;
}

class Util {
    static JTextArea lineWrappedTextArea() {
        var ret = new JTextArea();
        ret.setLineWrap(true);
        ret.setWrapStyleWord(true);
        return ret;
    }
}

class GUI extends JFrame {
    Controller controller;
    //The following are references to some GUI elements. Not all GUI elements are stored in this class because not all elements need to be referenced.
    Map<String, DecompilerGUIElements> decompilerGUIElements = new HashMap<>(); //maps the decompiler name to the corresponding text area
    CompilerGUIElements compilerGUIElements = new CompilerGUIElements();

    public GUI(Controller controller_) throws Exception {
        controller = controller_;
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setContentPane(main_panel());
    }

    private JPanel main_panel() {
        var ret = new JPanel();
        //The main panel is divided into columns. The compiler related GUI elements use two columns. Then there's a column for each decompiler. Because there is a varying number of columns I set it to 0 here, which means new columns will be created as necessary.
        ret.setLayout(new GridLayout(1, 0));
        compilerPanels().forEach(ret::add);
        controller.decompilers.forEach(decompiler -> {
            ret.add(decompilerPanel(decompiler));
        });
        return ret;
    }

    private List<JPanel> compilerPanels() {
        var ret = new ArrayList<JPanel>();

        compilerGUIElements.assembly_codeArea = Util.lineWrappedTextArea();
        compilerGUIElements.assembly_label_name = new JLabel("assembly");
        compilerGUIElements.assembly_label_status = new JLabel("ready");
        compilerGUIElements.source_codeArea = Util.lineWrappedTextArea();
        compilerGUIElements.source_codeArea.setText("int main() { return 0; }");
        compilerGUIElements.source_label_status = new JLabel("ready");
        compilerGUIElements.source_field_flags = new JTextField();
        compilerGUIElements.source_label_flags = new JLabel("flags");
        compilerGUIElements.LLVM_IR_codeArea = Util.lineWrappedTextArea();
        compilerGUIElements.LLVM_IR_label_name = new JLabel("LLVM IR");
        compilerGUIElements.LLVM_IR_label_status = new JLabel("ready");
        compilerGUIElements.compileButton = new ListeningButton("Compile and decompile") {
            public void mouseClicked(MouseEvent e) {
                try {
                    controller.compile();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        var source_panel = new JPanel();
        source_panel.setLayout(new BorderLayout());
        var source_panel_north = new JPanel();
        source_panel_north.setLayout(new GridLayout(2, 2));
        source_panel_north.add(compilerGUIElements.compileButton);
        source_panel_north.add(compilerGUIElements.source_label_status);
        source_panel_north.add(compilerGUIElements.source_label_flags);
        source_panel_north.add(compilerGUIElements.source_field_flags);
        source_panel.add(source_panel_north, BorderLayout.NORTH);
        source_panel.add(new JScrollPane(compilerGUIElements.source_codeArea), BorderLayout.CENTER);

        var assembly_panel = new JPanel();
        assembly_panel.setLayout(new BorderLayout());
        var assembly_panel_north = new JPanel();
        assembly_panel_north.setLayout(new GridLayout(0, 2));
        assembly_panel_north.add(compilerGUIElements.assembly_label_name);
        assembly_panel_north.add(compilerGUIElements.assembly_label_status);
        assembly_panel.add(assembly_panel_north, BorderLayout.NORTH);
        assembly_panel.add(new JScrollPane(compilerGUIElements.assembly_codeArea), BorderLayout.CENTER);

        var LLVM_IR_panel = new JPanel();
        LLVM_IR_panel.setLayout(new BorderLayout());
        var LLVM_IR_panel_north = new JPanel();
        LLVM_IR_panel_north.setLayout(new GridLayout(0,2));
        LLVM_IR_panel_north.add(compilerGUIElements.LLVM_IR_label_name);
        LLVM_IR_panel_north.add(compilerGUIElements.LLVM_IR_label_status);
        LLVM_IR_panel.add(LLVM_IR_panel_north, BorderLayout.NORTH);
        LLVM_IR_panel.add(new JScrollPane(compilerGUIElements.LLVM_IR_codeArea), BorderLayout.CENTER);

        // I use two columns for compiler related elements. The left panel will contain the source code and assembly. The right panel will contain the LLVM IR. In this way, more space is reserved for LLVM IR.
        var left = new JPanel();
        var right = new JPanel();
        left.setLayout(new GridLayout(2,0));
        left.add(source_panel);
        left.add(assembly_panel);
        right.setLayout(new GridLayout(1,1));
        right.add(LLVM_IR_panel);

        ret.add(left);
        ret.add(right);
        return ret;
    }

    private JPanel decompilerPanel(Decompiler decompiler) {
        var ret = new JPanel();
        ret.setLayout(new BorderLayout());

        var elts = new DecompilerGUIElements();
        elts.codeArea = new JTextArea();
        elts.codeArea.setLineWrap(true);
        elts.codeArea.setWrapStyleWord(true);
        elts.label_name = new JLabel(decompiler.name);
        elts.label_status = new JLabel("ready");
        decompilerGUIElements.put(decompiler.name, elts);

        var north = new JPanel();
        north.setLayout(new GridLayout(0, 2));
        north.add(elts.label_name);
        north.add(elts.label_status);

        var center = new JScrollPane(elts.codeArea);

        ret.add(north, BorderLayout.NORTH);
        ret.add(center, BorderLayout.CENTER);

        return ret;
    }
}
