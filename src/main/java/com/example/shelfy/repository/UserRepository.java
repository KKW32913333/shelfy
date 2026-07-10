package com.example.shelfy.repository;

import com.example.shelfy.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

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
        String sql = "SELECT * FROM app_user WHERE username = ?";
        return linkleJdbc.query(sql, userRowMapper, username)
                .stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM app_user WHERE id = ?";
        return linkleJdbc.query(sql, userRowMapper, id)
                .stream().findFirst();
    }
}
