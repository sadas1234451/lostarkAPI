package org.embed.repository;

import java.util.Optional;

import org.embed.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

// Admin Entity와 DB를 연결하는 인터페이스
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    // Spring Security가 로그인 시 사용할 메소드: username으로 Admin 정보를 조회
    Optional<Admin> findByUsername(String username);
}