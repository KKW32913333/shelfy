package com.example.shelfy.repository;

import com.example.shelfy.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);
    void deleteByEndpoint(String endpoint);
    boolean existsByEndpoint(String endpoint);
}
