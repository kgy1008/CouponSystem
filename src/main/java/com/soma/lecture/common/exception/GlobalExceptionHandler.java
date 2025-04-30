package com.soma.lecture.common.exception;

import com.soma.lecture.common.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Void> handleBadRequestException(NotFoundException e) {
        return ApiResponse.fail(e.getErrorCode());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Void> handleBadRequestException(UnauthorizedException e) {
        return ApiResponse.fail(e.getErrorCode());
    }
}
