package com.soma.lecture.users.service;

import com.soma.lecture.common.exception.BadRequestException;
import com.soma.lecture.common.exception.ConflictException;
import com.soma.lecture.common.exception.NotFoundException;
import com.soma.lecture.common.exception.UnauthorizedException;
import com.soma.lecture.common.response.ErrorCode;
import com.soma.lecture.users.controller.request.MemberRequest;
import com.soma.lecture.users.domain.Member;
import com.soma.lecture.users.domain.repository.MemberRepository;
import com.soma.lecture.users.service.response.UserLoginResponse;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 15;
    private static final Pattern VALID_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$");

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(final MemberRequest request) {
        checkDuplicated(request.email());
        String encodedPassword = generateEncodedPassword(request.password());
        Member member = new Member(request.email(), encodedPassword);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public UserLoginResponse login(final MemberRequest request) {
        Member member = validateInfo(request.email(), request.password());
        return new UserLoginResponse(member.getUserUuid(), member.getRole());
    }

    private void checkDuplicated(final String email) {
        boolean exists = memberRepository.existsByEmail(email);
        if (exists) {
            throw new ConflictException(ErrorCode.MEMBER_CONFLICT);
        }
    }

    private String generateEncodedPassword(final String rawPassword) {
        validatePassword(rawPassword);
        return passwordEncoder.encode(rawPassword);
    }

    private void validatePassword(final String rawPassword) {
        if (rawPassword.length() < PASSWORD_MIN_LENGTH || rawPassword.length() > PASSWORD_MAX_LENGTH) {
            throw new BadRequestException(ErrorCode.PASSWORD_LENGTH_EXCEPTION);
        }

        if (!VALID_PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new BadRequestException(ErrorCode.PASSWORD_FORMAT_EXCEPTION);
        }
    }

    private Member validateInfo(final String email, final String password) {
        Member member = findMemberByEmail(email);
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new UnauthorizedException(ErrorCode.PASSWORD_INVALID);
        }
        return member;
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOTFOUND));
    }
}
