package com.soma.lecture.common.exception;

import com.soma.lecture.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public NotFoundException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
