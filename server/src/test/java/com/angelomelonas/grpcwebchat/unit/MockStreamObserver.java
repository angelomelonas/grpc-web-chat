package com.angelomelonas.grpcwebchat.unit;

import io.grpc.stub.StreamObserver;

public class MockStreamObserver implements StreamObserver {
    public Object response = null;
    public Throwable error = null;
    public boolean onCompletedCalled = false;

    @Override
    public void onNext(Object value) {
        this.response = value;
    }

    @Override
    public void onError(Throwable t) {
        this.error = t;
    }

    @Override
    public void onCompleted() {
        onCompletedCalled = true;
    }
}
