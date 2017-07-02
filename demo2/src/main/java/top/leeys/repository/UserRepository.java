package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.leeys.domain.User;

public interface UserRepository extends JpaRepository<User, String>{
    
    User findByUid(String uid);
    
    User findByToken(String token);
}
