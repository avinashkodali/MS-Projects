package com.aws.ccproject.constants;

import java.util.concurrent.atomic.AtomicInteger;

public final class GenerateIntId {
	private static final AtomicInteger atomicInt = new AtomicInteger(1);

    private GenerateIntId() {}

    public static int generate(){
        return atomicInt.getAndIncrement();
    }
}
