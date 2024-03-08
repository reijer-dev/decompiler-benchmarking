package nl.ou.debm.common.task;

public interface ICancellableTask {
    void run();
    void cancel();
    void await() throws InterruptedException;
    boolean is_running();
}
