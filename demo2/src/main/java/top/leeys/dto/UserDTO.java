package top.leeys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import top.leeys.domain.User;

@Data @NoArgsConstructor
public class UserDTO {
    
    private User user;
    
    /* 从record表计算出来 */
    private Integer playNum;     //游戏局数
    private Integer totalScore;  //总成绩
    private Integer rank;        //总成绩排名
    public UserDTO(User user) {
        this.user = user;
    }
}

