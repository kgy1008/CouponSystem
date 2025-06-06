package com.soma.lecture.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    COUPON_CREATED(HttpStatus.CREATED, "쿠폰이 성공적으로 생성되었습니다."),
    MEMBER_CREATED(HttpStatus.CREATED, "회원가입이 성공적으로 완료되었습니다."),
    MEMBER_AUTHENTICATED(HttpStatus.OK, "로그인이 성공적으로 완료되었습니다."),
    COUPON_ISSUED(HttpStatus.CREATED, "쿠폰이 성공적으로 발급되었습니다."),
    COUPON_READ(HttpStatus.OK, "내 쿠폰 목록을 성공적으로 불러왔습니다."),
    COUPON_USED(HttpStatus.OK, "쿠폰이 성공적으로 사용되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
