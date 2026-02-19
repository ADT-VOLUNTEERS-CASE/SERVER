package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures and returns the application's security filter chain.
     * <p>
     * Configured behavior:
     * - CSRF protection disabled.
     * - Public access allowed for authentication and API documentation endpoints.
     * - All other requests require authentication.
     * - Session management set to STATELESS.
     * - Uses the configured AuthenticationProvider and inserts the JWT authentication filter
     * before the UsernamePasswordAuthenticationFilter.
     *
     * @param http the HttpSecurity instance to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs while building the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers("/api/v1/auth/register",
                                        "/api/v1/auth/authenticate",
                                        "/api/v1/auth/refreshtoken",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/docs",
                                        "/api/v1/ping"
                                )
                                .permitAll()
                                .requestMatchers("/api/v1/adminping", "/api/v1/auth/register/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/v1/coordinatorping", "/api/v1/cover/create", "/api/v1/tag/create", "/api/v1/event/update/**", "/api/v1/event/create", "/api/v1/event/delete/**").hasAuthority("ROLE_COORDINATOR")
                                .requestMatchers("/api/v1/location/create", "/api/v1/location/update/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDINATOR")

                                .requestMatchers(HttpMethod.GET, "/api/v1/tag/id/**", "/api/v1/tag/name/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/tag/id/**", "/api/v1/tag/name/**").hasAuthority("ROLE_COORDINATOR")
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/tag/**").hasAuthority("ROLE_COORDINATOR")

                                .requestMatchers(HttpMethod.GET, "/api/v1/cover/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/cover/**").hasAuthority("ROLE_COORDINATOR")
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/cover/**").hasAuthority("ROLE_COORDINATOR")

                                .requestMatchers(HttpMethod.GET, "/api/v1/user/me").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/user/coordinator/**").hasAuthority("ROLE_COORDINATOR")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}