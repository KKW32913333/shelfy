package com.example.shelfy.service;

import com.example.shelfy.model.User;
import com.example.shelfy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

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
            throw new IllegalStateException("グループが選択されていません");
        }
        return groupId;
    }

    public void setCurrentGroupId(Long groupId) {
        getSession().setAttribute(SESSION_GROUP_ID, groupId);
    }

    private HttpSession getSession() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession(true);
    }
}
