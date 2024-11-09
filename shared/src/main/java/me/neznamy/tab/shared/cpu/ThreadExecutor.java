package me.neznamy.tab.shared.cpu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread executor for accepting tasks to execute them in a single threaded executor.
 * All tasks are try/catch-ed and might track CPU usage if needed.
 */
public class ThreadExecutor {

    private final String threadName;
    private final ScheduledExecutorService executor;

    /**
     * Constructs new instance and starts new thread executor with give name.
     * 
     * @param   threadName
     *          Name of the created thread
     */
    public ThreadExecutor(@NotNull String threadName) {
        this.threadName = threadName;
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(threadName).build());
    }

    /**
     * Shuts down the executor.
     */
    @SneakyThrows
    public void shutdown() {
        executor.shutdown();
        if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            TAB.getInstance().getErrorManager().printError("Soft shutdown of thread " + threadName + " exceeded time limit of 500ms, forcing shutdown. This may cause issues.", null);
            executor.shutdownNow();
        }
    }

    public void execute(@NotNull Runnable task) {
        if (executor.isShutdown()) return;
        executor.execute(new CaughtTask(task));
    }

    public void execute(@NotNull TimedCaughtTask task) {
        if (executor.isShutdown()) return;
        executor.execute(task);
    }

    public void executeLater(@NotNull TimedCaughtTask task, int delayMillis) {
        if (executor.isShutdown()) return;
        executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts a repeating task.
     *
     * @param   task
     *          Task to run periodically
     * @param   intervalMilliseconds
     *          How often should the task run
     */
    public void repeatTask(@NotNull TimedCaughtTask task, int intervalMilliseconds) {
        if (executor.isShutdown()) return;
        executor.scheduleAtFixedRate(task, intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }
}
