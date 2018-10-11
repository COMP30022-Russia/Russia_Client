package com.comp30022.team_russia.assist;

import org.junit.Rule;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all unit tests.
 */
public class TestBase {
    /**
     * The LiveData fields in the ViewModel are calculated asynchronously.
     * This is fine (actually preferred) in the application. But in unit tests,
     * that behaviour can lead to our tests finishing before the fields are
     * updated.
     * Therefore, we use InstantTaskExecutorRule to force tests to run
     * synchronously (i.e. single-threaded).
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule
        = new InstantTaskExecutorRule();

    /**
     * Single-threaded, synchronous executor service.
     */
    protected ExecutorService executorService = new SynchronousExecutorService();
}

class SynchronousExecutorService extends AbstractExecutorService {

    @Override
    public void shutdown() {

    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit)
        throws InterruptedException {
        return false;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }
}
