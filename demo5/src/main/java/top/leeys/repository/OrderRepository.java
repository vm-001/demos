package top.leeys.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import top.leeys.domain.Order;

public interface OrderRepository extends JpaRepository<Order, String>{

    @Query(value = "select * from tb_order order by created_time desc limit ?1, ?2", nativeQuery = true)
    List<Order> findByPosition(int offset, int limit);
}
