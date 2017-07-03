package top.leeys.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import top.leeys.domain.User;

public interface UserRepository extends JpaRepository<User, String>{
    
    User findByEmail(String email);
    
    @Query(value = "select * from tb_user order by created_time limit ?1, ?2", nativeQuery = true)
    List<User> findByPosition(int offset, int limit);
}
