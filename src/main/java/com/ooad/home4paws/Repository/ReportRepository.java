package com.ooad.home4paws.Repository;

import com.ooad.home4paws.Entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserId(Long userId);
}
