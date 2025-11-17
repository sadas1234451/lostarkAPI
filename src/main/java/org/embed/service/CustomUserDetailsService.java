package org.embed.service;

import java.util.Collection;
import java.util.Collections;

import org.embed.entity.Admin;
import org.embed.repository.AdminRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
//사용자 인증 클래스
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                                        .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없음" + username));
        
        return  new org.springframework.security.core.userdetails.User(
            admin.getUsername(),
            admin.getPassword(),
            getAuthorities(admin.getRole())
        );
        }
    
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
    // DB에서 가져온 'ROLE_ADMIN' 문자열을 Spring Security 객체로 변환
    if (!role.startsWith("ROLE_")) {
        role = "ROLE_" + role;
    }
    return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
    
}
