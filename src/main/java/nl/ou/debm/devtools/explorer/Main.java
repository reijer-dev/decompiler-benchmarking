package nl.ou.debm.devtools.explorer;


import nl.ou.debm.common.*;
import nl.ou.debm.common.task.*;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

class Constants {
    public static String temp_dir = Environment.decompilerPath + "temp\\";
    public static String mainStartMarker = "8270329";
}

public class Main {
    public static void main(String[] args) throws Exception
    {
        //use args[0] as the decompiler location
        if (args.length > 0) {
            Environment.decompilerPath = args[0];
            Constants.temp_dir = Environment.decompilerPath + "temp\\";
        }

        //start program
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
            if ( ! Util.hasExecutableExtension(file.getName())) continue;
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
    // Note: The assembly (.s) and LLVM IR (.ll) files have the same name as the source file, other than the extension. This is intentional, to avoid a problem, when one of the extra flags given in the GUI is a filename. When compiling to assembly or LLVM IR, clang tries to compile all input files to a separate output file. When combined with an explicit output filename (flag -o) clang fails because only one output filename is given and it needs to create multiple files. Therefore I omit the -o flag and rely on the default filenames that clang chooses. When the source file is called source.c, then the assembly file will be called source.s . To be consistent I also name the executable source.exe, which is fitting because it's the source for the decompilers.
    static String source_filePath() { return Constants.temp_dir + "source.c"; }
    static String compiled_binary_filePath() { return Constants.temp_dir + "source.exe"; }
    static String compiled_assembly_filePath() { return Constants.temp_dir + "source.s"; }
    static String compiled_LLVM_IR_filePath() { return Constants.temp_dir + "source.ll"; }
    static String decompiled_C_FilePath(Decompiler d) {
        return Constants.temp_dir + d.name + "-result.c";
    }

    //Tasks that are not needed are paused to save CPU time, or unpaused if needed again. New task instances are still added by the compile function, which is intentional: for example, if a decompiler is hidden, its task will begin once it's made visible again.
    public void on_setting_change() {
        compilationTask_assembly.setPaused(!gui.show_assembly());
        compilationTask_LLVM_IR.setPaused(!gui.show_LLVM_IR());
        decompilers.forEach(d -> {
            d.decompilationTask.setPaused(!gui.show_decompiler(d));
        });

        gui.rebuild();
    }

    // This is a little complex because I want to allow quotes around flags that contain spaces. Otherwise splitting the flags by space would work.
    ArrayList<String> parseFlags(String flags) {
        var ret = new ArrayList<String>();
        var sb = new StringBuilder(); //to build up the currently parsed flag
        var in_quoted_part = false;

        Runnable finish_flag = () -> {
            var flag = sb.toString();
            sb.setLength(0);
            if (flag.equals("")) return;
            ret.add(flag);
        };

        for (int i = 0; i < flags.length(); i++) {
            char c = flags.charAt(i);

            //If the current character is a slash, it may mean that the next character is an escaped quote, in which case the slash must be ignored and the quote emitted as part of the current flag.
            if (c == '\\' && i+1 < flags.length() ) {
                char c_next = flags.charAt(i+1);
                if (c_next == '\"') {
                    sb.append('\"');
                    i++;
                    continue;
                }
            }

            //handle unescaped occurence of a quote
            if (c == '\"') {
                if ( ! in_quoted_part) {
                    in_quoted_part = true;
                    continue;
                }
                else {
                    in_quoted_part = false;
                    //This was the end of a quoted part.
                    finish_flag.run();
                    continue;
                }
            }

            if (c == ' ') {
                if ( ! in_quoted_part) {
                    finish_flag.run();
                    continue;
                }
            }

            sb.append(c);
        }

        finish_flag.run();
        return ret;
    }

    public void compile() throws Exception {
        var flags = parseFlags(gui.compilerGUIElements.source_field_flags.getText());
        System.out.println("flags:");
        for (var flag : flags) {
            System.out.println(flag);
        }
        var c_code = gui.compilerGUIElements.source_codeArea.getText();
        if (gui.compilerGUIElements.use_codeMarker.isSelected()) {
            var index = c_code.indexOf("int main()");
            if (index >= 0) {
                if (!c_code.contains("#include <stdio.h>"))
                    c_code = "#include <stdio.h>" + System.lineSeparator() + System.lineSeparator() + c_code;

                var mainStartMarkerStatement = "printf(\"" +
                        Constants.mainStartMarker +
                        "\");";
                if (!c_code.contains(mainStartMarkerStatement)) {
                    //Find beginning of main function body
                    index += "int main()".length();
                    while (c_code.charAt(index) != '{')
                        index++;
                    //Append start marker
                    c_code = c_code.substring(0, index + 1) +
                            System.lineSeparator() +
                            mainStartMarkerStatement +
                            System.lineSeparator() +
                            c_code.substring(index + 1);
                }
            }
        }
        gui.compilerGUIElements.source_codeArea.setText(c_code);
        compile(c_code, flags);
    }

    //This also starts decompilation tasks after compilation is done.
    public void compile(String code, List<String> extra_compilerParams) throws Exception {
        System.out.println("new compilation");

        //set the compiler path if not set already. Clang is used.
        if (compilerPath == "") {
            compilerPath = Misc.strGetExternalSoftwareLocation(ECompiler.CLANG.strProgramName());
        }

        // All tasks are ended, because a new compilation means that everything has to be done again (including decompilation).
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
            compilerParams.addAll(extra_compilerParams);
            compilerParams.add("-o"); compilerParams.add(compiled_binary_filePath());

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
            compilerParams.addAll(extra_compilerParams);
            compilerParams.add("-S"); compilerParams.add("-emit-llvm");
            // no explicit output filename because that can fail when extra_compilerParams contains a filename
            //compilerParams.add("-o"); compilerParams.add(compiled_LLVM_IR_filePath());

            //create compilation process
            var procBuilder = new ProcessBuilder(compilerParams);
            procBuilder.directory(new File(Constants.temp_dir));
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
            compilerParams.addAll(extra_compilerParams);
            compilerParams.add("-S");
            compilerParams.add("-masm=intel");
            // no explicit output filename because that can fail when extra_compilerParams contains a filename
            //compilerParams.add("-o"); compilerParams.add(compiled_assembly_filePath());

            //create compilation process
            var procBuilder = new ProcessBuilder(compilerParams);
            procBuilder.directory(new File(Constants.temp_dir));
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
    public void on_compilation_done(ProcessTask.ProcessResult compilation_result) throws Exception {
        System.out.println("on_compilation_done");

        //show the console output. For binary compilation, the console output is always shown. For other tasks, the console output is only shown when there is an error.
        gui.compilerGUIElements.binary_consoleOutput_CodeArea.setText(compilation_result.consoleOutput);

        if (compilation_result.exitCode != 0) {
            System.out.println("The compiler terminated abnormally.");
            gui.compilerGUIElements.source_label_status.setText("ready (with errors)");
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
            //todo: unclear why try-catch is required here and not in method compile
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
    public void on_decompilation_done(Decompiler d, ProcessTask.ProcessResult result) throws Exception {
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

    public void on_compilation_LLVM_IR_done(ProcessTask.ProcessResult result) throws Exception {
        System.out.println("on_compilation_LLVM_IR_done");
        if (result.exitCode != 0) {
            System.out.println("The compiler (LLVM IR) terminated abnormally.");
            //I do not show the console output in the LLVM IR code area because it's already in use for binary compilation errors.
            gui.compilerGUIElements.LLVM_IR_codeArea.setText(result.consoleOutput);
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

    public void on_compilation_assembly_done(ProcessTask.ProcessResult result) throws Exception {
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
    RSyntaxTextArea codeArea;
    JCheckBox show;
}

class CompilerGUIElements {
    JButton compileButton;
    JCheckBox use_codeMarker;

    RSyntaxTextArea source_codeArea;
    JLabel source_label_status;
    JTextField source_field_flags;
    JLabel source_label_flags;

    RSyntaxTextArea assembly_codeArea;
    JLabel assembly_label_name;
    JLabel assembly_label_status;
    JCheckBox show_assembly;

    RSyntaxTextArea LLVM_IR_codeArea;
    JLabel LLVM_IR_label_name;
    JLabel LLVM_IR_label_status;
    JCheckBox show_LLVM_IR;

    RSyntaxTextArea binary_consoleOutput_CodeArea;
    JCheckBox show_binary_consoleOutput;
}

class Util {
    static RSyntaxTextArea codeArea(String language) {
        var ret = new RSyntaxTextArea();
        ret.setLineWrap(true);
        ret.setSyntaxEditingStyle(language);
        return ret;
    }

    //This is a workaround for a bug in RSyntaxTextArea to make line wrapping work. It works only when the component is wrapped in a JPanel with BorderLayout. See: https://github.com/bobbylight/RSyntaxTextArea/issues/174
    static JPanel RSyntaxTextArea_linewrapWorkaround(JComponent component) {
        var wrapper = new JPanel(new BorderLayout());
        wrapper.add(component);
        return wrapper;
    }

    static boolean hasExecutableExtension(String filename) {
        return
            filename.endsWith(".exe")
            || filename.endsWith(".bat")
            || filename.endsWith(".ps1")
            || filename.endsWith(".sh")
            || filename.endsWith(".elf")
            || filename.endsWith(".bin")
            || filename.endsWith(".com")
        ;
    }
}

class GUI extends JFrame {
    Controller controller;
    //The following are references to some GUI elements. Not all GUI elements are stored in this class because not all elements need to be referenced.
    Map<String, DecompilerGUIElements> decompilerGUIElements = new HashMap<>(); //maps the decompiler name to the corresponding text area
    CompilerGUIElements compilerGUIElements = new CompilerGUIElements();
    boolean initialized = false;

    public GUI(Controller controller_) throws Exception {
        controller = controller_;
        setTitle(Environment.decompilerPath);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        rebuild();
    }

    public void rebuild() {
        if ( ! initialized) {
            String initial_c_code = "int main() { return 0; }";
            //if the source file saved by a previous instance of the program exists, use it:
            if (IOElements.bFileExists(Controller.source_filePath())) {
                try {
                    initial_c_code = Files.readString(Path.of(Controller.source_filePath()));
                    System.out.println("existing source code read from file");
                } catch(Exception ignored) {}
            }

            //create compiler-related elements
            compilerGUIElements.assembly_codeArea = Util.codeArea(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
            compilerGUIElements.assembly_label_name = new JLabel("assembly");
            compilerGUIElements.assembly_label_status = new JLabel("ready");
            compilerGUIElements.source_codeArea = Util.codeArea(SyntaxConstants.SYNTAX_STYLE_C);
            compilerGUIElements.source_codeArea.setText(initial_c_code);
            compilerGUIElements.source_label_status = new JLabel("ready");
            compilerGUIElements.source_field_flags = new JTextField();
            compilerGUIElements.source_label_flags = new JLabel("flags");
            compilerGUIElements.LLVM_IR_codeArea = Util.codeArea(SyntaxConstants.SYNTAX_STYLE_NONE);
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
            compilerGUIElements.binary_consoleOutput_CodeArea = Util.codeArea(SyntaxConstants.SYNTAX_STYLE_NONE);
            //checkboxes
            compilerGUIElements.use_codeMarker = new JCheckBox("Use code marker", true);
            compilerGUIElements.show_binary_consoleOutput = new JCheckBox("Show console output", show_consoleOutput());
            compilerGUIElements.show_binary_consoleOutput.addItemListener(e -> {
                controller.on_setting_change();
            });
            compilerGUIElements.show_assembly = new JCheckBox("Show assembly", show_assembly());
            compilerGUIElements.show_assembly.addItemListener(e -> {
                controller.on_setting_change();
            });
            compilerGUIElements.show_LLVM_IR = new JCheckBox("Show LLVM IR", show_LLVM_IR());
            compilerGUIElements.show_LLVM_IR.addItemListener(e -> {
                controller.on_setting_change();
            });


            //create decompiler elements
            controller.decompilers.forEach(d -> {
                var elts = new DecompilerGUIElements();
                elts.codeArea = Util.codeArea(SyntaxConstants.SYNTAX_STYLE_C);
                elts.label_name = new JLabel(d.name);
                elts.label_status = new JLabel("ready");
                elts.show = new JCheckBox("Show " + d.name, show_decompiler(d));
                elts.show.addItemListener(e -> {
                    controller.on_setting_change();
                });
                decompilerGUIElements.put(d.name, elts);
            });

            initialized = true;
        }
        setContentPane(main_panel());
        //Without the following the GUI doesn't update until the window is resized.
        repaint();
        revalidate();
    }

    private JPanel main_panel() {
        var ret = new JPanel();
        //The main panel is divided into columns. The compiler related GUI elements use two columns. Then there's a column for each decompiler. Because there is a varying number of columns I set it to 0 here, which means new columns will be created as necessary.
        ret.setLayout(new GridLayout(1, 0));
        default_panels().forEach(ret::add);
        controller.decompilers.forEach(decompiler -> {
            if (show_decompiler(decompiler))
                ret.add(decompilerPanel(decompiler));
        });
        return ret;
    }

    //These panels contain the settings (checkboxes), compile button and all compiler-related code areas (source, LLVM IR and assembly). It's everything except for the decompiler panels (which are not "default" because the user can choose his own decompilers).
    private List<JPanel> default_panels() {
        var ret = new ArrayList<JPanel>();

        var source_panel = new JPanel();
        source_panel.setLayout(new BorderLayout());
        var source_panel_north = new JPanel();
        source_panel_north.setLayout(new GridLayout(0, 2));
        source_panel_north.add(compilerGUIElements.compileButton);
        source_panel_north.add(compilerGUIElements.source_label_status);
        source_panel_north.add(compilerGUIElements.source_label_flags);
        source_panel_north.add(compilerGUIElements.source_field_flags);
        //checkboxes:
        source_panel_north.add(compilerGUIElements.use_codeMarker);
        source_panel_north.add(compilerGUIElements.show_binary_consoleOutput);
        source_panel_north.add(compilerGUIElements.show_assembly);
        source_panel_north.add(compilerGUIElements.show_LLVM_IR);
        controller.decompilers.forEach(d -> {
            source_panel_north.add(decompilerGUIElements.get(d.name).show);
        });
        source_panel.add(source_panel_north, BorderLayout.NORTH);
        source_panel.add(searchableCodeArea(compilerGUIElements.source_codeArea), BorderLayout.CENTER);

        var assembly_panel = new JPanel();
        assembly_panel.setLayout(new BorderLayout());
        var assembly_panel_north = new JPanel();
        assembly_panel_north.setLayout(new GridLayout(0, 2));
        assembly_panel_north.add(compilerGUIElements.assembly_label_name);
        assembly_panel_north.add(compilerGUIElements.assembly_label_status);
        assembly_panel.add(assembly_panel_north, BorderLayout.NORTH);
        assembly_panel.add(searchableCodeArea(compilerGUIElements.assembly_codeArea), BorderLayout.CENTER);

        var LLVM_IR_panel = new JPanel();
        LLVM_IR_panel.setLayout(new BorderLayout());
        var LLVM_IR_panel_north = new JPanel();
        LLVM_IR_panel_north.setLayout(new GridLayout(0,2));
        LLVM_IR_panel_north.add(compilerGUIElements.LLVM_IR_label_name);
        LLVM_IR_panel_north.add(compilerGUIElements.LLVM_IR_label_status);
        LLVM_IR_panel.add(LLVM_IR_panel_north, BorderLayout.NORTH);
        LLVM_IR_panel.add(searchableCodeArea(compilerGUIElements.LLVM_IR_codeArea), BorderLayout.CENTER);

        var consoleOutput_panel = new JPanel(new BorderLayout());
        consoleOutput_panel.add(new JLabel("Console Output (from binary compilation)"), BorderLayout.NORTH);
        consoleOutput_panel.add(
                searchableCodeArea(compilerGUIElements.binary_consoleOutput_CodeArea)
                , BorderLayout.CENTER
        );

        ret.add(source_panel);
        if (show_consoleOutput()) ret.add(consoleOutput_panel);
        if (show_assembly())      ret.add(assembly_panel);
        if (show_LLVM_IR())       ret.add(LLVM_IR_panel);
        return ret;
    }

    private JPanel decompilerPanel(Decompiler decompiler) {
        var ret = new JPanel();
        ret.setLayout(new BorderLayout());

        var elts = decompilerGUIElements.get(decompiler.name);

        var north = new JPanel();
        north.setLayout(new GridLayout(0, 2));
        north.add(elts.label_name);
        north.add(elts.label_status);

        var center = searchableCodeArea(elts.codeArea);

        ret.add(north, BorderLayout.NORTH);
        ret.add(center, BorderLayout.CENTER);

        return ret;
    }

    //This adds functionality for searching and makes the panel scrollable and with line numbers.
    private JPanel searchableCodeArea(RSyntaxTextArea codeArea) {
        var ret = new JPanel(new BorderLayout());

        var searchTerms = new JTextField();

        Runnable searchAction = () -> {
            try {
                var context = new SearchContext(searchTerms.getText());
                context.setMatchCase(false);
                context.setSearchWrap(true);
                SearchEngine.find(codeArea, context);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };

        var searchButton = new ListeningButton("search") {
            public void mouseClicked(MouseEvent e) {
                searchAction.run();
            }
        };

        searchTerms.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    searchAction.run();
                }
            }
        });

        //focus the searchTerms field at ctrl+f when the codeArea has focus
        codeArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_F) {
                    searchTerms.requestFocusInWindow();
                }
            }
        });

        var north = new JPanel(new GridLayout(0, 2));
        north.add(searchButton);
        north.add(searchTerms);

        ret.add(north, BorderLayout.NORTH);
        var scrollPane = new RTextScrollPane(codeArea);
        scrollPane.setLineNumbersEnabled(true);
        ret.add(Util.RSyntaxTextArea_linewrapWorkaround(scrollPane), BorderLayout.CENTER);

        return ret;
    }

    boolean show_assembly() {
        if ( ! initialized)
            return false;
        return compilerGUIElements.show_assembly.isSelected();
    }
    boolean show_LLVM_IR() {
        if ( ! initialized)
            return false;
        return compilerGUIElements.show_LLVM_IR.isSelected();
    }
    boolean show_decompiler(Decompiler d) {
        if ( ! initialized)
            return true;
        return decompilerGUIElements.get(d.name).show.isSelected();
    }
    boolean show_consoleOutput() {
        if ( ! initialized)
            return false;
        return compilerGUIElements.show_binary_consoleOutput.isSelected();
    }
}
