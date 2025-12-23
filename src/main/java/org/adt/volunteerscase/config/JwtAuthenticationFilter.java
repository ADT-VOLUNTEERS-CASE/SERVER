package org.adt.volunteerscase.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Authenticates requests carrying a Bearer JWT and populates the SecurityContext when the token is valid.
     *
     * <p>If the request contains an Authorization header starting with "Bearer ", the filter extracts the token,
     * derives the username, loads corresponding user details, and—if the token is valid—sets an authenticated
     * UsernamePasswordAuthenticationToken on the SecurityContext. The request is always forwarded to the remaining filter chain.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    )
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");//Получение заголовка
        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);//Вырезаю "Bearer " для получения токена
        userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){ //если почта не пуста и аутентификация не проведена ма получаем данные о пользователе из данных
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)){ //если токен валидный, то создается объект в который передаются данные пользователя,учетные данные и полномочия.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}