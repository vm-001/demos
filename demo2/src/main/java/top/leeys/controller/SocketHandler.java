package top.leeys.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import top.leeys.config.ErrorCode;
import top.leeys.domain.User;
import top.leeys.exception.BaseException;
import top.leeys.pojo.Player;

/**
 * socket处理器
 * 
 * @author leeys.top@gmail.com
 */
public class SocketHandler extends AbstractWebSocketHandler{
    @Autowired ObjectMapper mapper;
    
    private Map<String, Player> playerMap = new HashMap<>();
    
    public Collection<Player> getOnlineUser() {
        return playerMap.values();
    }
    
    public User getPartner(String uuid) {
        Player p = playerMap.get(uuid);
        if (p != null) {
            WebSocketSession partnerSession = p.getPartner();
            if (partnerSession != null) {
                return (User)partnerSession.getAttributes().get("user");
            }
        }
        return null;
    }
    
    /**
     * 主邀方向被邀方发起请求
     * @param inviterUser 主邀方
     * @param invitee     被邀方
     * @throws IOException
     */
    public void sendInvite(User inviterUser, String invitee) throws IOException {
        Player inviterP = playerMap.get(invitee);
        if (inviterP == null) {
            throw new BaseException(ErrorCode.USER_DISCONNECTED, "用户已经离线");
        } else {
            /**
                                            数据结构
                {
                    t: 0,
                    f: {uuid},
                    n: {name}
                }
             */
            String data = "{\"t\":0,\"f\":\"" + inviterUser.getUuid() + "\",\"n\":\"" + inviterUser.getUsername() +"\"}";
            
            inviterP.getSession().sendMessage(new TextMessage(data));
        }
    }
    
    /**
     * 被邀方确认主邀方的请求, 向主邀方发送被邀请方同意邀请的通知
     * @param inviteeUser 被邀方
     * @param inviter     主邀请
     * @throws IOException
     */
    public void confirm(User inviteeUser, String inviter) throws IOException {
        binding(inviter, inviteeUser.getUuid());
        /**
                                  数据结构
            {
                t: 1,
                n: {name}
            }
         */
        WebSocketSession inviterSession =  playerMap.get(inviter).getSession();
        String data = "{\"t\":1,\"n\":\"" + inviteeUser.getUsername() + "\"}";
        inviterSession.sendMessage(new TextMessage(data));
    }
    
    /**
     * 绑定双方数据
     */
    public void binding(String inviter, String invitee) {
        Player inviterP = playerMap.get(inviter);
        Player inviteeP = playerMap.get(invitee);
        if (inviterP == null || inviteeP == null) {
            throw new BaseException(ErrorCode.USER_DISCONNECTED, "用户已经离线");
        }
        //主邀方已经跟别人在游戏
        if (inviterP.getPartner() != null) {
            throw new BaseException(ErrorCode.INVITER_IN_GAME, "inviter in game");
        }
        inviterP.setPartner(inviteeP.getSession());
        inviteeP.setPartner(inviterP.getSession());
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        //对客户端的ping进行响应
        session.sendMessage(new BinaryMessage(new byte[1]));
    }
    
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        //向搭档发送数据
        WebSocketSession partner = playerMap.get(user.getUuid()).getPartner();
        if (partner != null)
            partner.sendMessage(message);
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = (User) session.getAttributes().get("user");
        playerMap.put(user.getUuid() , new Player(user, session, null));
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        System.out.println("connection close：" + user.getUuid() + " " + status);
        //向搭档发起通知
        WebSocketSession partnerSession = playerMap.get(user.getUuid()).getPartner();
        if (partnerSession != null) {
            User partnerUser = (User) partnerSession.getAttributes().get("user");
            partnerSession.sendMessage(new BinaryMessage(new byte[2])); //2字节的二进制数据表示搭档掉线
            playerMap.get(partnerUser.getUuid()).setPartner(null);  //取消对方设置的同伴
        }
        playerMap.remove(user.getUuid());
    }
    
}
