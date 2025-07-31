package com.plantalk.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**"))  // API 요청에만 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                // 공개 리소스 설정
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/img/**").permitAll()
                // API 접근 허용 (개발 중에는 편의를 위해, 실제 프로덕션에서는 인증 필요)
                .requestMatchers("/api/**").permitAll()
                // 인증이 필요한 페이지 설정
                .requestMatchers("/plant-list", "/plant-register", "/plant-edit", "/chat/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")  // 로그인 처리 URL 명시적 설정
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")  // 로그인 실패 시 URL
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
