package top.leeys.dto;

import lombok.Data;
import top.leeys.domain.User;

@Data
public class RankDTO {
    
    private User user;
    
    private Integer totalScore;
}
