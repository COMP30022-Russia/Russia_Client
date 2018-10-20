package com.comp30022.team_russia.assist.base;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a collection of {@link Disposable} resources, like a bunch of subscriptions, that
 * can be disposed as a whole.
 */
public class DisposableCollection implements Disposable {

    private final Set<Disposable> disposables = new HashSet<>();

    private boolean disposed = false;

    /**
     * Add a {@link Disposable} to this collection.
     * @param disposable The {@link Disposable} to be added.
     */
    public void add(Disposable disposable) {
        if (disposable == null || disposed) {
            return;
        }
        synchronized (this.disposables) {
            this.disposables.add(disposable);
        }
    }

    @Override
    public void dispose() {
        synchronized (this.disposables) {
            for (Disposable disposable : disposables) {
                disposable.dispose();
            }
            this.disposables.clear();
        }
        this.disposed = true;
    }
}
