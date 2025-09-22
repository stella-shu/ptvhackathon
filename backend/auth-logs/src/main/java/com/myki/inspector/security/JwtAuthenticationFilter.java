package com.myki.inspector.security;

import com.myki.inspector.entity.Inspector;
import com.myki.inspector.repository.InspectorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final InspectorRepository inspectorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String inspectorId;
        try {
            inspectorId = jwtService.extractClaim(token, claims -> claims.getSubject());
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (inspectorId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Inspector> inspectorOpt = inspectorRepository.findByInspectorId(inspectorId);
            if (inspectorOpt.isPresent() && jwtService.isTokenValid(token, inspectorId)) {
                UserDetails userDetails = User
                        .withUsername(inspectorId)
                        .password("N/A")
                        .authorities("ROLE_INSPECTOR")
                        .build();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(), null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}

