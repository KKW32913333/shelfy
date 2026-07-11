package com.example.shelfy.controller;

import com.example.shelfy.model.PushSubscription;
import com.example.shelfy.repository.PushSubscriptionRepository;
import com.example.shelfy.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PushNotificationController {

    private final PushSubscriptionRepository subscriptionRepository;
    private final CurrentUserService currentUserService;

    @PostMapping("/api/push/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> body) {
        try {
            Long userId = currentUserService.getCurrentUserId();
            String endpoint = (String) body.get("endpoint");

            if (subscriptionRepository.existsByEndpoint(endpoint)) {
                return ResponseEntity.ok(Map.of("status", "already_subscribed"));
            }

            Map<String, String> keys = (Map<String, String>) body.get("keys");

            PushSubscription sub = new PushSubscription();
            sub.setUserId(userId);
            sub.setEndpoint(endpoint);
            sub.setP256dh(keys.get("p256dh"));
            sub.setAuth(keys.get("auth"));
            subscriptionRepository.save(sub);

            return ResponseEntity.ok(Map.of("status", "subscribed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/push/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> body) {
        subscriptionRepository.deleteByEndpoint(body.get("endpoint"));
        return ResponseEntity.ok(Map.of("status", "unsubscribed"));
    }
}
