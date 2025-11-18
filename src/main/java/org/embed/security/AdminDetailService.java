package org.embed.security; // ⬅️ 패키지 변경

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

@Service
public class AdminDetailService implements UserDetailsService {
    
    private final AdminRepository adminRepository;

    public AdminDetailService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                                    .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없음: " + username));
        
        return new org.springframework.security.core.userdetails.User(
            admin.getUsername(),
            admin.getPassword(),
            getAuthorities(admin.getRole())
        );
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}