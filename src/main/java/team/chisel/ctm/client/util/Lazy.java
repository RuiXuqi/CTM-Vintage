/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package team.chisel.ctm.client.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Proxy object for a value that is calculated on first access, and can be refreshed as well.
 *
 * @param <T> The type of the value
 */
public final class Lazy<T> implements Supplier<T> {
    /**
     * Constructs a lazy-initialized object.
     *
     * @param supplier The supplier for the value, to be called the first time the value is needed,
     *                 or whenever the cache has been invalidated.
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Invalidates the cache, causing the supplier to be called again on the next access.
     */
    public synchronized void invalidate() {
        this.cachedValue = null;
    }

    private final Supplier<T> delegate;
    @Nullable
    private volatile T cachedValue;

    private Lazy(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        T ret = this.cachedValue;
        if (ret == null) {
            synchronized (this) {
                ret = this.cachedValue;
                if (ret == null) {
                    this.cachedValue = ret = this.delegate.get();
                    if (ret == null) {
                        throw new IllegalStateException("Lazy value cannot be null, but supplier returned null: " + this.delegate);
                    }
                }
            }
        }
        return ret;
    }
}
