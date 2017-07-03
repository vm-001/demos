package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.leeys.domain.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer>{
    Admin findByUsername(String username);
    Admin findByToken(String token);
}
