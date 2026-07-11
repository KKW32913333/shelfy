package com.example.shelfy.repository;
import com.example.shelfy.model.ShelfyScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ShelfyScoreRepository extends JpaRepository<ShelfyScore, Long> {
    Optional<ShelfyScore> findByGroupId(Long groupId);
}
