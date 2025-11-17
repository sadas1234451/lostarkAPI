package org.embed.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService; // ⭐ DB 인증을 위해 추가 ⭐
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CustomUserDetailsService
    
    // 5. 보안 필터 체인 설정 (기존과 동일)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
             // ... (authorizeHttpRequests, formLogin 등 나머지 로직은 그대로 유지)
            .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(
                        // URL 매칭
                        "/multi-search.html", 
                        "/api/multi-search",
                        "/mainHome",
                        "/character_Info",
                        "/market/**",
                        // "/api/multi-search" 중복됨
                        "/dbtest"
                    ).permitAll()
                    
                    .requestMatchers(HttpMethod.GET, "/api/notices").permitAll()
                    .requestMatchers("/**.css", "/**.js").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                    
                    .anyRequest().authenticated()
                )
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}