package com.snor.sunmicardreader.util;

public final class TupleUtil {

    private TupleUtil() {
        throw new AssertionError();
    }

    /**
     * 创建二元组
     */
    public static <A, B> Tuple<A, B> tuple(A a, B b) {
        return new Tuple<>(a, b);
    }



}
