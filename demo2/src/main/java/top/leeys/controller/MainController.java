package top.leeys.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.RateLimited;
import top.leeys.annotation.Token;
import top.leeys.config.AppConfig;
import top.leeys.config.ErrorCode;
import top.leeys.domain.Comment;
import top.leeys.domain.Record;
import top.leeys.domain.User;
import top.leeys.dto.CommentDTO;
import top.leeys.dto.RankDTO;
import top.leeys.dto.UserDTO;
import top.leeys.exception.BaseException;
import top.leeys.pojo.AccessToken;
import top.leeys.pojo.Message;
import top.leeys.pojo.Player;
import top.leeys.service.CommentService;
import top.leeys.service.RecordService;
import top.leeys.service.UserService;


@RestController
@Slf4j
public class MainController {
    
    @Autowired SocketHandler socket;
    @Autowired ObjectMapper mapper;
    @Autowired RestTemplate rest;
    
    @Autowired UserService userService;
    @Autowired CommentService commentService;
    @Autowired RecordService recordService;
    
    /**
     * 获取当前所有在线玩家
     */
    @GetMapping("/online")
    public ResponseEntity<Message> getOnlineUser() {
        Collection<Player> users = socket.getOnlineUser();
        return new ResponseEntity<>(new Message(0, "success", users), HttpStatus.OK);
    }
    
    /**
     * 邀请其他玩家
     */
    @Token
    @PostMapping("/invite/{uuid}")
    public ResponseEntity<Message> invite(
            @PathVariable("uuid") String uuid,
            @RequestAttribute("user") User user) {
        try {
            socket.sendInvite(user, uuid);
            return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new Message(ErrorCode.USER_DISCONNECTED, "用户已经离线"), HttpStatus.OK);
    }
    
    @Token
    @PostMapping("/confirm/{uuid}")
    public ResponseEntity<Message> confirm(
            @PathVariable("uuid") String uuid,
            @RequestAttribute("user") User user) {
        try {
            socket.confirm(user, uuid);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BaseException e) {
            return new ResponseEntity<>(new Message(e.code(), e.message()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
    }
    
    @GetMapping("/ranking")
    @ResponseBody
    public ResponseEntity<Message> getRanking(
            @RequestParam(value = "type", required = false, defaultValue = "1") String type) {
        
        //总成绩 单局成绩 平均成绩 游戏局数
//        switch (type) {
//        case "1":
//            break;
//        case "2":
//            break;
//        case "3":
//            break;
//        case "4":
//            break;
//        default:
//            break;
//        }
        List<RankDTO> rankingList = recordService.getRankingInfo();
        return new ResponseEntity<>(new Message(0, "success", rankingList), HttpStatus.OK);
    }
    
    public Message buildMessage(Integer code, String message, Object content) {
        Message msg = new Message();
        if (content == null) {
            msg.setMsg(code, message);
        } else {
            msg.setMsg(code, message, content);
        }
        return msg;
    }
    
    @GetMapping("/comment")
    public ResponseEntity<Message> getCommentList(
            @RequestParam(value = "page", required = false, defaultValue = "1") 
            Integer page) {
        log.debug(page.toString());
        CommentDTO commentDTO = commentService.getComment(page);
        return new ResponseEntity<>(new Message(0, "success", commentDTO), HttpStatus.OK);
    }
    
    @Token
    @PostMapping("/comment")
    public ResponseEntity<Message> createComment(
            @RequestParam("content") String content,
            @RequestAttribute("user") User user) {
        System.out.println(content);
        Comment comment = new Comment(null, user, new Date(), content);
        commentService.saveComment(comment);
        return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
    }
    
    @Token
    @RateLimited(value = 100)
    @PostMapping("/record")
    public ResponseEntity<Message> createRecord(
            @RequestParam("score") int score,
            @RequestParam(name = "type", required = false, defaultValue = "1") int type , //1单人，2双人
            @RequestAttribute("user") User user) {
        if (type != 1 && type != 2) {
            System.out.println(type);
            throw new BaseException(ErrorCode.PARAMS_ERROR, "params error");
        }
        //250分 上限?
        if (score > 250) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "params error");
        }
        if (type == 1) {
            Record record = new Record(null, user, score, type, new Date());
            recordService.save(record);
        } else if (type == 2) {
            score = score / 2;
            Record record = new Record(null, user, score, type, new Date());
            recordService.save(record);
            User partner = socket.getPartner(user.getUuid());
            if (partner != null) {
                Record record2 = new Record(null, partner, score, type, new Date());
                recordService.save(record2);
            }
        }
        return new ResponseEntity<>(new Message(0, "success"), HttpStatus.OK);
    }
    
    @GetMapping("/user/{uuid}")
    public ResponseEntity<Message> getUserInfo(
            @PathVariable("uuid") String uuid) {
        Message message = new Message();
        UserDTO userDTO = userService.getUserInfo(uuid);
        if (userDTO == null) {
            log.info("user {} not found", uuid);
            message.setMsg(ErrorCode.USER_NOT_FOUND, "not found");
        } else {            
            message.setMsg(0, "success", userDTO);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
    
    /**
     * 新浪登录的回调地址, 用于获取code, 再根据code向新浪换取access_token
     * 仅考虑微博登录, type在整个项目没用到
     */
    @GetMapping(value = "/oauth/{type}/callback")
    public void callback(
            @PathVariable("type") String type,
            @RequestParam("code") String code,
            HttpServletResponse response) throws JsonParseException, JsonMappingException, IOException {
        PrintWriter out =  response.getWriter();
        System.out.println("code" + code);
        //根据code换取access_token
        String atStr = null;
        try {
            atStr = rest.postForObject(AppConfig.OAuth.Weibo.ACCESS_TOKEN_URL, null, String.class, 
                    AppConfig.OAuth.Weibo.APP_KEY, AppConfig.OAuth.Weibo.APP_SECRET, AppConfig.OAuth.REDIRECT_URL, code);
        } catch (HttpClientErrorException e) {
            //code不合法
            System.out.println(e);
            String script = "<script type=\"text/javascript\"  charset=\"UTF-8\">(function() {alert('参数异常!');window.location = 'http://' + window.location.host + /demo2/;})();</script>";
            out.print(script);
            out.flush();
            out.close();
            return;
        }
        AccessToken result = mapper.readValue(atStr, AccessToken.class);
        User user = userService.findByUid(result.getUid());
        if (user == null) { 
            //表示新用户，进行保存操作
            user = userService.save(result);
        }
        //客户端使用localStorage存储token
        String script = "<script type=\"text/javascript\">(function() {window.localStorage.setItem('demo2_key', JSON.stringify({'uuid': '" + 
                user.getUuid() + "', 'token':'" + user.getToken() + "'}));window.location = 'http://' + window.location.host + /demo2/;})();</script>";
        out.print(script);
        out.flush();
        out.close();
    }
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        AccessToken s = new ObjectMapper().readValue("{\"access_token\":\"2.00PPebXG09j1m_cc6e2517e3U9n7lD\",\"remind_in\":\"157679999\",\"expires_in\":157679999,\"uid\":\"5993388921\"}", AccessToken.class);
        System.out.println(s);
    }
    
}
