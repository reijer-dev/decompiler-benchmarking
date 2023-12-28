package nl.ou.debm.devtools.explorer;

public interface ICancellableTask {
    void run();
    void cancel();
    void await() throws InterruptedException;
    boolean is_running();
}
