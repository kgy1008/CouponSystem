package com.soma.lecture.common.exception;

import com.soma.lecture.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnauthorizedException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
