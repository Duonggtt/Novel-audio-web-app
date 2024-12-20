package com.spring3.oauth.jwt.configurations;

import com.spring3.oauth.jwt.helpers.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Value("${ALLOW_ORIGIN}")
    private String allowedOrigins;

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsServiceImpl();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeRequests(authorize -> authorize
                .requestMatchers("/api/v1/test", "/api/v1/save", "/api/v1/login", "/api/v1/refreshToken", "/api/v1/coins/packages", "/api/v1/subs/packages",
                    "/api/v1/forgot-password", "api/v1/reset-password", "/api/v1/verify-otp",
                    "/api/v1/genres/", "/api/v1/select-genres", "/api/v1/novels/**",
                    "/api/v1/bxh/top-read", "api/v1/bxh/top-point", "/api/v1/novels/bxh/top-read",
                        "/api/v1/mail-role/acceptRedirect", "/api/v1/mail-role/declineRedirect", "/api/v1/mail-role/accept", "/api/v1/mail-role/decline",
                        "api/v1/send-update-role-email", "/api/v1/payment/callback", "/api/v1/payment/packages", "/api/v1/payment/callback-html")
                .permitAll()

                .requestMatchers("/api/v1/audio-files/**","/api/v1/authors/**", "/api/v1/chapters/**",
                    "/api/v1/comments/**", "/api/v1/genres/**", "/api/v1/users/**",
                    "/api/v1/liked-novels/**", "/api/v1/rates/**", "/api/v1/reading-library/**",
                    "/api/v1/reading-progress/**", "/api/v1/images/**", "/api/v1/**",
                    "/api/v1/notifications/**", "/api/v1/novels/save", "/api/v1/novels/like-count-update/**",
                    "/api/v1/novels/like/**", "/api/v1/novels/update/**")
                .authenticated())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
