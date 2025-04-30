package com.soma.lecture.coupon.domain;

import java.util.Arrays;

public enum Type {
    CHICKEN("chicken"),
    HAMBURGER("hamburger"),
    PIZZA("pizza");

    private final String name;

    Type(final String name) {
        this.name = name;
    }

    public static Type from(String name) {
        return Arrays.stream(values())
                .filter(n -> n.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 쿠폰 타입입니다: " + name));
    }

}
