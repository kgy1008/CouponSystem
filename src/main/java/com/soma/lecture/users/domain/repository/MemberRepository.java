package com.soma.lecture.users.domain.repository;

import com.soma.lecture.users.domain.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM Member u WHERE u.email = :email) THEN true ELSE false END")
    boolean existsByEmail(@Param("email") String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUserUuid(UUID uuid);
}
