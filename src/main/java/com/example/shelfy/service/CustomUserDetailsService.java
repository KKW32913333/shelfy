package com.example.shelfy.service;

import com.example.shelfy.model.User;
import com.example.shelfy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("LOGIN_ATTEMPT: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("USER_NOT_FOUND: {}", username);
                    return new UsernameNotFoundException("not found: " + username);
                });
        log.info("USER_FOUND: approved={} rejected={}", user.isApproved(), user.isRejected());
        if (!user.isApproved()) {
            throw new UsernameNotFoundException("not approved");
        }
        if (user.isRejected()) {
            throw new UsernameNotFoundException("rejected");
        }
        var authorities = user.isAdmin()
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        log.info("AUTH_SUCCESS: {}", username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities);
    }
}
