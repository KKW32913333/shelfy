package com.example.shelfy.repository;
import com.example.shelfy.model.ShelfyBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ShelfyBadgeRepository extends JpaRepository<ShelfyBadge, Long> {
    List<ShelfyBadge> findByGroupIdOrderByEarnedAtDesc(Long groupId);
    boolean existsByGroupIdAndBadgeKey(Long groupId, String badgeKey);
}
