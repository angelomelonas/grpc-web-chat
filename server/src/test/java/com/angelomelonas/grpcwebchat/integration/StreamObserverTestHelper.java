package com.angelomelonas.grpcwebchat.integration;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StreamObserverTestHelper<T> implements StreamObserver<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamObserverTestHelper.class);

    private final Object completeLock = new Object();
    private boolean completed = false;
    private final List<T> results = new ArrayList<>();
    private Optional<T> result = Optional.empty();
    private final Object onNextLock = new Object();
    private boolean onNext = false;

    @Override
    public void onNext(T value) {
        result = Optional.ofNullable(value);
        results.add(value);
        notifyOnNext();
    }

    @Override
    public void onError(Throwable t) {
        LOGGER.error("onError called in StreamObserverTestHelper", t);
    }

    @Override
    public void onCompleted() {
        notifyOnCompleted();
    }

    public Optional<T> waitForOnNext() {
        try {
            synchronized (onNextLock) {
                while (!onNext && !completed) {
                    onNextLock.wait(100);
                }
            }
            if (!onNext) {
                if (completed) {
                    throw new IllegalArgumentException("Did not get an onNext but an onCompleted.");
                }
            }
            return result;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Optional<T> waitForOnCompleted() {
        synchronized (completeLock) {
            while (!completed) {
                try {
                    completeLock.wait(100);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return result;
    }

    public List<T> getResults() {
        return results;
    }


    protected void notifyOnNext() {
        synchronized (onNextLock) {
            onNext = true;
            onNextLock.notify();
        }
    }

    private void notifyOnCompleted() {
        synchronized (completeLock) {
            completed = true;
            completeLock.notify();
        }
    }
}
