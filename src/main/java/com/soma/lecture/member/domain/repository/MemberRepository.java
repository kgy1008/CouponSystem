package com.soma.lecture.member.domain.repository;

import com.soma.lecture.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
