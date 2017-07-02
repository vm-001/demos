package top.leeys.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import top.leeys.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer>{



}
