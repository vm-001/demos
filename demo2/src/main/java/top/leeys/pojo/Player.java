package top.leeys.pojo;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.leeys.domain.User;

@Data @AllArgsConstructor
@JsonIgnoreProperties({"session", "partner"})
public class Player {
    
//    private String sessionId;
    
    private User user;
    
    private WebSocketSession session;
    
    private WebSocketSession partner;
    
}
