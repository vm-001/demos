package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import top.leeys.domain.Record;

public interface RecordRepository extends JpaRepository<Record, Integer> {

    // TODO 总成绩排名前10

    // TODO 游戏局数排名前10

    // TODO 平均成绩排名前10

    @Query(value = "SELECT COUNT(id) FROM tb_record WHERE user_uuid = ?1 AND created_time BETWEEN curdate() AND date_add(CURDATE(), INTERVAL 1 DAY)", nativeQuery = true)
    int getTodayPlayNum(String uuid);
}
