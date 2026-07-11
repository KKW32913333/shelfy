package com.example.shelfy.repository;

import com.example.shelfy.model.ConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ConsumptionLogRepository extends JpaRepository<ConsumptionLog, Long> {
    List<ConsumptionLog> findByGroupIdOrderByConsumedAtDesc(Long groupId, Pageable pageable);
    List<ConsumptionLog> findByGroupIdOrderByConsumedAtDesc(Long groupId);
}
