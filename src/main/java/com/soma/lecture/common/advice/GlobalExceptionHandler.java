package com.soma.lecture.common.advice;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(ConflictException.class)
    public ApiResponse<Void> handleConflictException(ConflictException e) {
        return ApiResponse.fail(e.getErrorCode());
    }

    @ExceptionHandler(BadRequestException.class)
    public ApiResponse<Void> handleBadRequestException(BadRequestException e) {
        return ApiResponse.fail(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        FieldError firstError = bindingResult.getFieldErrors().getFirst();
        String errorMessage = firstError.getDefaultMessage();

        return ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage);
    }
}
