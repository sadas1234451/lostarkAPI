package org.embed.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService; // ⭐ DB 인증을 위해 추가 ⭐
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // ⭐ 1. CustomUserDetailsService를 주입받기 위한 필드 ⭐
    private final UserDetailsService userDetailsService;

    // ⭐ 2. 생성자를 통해 CustomUserDetailsService (DB 인증 로직) 주입 ⭐
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // 3. 비밀번호 인코더 정의
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }

    // CustomUserDetailsService
    
    // 5. 보안 필터 체인 설정 (기존과 동일)
    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        
        .authorizeHttpRequests(authorize -> authorize
            // 1. 🛑 가장 먼저, 관리자만 접근 가능한 페이지를 설정합니다. 🛑
            // 아래 목록에 있는 URL들은 반드시 ROLE_ADMIN 권한이 있어야 접근 가능합니다.
            .requestMatchers(HttpMethod.POST, "/api/page/notice").hasAuthority("ROLE_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/page/notice/{id}").hasAuthority("ROLE_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/page/notice/{id}").hasAuthority("ROLE_ADMIN")
            
            // 2. 관리자 페이지 전체 (GET도 포함)는 관리자 권한 필수
            .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            
            // 2. 🟢 나머지 모든 요청은 인증 없이 접근 허용 (최종 정책) 🟢
            // 이 설정 위에 명시되지 않은 모든 URL은 누구나 접근 가능합니다.
            .anyRequest().permitAll() 
        )
        // ... formLogin 및 logout 설정은 그대로 유지 ...
        .formLogin(formLogin -> formLogin
            
            .defaultSuccessUrl("/mainHome")
            .failureUrl("/admin/login?error")
            .permitAll())
        .logout(logout -> logout.permitAll());

    return http.build();
}
}