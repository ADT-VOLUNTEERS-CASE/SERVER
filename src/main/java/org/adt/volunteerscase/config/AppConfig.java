package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository repository;

    /**
     * Provide a UserDetailsService that loads a user by email and adapts it to Spring Security's UserDetails.
     *
     * @return a UserDetailsService which looks up a UserEntity by the provided username (email) and returns a UserDetailsImpl constructed from the user and its authentication data
     * @throws UsernameNotFoundException if no user is found for the given username or if the user's authentication data is missing
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity user = repository.findByEmailWithAuth(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (user.getUserAuth() == null) {
                throw new UsernameNotFoundException("User authentication data not found");
            }

            return new UserDetailsImpl(user, user.getUserAuth());
        };
    }

    /**
     * Creates a PasswordEncoder bean that uses the BCrypt algorithm for hashing passwords.
     *
     * @return a {@link PasswordEncoder} implementation backed by BCrypt for secure password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates and configures a DaoAuthenticationProvider that uses the application's
     * UserDetailsService and PasswordEncoder.
     *
     * @return the configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    /**
     * Retrieve the application's AuthenticationManager from the provided AuthenticationConfiguration.
     *
     * @param configuration the AuthenticationConfiguration used to obtain the AuthenticationManager
     * @return the configured AuthenticationManager
     * @throws Exception if the AuthenticationManager cannot be created or retrieved from the configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}