package com.soma.lecture.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 409 Error
    MEMBER_CONFLICT(HttpStatus.CONFLICT, "이미 가입한 회원입니다."),
    // 400 Error
    PASSWORD_LENGTH_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호는 최소 8자, 최대 10자로 설정해주세요."),
    PASSWORD_FORMAT_EXCEPTION(HttpStatus.BAD_REQUEST, "비밀번호 형식이 틀렸습니다."),
    INVALID_COUPON_TYPE(HttpStatus.BAD_REQUEST, "쿠폰 타입이 잘못되었습니다."),
    COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST, "쿠폰이 모두 소진되었습니다"),
    // 401 Error
    PASSWORD_INVALID(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다."),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "관리자만 접근 가능합니다."),
    // 404 Error
    MEMBER_NOTFOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    COUPON_NOTFOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    // 409 Error
    ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 쿠폰이 발급되었습니다.")
    ;
    private final HttpStatus httpStatus;
    private final String message;
}
