package nl.ou.debm.common.task;

import nl.ou.debm.common.Misc;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProcessTask implements ICancellableTask {
    public static class ProcessResult {
        public int exitCode;
        public long procId;
        public String consoleOutput="";
        public String command="";

        public ProcessResult(){}
        public ProcessResult(ProcessResult rhs){
            this.exitCode=rhs.exitCode;
            this.procId=rhs.procId;
            this.consoleOutput=rhs.consoleOutput;
            this.command=rhs.command;
        }
        public String toString(){
            return "PID = " + this.procId + " (" + Misc.strGetHexNumberWithPrefixZeros(this.procId, 8) + ")\n" +
                    "command = " + this.command + "\n" +
                    "exit = " + this.exitCode + "\n" +
                    "console=\n" + this.consoleOutput;
        }
    }

    /** reference to a list to store errors in; access will be sync'd */   private final List<ProcessResult> m_ProcessResultList;


    private ProcessBuilder procBuilder;
    private Process proc;
    private Runnable start_process;
    private boolean m_is_running = false;
    private boolean process_created = false;
    private boolean cancelled = false;

    public ProcessTask(Supplier<ProcessBuilder> process_creator, Consumer<ProcessResult> callback) {
        m_ProcessResultList = null;
        DoTheWork(process_creator, callback);
    }

    public ProcessTask(Supplier<ProcessBuilder> process_creator, Consumer<ProcessResult> callback, List<ProcessResult> errorList) {
        m_ProcessResultList = errorList;
        DoTheWork(process_creator, callback);
    }

    private void DoTheWork(Supplier<ProcessBuilder> process_creator, Consumer<ProcessResult> callback) {
        start_process = () -> {
            m_is_running = true;

            //create the process on the GUI thread because process_creator may use the GUI
            SwingUtilities.invokeLater(() -> {
                procBuilder = process_creator.get();
                try {
                    synchronized (this) {
                        proc = procBuilder.start();
                        process_created = true;
                    }

                    var waiter_thread = new Thread(() -> {
                        // a lot of boilerplate to get the console output. I need it, but it's also necessary to read it, otherwise the process will never terminate.
                        // See for example https://stackoverflow.com/questions/3285408/java-processbuilder-resultant-process-hangs
                        final InputStream stdoutInputStream = proc.getInputStream();
                        final BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
                        final String consoleOutput = stdoutReader.lines().collect(Collectors.joining(System.lineSeparator()));

                        try {
                            System.out.println(Misc.strGetHexNumberWithPrefixZeros(proc.pid(),8) + ": waiting for process " + proc.pid());
                            proc.waitFor();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(Misc.strGetHexNumberWithPrefixZeros(proc.pid(),8) + ": done waiting for process " + proc.pid());

                        ProcessResult result = new ProcessResult();
                        result.exitCode = proc.exitValue();
                        result.consoleOutput = consoleOutput;
                        result.procId = proc.pid();

                        if (result.exitCode != 0) {
                            System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId,8) + ": process with id " + result.procId + " exited with code " + result.exitCode);
                            System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId,8) +": Working directory: " + procBuilder.directory());
                            System.out.print(Misc.strGetHexNumberWithPrefixZeros(result.procId,8) +": Command:");
                            for (var parameter : procBuilder.command()) {
                                System.out.print(" " + parameter);
                                result.command+=" " + parameter;
                            }
                            System.out.println("\n" +
                                    Misc.strGetHexNumberWithPrefixZeros(result.procId,8) +
                                    ": console output: " + Misc.strGetHexNumberWithPrefixZeros(result.procId,8) +": " +
                                    result.consoleOutput.replaceAll("\n", "\n" + Misc.strGetHexNumberWithPrefixZeros(result.procId,8) +": "));
                        }

                        //Do not execute the callback if the task was cancelled. Because the cancelled boolean is not protected by locks, cancelling does not guarantee anything. It is intended just to make the task end faster, including by skipping the callback. (In practice, given how this class is currently used, this prevents updating the GUI twice if a compilation/decompilation result of a cancelled task is already available.)
                        //The callback is executed on the GUI thread, which is necessary if it uses the GUI, which my callbacks do. This also means that the waiter thread returns before the task is finished. By definition, I consider a task to be truly finished if it is not running anymore. To allow waiting for that condition, I call notifyAll after setting m_is_running.
                        SwingUtilities.invokeLater(() -> {
                            if ( ! cancelled) {
                                try {
                                    callback.accept(result);
                                }
                                catch (RuntimeException re){
                                    if (m_ProcessResultList!=null) {
                                        synchronized (m_ProcessResultList) {
                                            // add errors to error list
                                            m_ProcessResultList.add(new ProcessResult(result));
                                        }
                                    }
                                }
                            }
                            synchronized (this) {
                                m_is_running = false;
                                notifyAll();
                            }
                        });
                    });
                    waiter_thread.start();

                } catch (IOException e) {
                    var message = new StringBuilder();
                    message.append("Exception during creation of a ProcessTask with command:");
                    for (var parameter : procBuilder.command()) {
                        message.append(" " + parameter);
                    };
                    throw new RuntimeException(message.toString(), e);
                }
            });
        };
    }

    public synchronized void run() {
        assert !process_created; //Assumption: the task can only be run once.
        start_process.run();
    }

    public synchronized void cancel() {
        cancelled = true;
        if (process_created)
            proc.destroy(); //will do nothing if the process does not exist
    }

    public synchronized void await() {
        assert m_is_running || process_created;
        while (m_is_running) {
            //wait for a notification that the task is not running anymore
            try {
                wait();
            } catch (InterruptedException ignored) {
                //consider this task done when there is an exception:
                return;
            }
        }
    }

    public synchronized boolean is_running() {
        return m_is_running;
    }
}
