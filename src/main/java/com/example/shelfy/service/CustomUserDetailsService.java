package com.example.shelfy.service;

import com.example.shelfy.model.User;
import com.example.shelfy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("★ログイン試行: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("★ユーザー未発見: {}", username);
                    return new UsernameNotFoundException("ユーザーが見つかりません: " + username);
                });

        log.info("★ユーザー発見: approved={} rejected={} hashPrefix={}",
                user.isApproved(), user.isRejected(),
                user.getPassword() != null ? user.getPassword().substring(0, 7) : "null");

        if (!user.isApproved()) {
            log.warn("★未承認: {}", username);
            throw new UsernameNotFoundException("承認待ちのユーザーです");
        }
        if (user.isRejected()) {
            log.warn("★拒否済: {}", username);
            throw new UsernameNotFoundException("アカウントが無効です");
        }

        var authorities = user.isAdmin()
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                          new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        log.info("★認証成功: {}", username);

        return new


cat > src/main/java/com/example/shelfy/service/CustomUserDetailsService.java << 'EOF'
package com.example.shelfy.service;

import com.example.shelfy.model.User;
import com.example.shelfy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("★ログイン試行: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("★ユーザー未発見: {}", username);
                    return new UsernameNotFoundException("ユーザーが見つかりません: " + username);
                });

        log.info("★ユーザー発見: approved={} rejected={} hashPrefix={}",
                user.isApproved(), user.isRejected(),
                user.getPassword() != null ? user.getPassword().substring(0, 7) : "null");

        if (!user.isApproved()) {
            log.warn("★未承認: {}", username);
            throw new UsernameNotFoundException("承認待ちのユーザーです");
        }
        if (user.isRejected()) {
            log.warn("★拒否済: {}", username);
            throw new UsernameNotFoundException("アカウントが無効です");
        }

        var authorities = user.isAdmin()
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                          new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        log.info("★認証成功: {}", username);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
