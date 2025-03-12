package mvp.deplog.global.security;

import lombok.RequiredArgsConstructor;
import mvp.deplog.domain.member.domain.repository.MemberRepository;
import mvp.deplog.global.security.AuthenticationEntryPointImpl;
import mvp.deplog.global.security.UserDetailsServiceImpl;
import mvp.deplog.global.security.filter.JwtAuthenticationProcessingFilter;
import mvp.deplog.global.security.jwt.JwtTokenProvider;
import mvp.deplog.infrastructure.redis.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    private static final String[] WHITE_LIST = {
            "/swagger", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/auth/**", "/auth/password", "/auth/reissue",
            "/comments/**",
            "/mails/**", "/verification-email",
            "/posts/**",
            "/images/**",
            "/test",
            "/actuator/**"
    };

    private static final String[] NEED_TOKEN = {
            "/likes/**",
            "/members/**",
            "/scraps/**",
            "/posts/{postId:[0-9]+}", "/posts/temps", "/posts", "/posts/uploadImages", "/posts/temps", "/posts/publishing/{postId:[0-9]+}", "/posts/edits/{postId:[0-9]+}", "/posts/temps/details/{postId:[0-9]+}"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new AuthenticationEntryPointImpl()))

                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/auth/logout").authenticated()
                                .requestMatchers(NEED_TOKEN).hasAnyRole("ADMIN", "MEMBER")
                                .requestMatchers(WHITE_LIST).permitAll()
//                                .requestMatchers("/test").hasAnyRole("ADMIN", "MEMBER")
                .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationProcessingFilter(), LogoutFilter.class)
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter(){
        return new JwtAuthenticationProcessingFilter(jwtTokenProvider, memberRepository, redisUtil);
    }
}
