package top.leeys.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.Admin;
import top.leeys.annotation.GeeTest;
import top.leeys.config.AppConfig;
import top.leeys.config.ErrorCode;
import top.leeys.domain.User;
import top.leeys.email.SignupEmail;
import top.leeys.pojo.CartItem;
import top.leeys.pojo.Data;
import top.leeys.pojo.Message;
import top.leeys.pojo.UserCreateForm;
import top.leeys.redis.Redis;
import top.leeys.util.CommonUtils;
import top.leeys.util.CustomValidator;

@RestController
@RequestMapping(value = "api/user", name = "用户接口")
@Slf4j
public class UserController extends BaseController{

    
    @GetMapping("/issignin")
    public ResponseEntity<Message> checkSignin(
            @SessionAttribute(required = false) User user) {
        Message message = user == null ? new Message(ErrorCode.UNKNOWN_ERROR, "未登录") : Message.ok();
        return ResponseEntity.ok(message);
    }
    
    /**
     * 返回userkey对应的购物车
     */
    @GetMapping("/cart")
    public ResponseEntity<Message> getCartItem(
            @SessionAttribute(name = "user", required = false) User user,
            @CookieValue(name = "user-key", required = false) String userkey) {
        if (user != null) {  //用户处于登录状态时，使用用户数据库的购物车id来获取购物车列表
            log.debug(user.toString());
            userkey = user.getCart(); 
        }
        List<CartItem> items = new ArrayList<>();
        Map<String, String> itemsMap = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        //以HashMap结构获取该cookie在redis中的购物车数据
        String key = "demo5.cart." + userkey;
        Redis.execute( jedis -> itemsMap.putAll(jedis.hgetAll(key)));
        //取出key并转换成Integer
        itemsMap.keySet().forEach(k -> ids.add(new Integer(k)));
        //一次SQL取出所有商品. 注意Map#get方法参数转化成String类型
        bookRepository.findAll(ids).forEach(
                book -> items.add(new CartItem(book, new Integer(itemsMap.get(book.getId() + "")))));
        return ResponseEntity.ok(Message.ok(items));
    }
    
    @PostMapping("/signin")
    public ResponseEntity<Message> signin(
            @RequestParam String username,
            @RequestParam String password,
            @CookieValue(name = "user-key") String userkey,
            HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            return ResponseEntity.ok(new Message(404, "用户名不存在"));
        } else if (!user.getPassword().equals(CommonUtils.encodePassword(password))) {
            return ResponseEntity.ok(new Message(ErrorCode.USERNAME_OR_PASSWORD_ERROR, "用户名或密码错误"));
        }
        
        if (user.getCart() == null) { //创建购物车
            user.setCart(userkey);
            userRepository.save(user);
        } else {  //合并购物车
            String newkey = "demo5.cart." + userkey;
            Redis.execute( jedis -> {
                jedis.hgetAll(newkey).forEach(
                        (k, v) -> jedis.hincrBy("demo5.cart." + user.getCart(), k, new Long(v)));
                jedis.del(newkey);  //删除
            });
        }
        user.setIp(request.getRemoteAddr());
        user.setLastSignin(new Date());
        userRepository.save(user);

        session.setAttribute("user", user);
        return ResponseEntity.ok(Message.ok());
    }
    
    @PostMapping("/signup")
    public ResponseEntity<Message> signup(
            @Valid @RequestBody UserCreateForm form,
            BindingResult result) {
        System.out.println(form);
        if (result.hasErrors()) {
            log.error("注册失败:" + result.getFieldError().getDefaultMessage());
            return ResponseEntity.ok(new Message(ErrorCode.PARAMS_ERROR, result.getFieldError().getDefaultMessage()));
        }
        User user = userRepository.findByEmail(form.getEmail());
        if (user != null) {
            return ResponseEntity.ok(new Message(ErrorCode.UNKNOWN_ERROR, "请勿重复注册"));
        }
        //检查redis数据是否一致
        String emailCode = Redis.get("demo5.email." + form.getEmail());
        if (emailCode == null) {
            return ResponseEntity.ok(new Message(ErrorCode.UNKNOWN_ERROR, "请重新注册"));  //1:redis数据过期，2:恶意注册
        } else if (!emailCode.equals(form.getEmailCode())) {
            return ResponseEntity.ok(new Message(ErrorCode.UNKNOWN_ERROR, "邮箱验证码不匹配"));
        }
        
        user = new User();
        user.setEmail(form.getEmail());
        user.setCreatedTime(new Date());
        user.setUsername(form.getName());
        user.setPassword(CommonUtils.encodePassword(form.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Message.ok());
    }
    
    @GeeTest
    @PostMapping(value = "/send/email/validate_code")
    public ResponseEntity<Message> sendEmail(
            @RequestParam String email) {
        if (!CustomValidator.isEmail(email)) {
            return ResponseEntity.ok(new Message(ErrorCode.EMAIL_ERROR, "邮箱格式错误"));
        }
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(new Message(ErrorCode.EMAIL_ALREADY_EXIST, "邮箱已被注册"));
        }
        
        /*异步发送邮件*/
        String emailCode = UUID.randomUUID().toString().replace("-", "");
        String url = AppConfig.HOST + AppConfig.CONTEXT_NAME + "/signup.html?email=" + email + "&code=" + emailCode;
        SignupEmail signupEmail = new SignupEmail(email, url);
        emailService.sendEmail(signupEmail);
        
        //存储邮箱验证码到redis
        Redis.execute(jedis -> jedis.setex("demo5.email." + email, 86400, emailCode));
        return ResponseEntity.ok(Message.ok());
    }
    
    /*=================管理员=================*/
    
    @Admin
    @GetMapping
    public ResponseEntity<Message> getUserList(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<User> userList = userRepository.findByPosition(offset, limit);
        Object data = new Data()
                .add("total", userRepository.count())
                .add("userList", userList)
                .data;
        return ResponseEntity.ok(Message.ok(data));
    }
    
    @Admin(true)
    @DeleteMapping("/user/{uuid}")
    public ResponseEntity<Message> deleteUser(
            @RequestParam String uuid) {
        User user = userRepository.findOne(uuid);
        if (user == null) {
//            return ResponseEntity.notFound().build();    //暂不严格执行rest风格
            return ResponseEntity.ok(Message.notFound());  
        }
        return ResponseEntity.ok(Message.ok());
    }
    
    @Admin(true)
    @PutMapping("/user/{uuid}")
    public ResponseEntity<Message> updateUser(
            @RequestBody User user) {
        userRepository.save(user);
        return ResponseEntity.ok(Message.ok());
    }
    
    
    /**
     * 管理员的登录
     */
    @PostMapping("/signin/admin")
    public ResponseEntity<Message> adminSignin(
            @RequestParam String username,
            @RequestParam String password) {
        log.debug("管理员登录: {}", username);
        top.leeys.domain.Admin admin = adminRepository.findByUsername(username);
        if (admin == null) {
            return ResponseEntity.ok(Message.notFound());
        }
        if (!admin.getPassword().equals(CommonUtils.encodePassword(password))) {
            return ResponseEntity.ok(new Message(ErrorCode.USERNAME_OR_PASSWORD_ERROR, "账号或密码错误")); 
        }
        Object data = new Data()
                .add("token", admin.getToken())
                .data;
        return ResponseEntity.ok(Message.ok(data));
    } 
}
