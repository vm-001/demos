package top.leeys.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import top.leeys.domain.Comment;
import top.leeys.dto.CommentDTO;
import top.leeys.repository.CommentRepository;

@Service
public class CommentService {
	@Autowired CommentRepository commentRepository; 
    
    public CommentDTO getComment(int pageNo) {
        CommentDTO commentDTO =  new CommentDTO();
        pageNo -= 1;  //下表-1
        int pageSize = 10;
        Order order = new Order(Direction.DESC, "id");
        Sort sort = new Sort(order);
        PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
        
        Page<Comment> page = commentRepository.findAll(pageable);
        
        commentDTO.setSize(page.getSize());
        commentDTO.setTotalPages(page.getTotalPages());
        commentDTO.setCommentList(page.getContent());
        return commentDTO;
    }
    
    /**
     * 保存评论
     */
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }
}
