package com.example.interviewsitebackend.config;

import com.example.interviewsitebackend.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Disable CSRF because we are using JWT
                .csrf(csrf -> csrf.disable())

                // ✅ Enable our CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()  // login/register allowed
                        .anyRequest().authenticated()
                )

                // ✅ Add JWT filter before Spring Security’s auth filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ This allows cookies to be included in responses
                .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🛑 CRITICAL CHANGE 1: Use setAllowedOrigins for specific production URLs
        // Replace 'interviewsite-frontend.vercel.app' with your actual Vercel domain.
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://interviewsite-frontend.vercel.app" // 🎯 Use HTTPS for Vercel
        ));
        // configuration.setAllowedOriginPatterns(List.of(...)); // Remove this if using setAllowedOrigins

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ CRITICAL CHANGE 2: Keep this, as it allows cookies (credentials) to be sent
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        // You can remove this unless you actually use WebSockets.
        // source.registerCorsConfiguration("/ws/**", configuration);
        return source;
    }

}
