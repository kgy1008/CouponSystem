package com.soma.lecture.coupon.domain;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.response.ErrorCode;
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
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_COUPON_TYPE));
    }
}
