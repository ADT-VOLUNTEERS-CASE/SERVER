package org.adt.volunteerscase.service;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.entity.user.UserAuthEntity;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.repository.UserAuthRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserAuthEntity userAuth = userAuthRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Authentication data not found for user: " + email));

        return UserDetailsImpl.builder()
                .user(user)
                .userAuth(userAuth)
                .build();
    }
}
