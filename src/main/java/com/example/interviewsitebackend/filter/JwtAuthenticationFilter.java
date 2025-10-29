package com.example.interviewsitebackend.filter;

import com.example.interviewsitebackend.repo.UserRepository;
import com.example.interviewsitebackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ 1. Skip authentication for public endpoints
        String path = request.getServletPath();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/signup")){
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 2. Get token from Authorization header
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            System.out.println("⚠️ No JWT found in Authorization header for " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 3. Validate token and set authentication
        if (jwtUtil.isTokenValid(token)) {
            String email = jwtUtil.extractEmail(token);
            System.out.println("✅ Valid token for user: " + email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userEntity = userRepository.findByEmail(email).orElse(null);

                if (userEntity != null) {
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().toString().toUpperCase())
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userEntity, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("🔐 Authenticated user: " + email);
                }
            }
        } else {
            System.out.println("❌ Invalid or expired token");
        }

        // ✅ 4. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
