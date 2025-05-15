package com.soma.lecture.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.users.controller.request.MemberRequest;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.repository.MemberRepository;
import com.soma.lecture.users.service.response.UserLoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("옳바르지 않은 길이의 비밀번호가 입력되면 예외가 발생한다")
    void validatePassword1() {
        String password1 = "1234";
        String password2 = "sdfsdfasdfasdfsadfsadfsdaf";

        assertThatThrownBy(() -> {
            userService.validatePassword(password1);
        }).isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ErrorCode.PASSWORD_LENGTH_EXCEPTION.getMessage());

        assertThatThrownBy(() -> {
            userService.validatePassword(password2);
        }).isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ErrorCode.PASSWORD_LENGTH_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("옳바르지 않은 형식의 비밀번호가 입력되면 예외가 발생한다")
    void validatePassword2() {
        String password = "qwertyq33";

        assertThatThrownBy(() -> {
            userService.validatePassword(password);
        }).isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ErrorCode.PASSWORD_FORMAT_EXCEPTION.getMessage());

    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
    void signUp_duplicateEmail() {
        // given
        String email = "test@email.com";
        String password = "Test1234#";
        memberRepository.save(new Member(email, passwordEncoder.encode(password), com.soma.lecture.users.domain.Role.MEMBER));
        MemberRequest request = new MemberRequest(email, password);

        // when & then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(ErrorCode.MEMBER_CONFLICT.getMessage());
    }


    @Test
    @DisplayName("정상적으로 회원가입이 된다")
    void signUp_success() {
        // given
        String email = "unique@email.com";
        String password = "Test1234#";
        MemberRequest request = new MemberRequest(email, password);

        // when
        userService.signUp(request);

        // then
        Member member = memberRepository.findByEmail(email).orElseThrow();
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void login_notFound() {
        MemberRequest request = new MemberRequest("notfound@email.com", "Test1234#");

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOTFOUND.getMessage());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void login_invalidPassword() {
        // given
        String email = "login@email.com";
        String password = "Test1234#";
        memberRepository.save(new Member(email, passwordEncoder.encode(password), com.soma.lecture.users.domain.Role.MEMBER));
        MemberRequest request = new MemberRequest(email, "WrongPassword1!");

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining(ErrorCode.PASSWORD_INVALID.getMessage());
    }

    @Test
    @DisplayName("정상적으로 로그인에 성공한다")
    void login_success() {
        // given
        String email = "success@email.com";
        String password = "Test1234#";
        memberRepository.save(new Member(email, passwordEncoder.encode(password), com.soma.lecture.users.domain.Role.MEMBER));
        MemberRequest request = new MemberRequest(email, password);

        // when
        UserLoginResponse response = userService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo(com.soma.lecture.users.domain.Role.MEMBER);
    }
}
