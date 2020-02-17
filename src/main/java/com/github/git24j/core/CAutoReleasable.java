package com.github.git24j.core;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;

/** Base class that auto release native pointers */
public abstract class CAutoReleasable {
    protected final AtomicLong _rawPtr = new AtomicLong();
    protected final boolean _isWeak;

    protected CAutoReleasable(boolean isWeak, long rawPtr) {
        _isWeak = isWeak;
        _rawPtr.set(rawPtr);
    }

    /** Call this to actual free c pointer, this will be called once this is not a weak ref. */
    protected abstract void freeOnce(long cPtr);

    @Override
    protected void finalize() throws Throwable {
        if (!_isWeak && _rawPtr.get() > 0) {
            freeOnce(_rawPtr.getAndSet(0));
        }
        super.finalize();
    }

    long getRawPointer() {
        long ptr = _rawPtr.get();
        if (_isWeak) {
            return ptr;
        }
        if (ptr == 0) {
            throw new IllegalStateException("Underlying c object has been released");
        }
        return ptr;
    }

    static long rawPtr(@Nullable CAutoReleasable obj) {
        return obj == null ? 0 : obj.getRawPointer();
    }
}