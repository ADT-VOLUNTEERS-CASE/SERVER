package org.adt.volunteerscase.entity.user;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Builder
public class UserDetailsImpl implements UserDetails {

    private final UserEntity user;
    private final UserAuthEntity userAuth;


    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (user != null && user.isAdmin()){
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (user != null && user.isCoordinator()){
            authorities.add(new SimpleGrantedAuthority("ROLE_COORDINATOR"));
        }

        return authorities;
    }

    /**
     * Provides the stored password hash for this user.
     *
     * @return the user's password hash
     */
    @Override
    public String getPassword() {
        if (userAuth == null) {
            throw new IllegalStateException("User cannot be null in UserDetails");
        }
        return userAuth.getPasswordHash();
    }

    /**
     * Provides the user's email address as the account username.
     *
     * @return the user's email address used as the username
     */
    @Override
    public @NonNull String getUsername() {
        if (user == null) {
            throw new IllegalStateException("User cannot be null in UserDetails");
        }
        return user.getEmail();
    }

    /**
     * Checks if the user has admin privileges.
     *
     * @return true if the user is an admin
     */
    public boolean isAdmin() {
        return user != null && user.isAdmin();
    }

    /**
     * Checks if the user has coordinator privileges.
     *
     * @return true if the user is a coordinator
     */
    public boolean isCoordinator() {
        return user != null && user.isCoordinator();
    }

    /**
     * Gets the user entity for additional information if needed.
     *
     * @return the UserEntity instance
     */
    public UserEntity getUser() {
        return user;
    }

    /**
     * Gets the user authentication entity.
     *
     * @return the UserAuthEntity instance
     */
    public UserAuthEntity getUserAuth() {
        return userAuth;
    }

    /**
     * Indicates whether the user account is considered non-expired.
     *
     * @return `true` if the account has not expired, `false` otherwise; this implementation always treats accounts as non-expired.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is not locked.
     *
     * <p>This implementation treats all accounts as not locked and always returns {@code true}.</p>
     *
     * @return {@code true} if the account is not locked; this implementation always returns {@code true}.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials are expired.
     *
     * @return `true` if the user's credentials are not expired, `false` otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is enabled.
     *
     * @return `true` if the user account is enabled, `false` otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}