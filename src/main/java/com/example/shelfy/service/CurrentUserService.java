package com.example.shelfy.service;

import com.example.shelfy.model.User;
import com.example.shelfy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final JdbcTemplate linkleJdbc;

    public CurrentUserService(UserRepository userRepository,
                              @Qualifier("linkleJdbcTemplate") JdbcTemplate linkleJdbc) {
        this.userRepository = userRepository;
        this.linkleJdbc = linkleJdbc;
    }

    private static final String SESSION_GROUP_ID = "currentGroupId";

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Long getCurrentGroupId() {
        HttpSession session = getSession();
        Long groupId = (Long) session.getAttribute(SESSION_GROUP_ID);
        if (groupId == null) {
            groupId = autoDetectGroupId();
            if (groupId != null) {
                session.setAttribute(SESSION_GROUP_ID, groupId);
            } else {
                throw new IllegalStateException("グループが選択されていません");
            }
        }
        return groupId;
    }

    public void setCurrentGroupId(Long groupId) {
        getSession().setAttribute(SESSION_GROUP_ID, groupId);
    }

    private Long autoDetectGroupId() {
        try {
            Long userId = getCurrentUserId();
            List<Map<String, Object>> rows = linkleJdbc.queryForList(
                "SELECT group_id FROM group_membership WHERE user_id = ? LIMIT 1", userId);
            if (rows.size() > 0) {
                return ((Number) rows.get(0).get("group_id")).longValue();
            }
            List<Map<String, Object>> userRows = linkleJdbc.queryForList(
                "SELECT active_group_id FROM app_user WHERE id = ?", userId);
            if (userRows.size() > 0 && userRows.get(0).get("active_group_id") != null) {
                return ((Number) userRows.get(0).get("active_group_id")).longValue();
            }
        } catch (Exception e) {
            // 取得失敗時はnullを返す
        }
        return null;
    }

    private HttpSession getSession() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession(true);
    }
}
