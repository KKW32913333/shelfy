package com.example.shelfy.repository;

import com.example.shelfy.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private final JdbcTemplate linkleJdbc;

    public UserRepository(@Qualifier("linkleJdbcTemplate") JdbcTemplate linkleJdbc) {
        this.linkleJdbc = linkleJdbc;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password_hash"));
        u.setApproved(rs.getBoolean("approved"));
        u.setAdmin(rs.getBoolean("admin"));
        u.setRejected(rs.getBoolean("rejected"));
        return u;
    };

    public Optional<User> findByUsername(String username) {
        try {
            String db = linkleJdbc.queryForObject("SELECT current_database()", String.class);
            Long count = linkleJdbc.queryForObject("SELECT COUNT(*) FROM app_user", Long.class);
            log.info("DB={} app_user件数={}", db, count);
        } catch (Exception e) {
            log.error("DB確認エラー: {}", e.getMessage());
        }
        String sql = "SELECT * FROM app_user WHERE username = ?";
        List<User> users = linkleJdbc.query(sql, userRowMapper, username);
        log.info("findByUsername({}) 結果件数={}", username, users.size());
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM app_user WHERE id = ?";
        return linkleJdbc.query(sql, userRowMapper, id)
                .stream().findFirst();
    }
}
