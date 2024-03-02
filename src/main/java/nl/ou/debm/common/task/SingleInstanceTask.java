package nl.ou.debm.common.task;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
What this class does:
- It models a thread of execution that can, at any time, be either busy running its current task, or idle.
- Initially there is no task, so the thread is idle.
- When a task is added (by calling setInstance) any previous task is cancelled. This is what makes it a "single" instance task. Adding a new task means all previous tasks are now irrelevant and do not need to be finished.

Example how this is useful: there should be at most one compilation task at any time. When the user clicks the compile button, any previous compilation can be cancelled because the new compilation task would overwrite the results anyway. This is mainly important for speed. The user should not have to wait for a task of which the result is not going to be used.

Implementation detail: There is a currentTask but also a nextTask, so that the caller of setInstance can "enqueue" the task and continue without waiting for the execution to actually start. This is important to avoid a deadlock if both the task initiation and task completion must take place on the GUI thread (which is the case in this program). It also makes it easier to reason about correctness: all functions that acquire a lock perform just a quick action that needs no further locks and then release the lock.
 */
public class SingleInstanceTask {
    ExecutorService exec = Executors.newFixedThreadPool(1);
    Optional<ICancellableTask> next_task = Optional.empty();
    Optional<ICancellableTask> current_task = Optional.empty();
    boolean paused = false;

    public SingleInstanceTask() {
        exec.submit(() -> {
            try {
                taskLoop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //The purpose of taskLoop is to keep running new tasks as they become available
    private void taskLoop() throws InterruptedException {
        while(true)
        {
            //Note that taskLoop is the ONLY function that changes current_task, so it can safely check current_task.isPresent without holding a lock.
            if (current_task.isPresent()) {
                current_task.get().await();
            }

            //Now it can be assumed that current_task has finished or does not exist at all.

            synchronized (this) {
                //wait until there is a next task. setInstance calls notifyAll on (this) so the waiting will end when there is a next task.
                while ( ! next_task.isPresent() || paused) {
                    wait();
                }
                //There is a next task. Make it the current task and continue.
                current_task = next_task;
                next_task = Optional.empty();
                current_task.get().run(); //starts the task but does not block this thread
            }
        }
    }

    public synchronized void setInstance(ICancellableTask new_task) {
        next_task = Optional.of(new_task);
        if (current_task.isPresent()) {
            current_task.get().cancel();
        }
        notifyAll();
    }

    public synchronized void cancel() throws InterruptedException {
        if (next_task.isPresent()) {
            next_task = Optional.empty();
        }
        if (current_task.isPresent()) {
            current_task.get().cancel();
        }
    }

    //If paused, no new tasks are executed until unpaused.
    public synchronized void setPaused(boolean paused_) {
        paused = paused_;
        notifyAll();
    }
}