package top.leeys.interceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import top.leeys.domain.User;
import top.leeys.service.UserService;

/**
 * websocket 握手拦截器
 * 
 * @author leeys.top@gmail.com
 */
public class MyHandshakeInterceptor implements HandshakeInterceptor{
    
    @Autowired UserService userService;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null) {
                User user = userService.findByToken(token);
                if (user != null) {
                    attributes.put("user", user);
                    System.out.println("用户连接 uuid : " + user.getUuid() + " " + user.getUsername());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {
    }

}
