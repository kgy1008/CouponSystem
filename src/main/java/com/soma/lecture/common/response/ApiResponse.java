package com.soma.lecture.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int code;
    private final String message;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ApiResponse<T> success(SuccessCode success) {
        return new ApiResponse<>(success.getHttpStatus().value(), success.getMessage());
    }

    public static <T> ApiResponse<T> success(SuccessCode success, T data) {
        return new ApiResponse<>(success.getHttpStatus().value(), success.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode error) {
        return new ApiResponse<>(error.getHttpStatus().value(), error.getMessage());
    }

    public static <T> ApiResponse<T> fail(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message);
    }
}
