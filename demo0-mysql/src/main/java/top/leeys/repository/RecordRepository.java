package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import top.leeys.domain.Record;


public interface RecordRepository extends JpaRepository<Record, String>{
} 
