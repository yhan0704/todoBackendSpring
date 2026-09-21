package backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(allowedOrigin);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 이 프로젝트가 세션이 아닌 JWT로 인증하기 때문에 Spring Security의 기본 동작들을 대부분
    // 꺼야 함. 각 설정이 "왜" 필요한지:
    // - csrf.disable(): CSRF는 브라우저가 쿠키/세션을 자동으로 실어 보내는 걸 노리는 공격이라
    //   서버에 세션이 없는 stateless API에는 해당 위협이 없음
    // - formLogin.disable(): Spring Security 기본 로그인 폼/리다이렉트를 끄고, 우리가 만든
    //   /auth/login(AuthService)으로만 로그인하게 함
    // - authorizeHttpRequests: /auth/**(로그인, 회원가입, 토큰 재발급)는 아직 토큰이 없는
    //   상태에서 호출돼야 하니 permitAll, 그 외 모든 요청은 인증(토큰 검증)을 요구
    // - exceptionHandling: 기본값(Http403ForbiddenEntryPoint)을 쓰면 토큰이 없거나 만료됐을 뿐인
    //   요청도(=401이어야 함) 403 + 빈 바디로 응답돼서 프론트의 401 기반 refresh 로직이 동작하지
    //   않았음. CustomAuthenticationEntryPoint로 그런 경우를 401로 통일함
    // - addFilterBefore: 요청이 UsernamePasswordAuthenticationFilter(폼 로그인용 기본 필터)에
    //   닿기 전에 JwtAuthenticationFilter를 먼저 태워서, 헤더의 토큰을 검증하고
    //   SecurityContext에 인증 정보를 채워 넣음. 이게 있어야 authorizeHttpRequests의
    //   authenticated() 판단이 실제로 "유효한 토큰이 있는가"를 기준으로 동작함
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
