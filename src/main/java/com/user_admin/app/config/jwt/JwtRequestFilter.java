package com.user_admin.app.config.jwt;

import com.user_admin.app.model.AuthToken;
import com.user_admin.app.repository.AuthTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JWT request filter for validating JWT tokens in incoming requests.
 * This filter extracts the JWT from the request header, validates it,
 * and sets the authentication in the security context if valid.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthTokenRepository authTokenRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();


    /**
     * Constructs a JwtRequestFilter with the specified dependencies.
     *
     * @param userDetailsService  the services to load user-specific data
     * @param jwtUtil             the utility class for JWT operations
     * @param authTokenRepository the repository for managing authentication tokens
     */
    @Autowired
    public JwtRequestFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil, AuthTokenRepository authTokenRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authTokenRepository = authTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Extract the Authorization header from the request
        final String authorizationHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        // Allow certain endpoints to be accessed without JWT validated
        if (requestURI.equals("/api/auth/login") ||
                pathMatcher.match("/api/reset-password/{token}/{email}/{jwtToken}", requestURI) ||
                pathMatcher.match("/api/activate-account/{activationToken}/{email}/{jwtToken}", requestURI)) {

            filterChain.doFilter(request, response); //Proceed with the filter chain
            return; // Exit the method
        }

        // Check if the Authorization header is present and starts with "Bearer "
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token is missing or invalid");
            return; // Exit the method
        }

        String email = null;
        String jwtToken = authorizationHeader.substring(7); // Extract the JWT token

        // Validate the JWT and handle potential exceptions
        try {
            email = jwtUtil.extractUsername(jwtToken); // Extract username (email) from the token
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return; // Exit the method
        } catch (SignatureException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return; // Exit the method
        } catch (MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return; // Exit the method
        }

        // If the email is valid and no authentication exists in the security context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email); // Load user details

            // Validate the JWT token against the suer details
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                Optional<AuthToken> authTokenOptional = authTokenRepository.findByToken(jwtToken);

                // Check if the JWT token exists in the repository
                if (authTokenOptional.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("JWT token not found");
                    return; // Exit the method
                } else {
                    AuthToken authToken = authTokenOptional.get();

                    // Check if the token has expired
                    if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("JWT token has expired");
                        return; // Exit the method
                    }

                    // Create an authentication token and set it in the security context
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    // Update the last used time for the token
                    authToken.setLastUsedAt(LocalDateTime.now());
                    authTokenRepository.save(authToken); // Save the updated token information
                }
            }
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}
