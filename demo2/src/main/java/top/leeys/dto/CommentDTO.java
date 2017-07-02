package top.leeys.dto;

import java.util.List;

import lombok.Data;
import top.leeys.domain.Comment;

@Data
public class CommentDTO {
    private List<Comment> commentList;
    private int totalPages;
    private int size;
}
