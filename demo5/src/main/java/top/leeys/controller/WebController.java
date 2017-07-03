package top.leeys.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import lombok.extern.slf4j.Slf4j;
import top.leeys.domain.Book;
import top.leeys.domain.Order;
import top.leeys.domain.User;
import top.leeys.redis.Redis;
import top.leeys.util.CommonUtils;

@Controller
@Slf4j
public class WebController extends BaseController{
    
    @GetMapping({"/", "/index"})
    public String index(
            Model model,
            @CookieValue(name = "user-key", required = false) String userkey,
            HttpSession session, HttpServletResponse response) throws ServletException, IOException {
        if (StringUtils.isEmpty(userkey)) {
            CommonUtils.setCookie(response, new Cookie("user-key", UUID.randomUUID().toString()));
        }
        
        //获取最新10本书
        List<Book> newBookList = bookRepository.findByPosition(0, 10);
        
        /*
         * 随机获取10本书, 因为使用SQL随机获取要么id不随机要么性能非常差
         * 为了防止删除图书后出现id空洞导致不够10本，先获取20本再取前10本
         */
        int num = new Integer(Redis.get("demo5.book.num"));
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ids.add((int)(Math.random() * num));
        }
        List<Book> randomBookList = bookRepository.findAll(ids);
        
        //获取畅销图书10本
        List<Book> hotBookList = bookRepository.getHotBook(10);
        
        model.addAttribute("newBookList", newBookList);
        model.addAttribute("randomBookList", randomBookList.subList(0, 10));
        model.addAttribute("hotBookList", hotBookList);
        
        return "index";
    }
    
    @GetMapping("/book/{id}")
    public String BookInfo(
            @PathVariable String id, Model model) {
        try {
            Book book = bookRepository.findOne(Integer.valueOf(id));
            if (book == null) {
                //TODO not found
            } else {
                model.addAttribute("book", book);
            }
        } catch (Exception e) {  //valueOf转换异常
            return "redirect:/";     //跳转到主页
        }
        return "detail";
    }
    
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword) {
        //TODO 从主页跳转到搜索页面可以做一次服务器渲染, 或者JS直接获取URL的keyword参数做ajax查询
        return "search";
    }
    
    @GetMapping("/signin")
    public String signin() {
        return "signin";
    }
    
    @GetMapping("/cashier/{orderId}")
    public void pay(
            @PathVariable String orderId,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Order order = orderRepository.findOne(orderId);
        if (order == null) {
            // TODO 无此订单
            log.debug("无此订单");
            response.sendError(404);
            return;
        }
        
        int status = order.getStatus();
        if (status == Order.Status.WAIT) { //未支付
            Map<String,String> params = new HashMap<>();
            params.put("out_trade_no", order.getUuid());
            params.put("product_code","FAST_INSTANT_TRADE_PAY");
            params.put("total_amount", order.getPrice() + "");
            params.put("subject","测试下单"); //不填支付宝报参数错误，解释： 商品的标题/交易标题/订单标题/订单关键字等，是请求时对应的参数，原样通知回来
            String form = alipayService.buildForm(params);
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().print(form);
            response.getWriter().flush();
            response.getWriter().close();
        } else if (status == Order.Status.SUCCESS){ 
            //TODO 已经支付
        } else if (status == Order.Status.OVER_TIME) {
            //TODO 支付超时 
        }
 
    }
    
    @GetMapping("/addcart")
    public String addCart(
            @SessionAttribute(required = false) User user,
            @RequestParam String id,
            @RequestParam(required = false) Integer num,  //增量改变
            @RequestParam(required = false) Integer count, //总数改变
            @CookieValue(name = "user-key", required = false) String userkey,
            HttpServletResponse response) throws IOException {
        log.debug("id:{} num:{} count:{} user-key:{}", id, num, count, userkey);
        if (num == null && count == null) {  //参数错误
            return "redirect:/cart.html";
        }
        
        if (StringUtils.isEmpty(userkey)) {
            //当cookie为空, 新增cookie
            userkey = UUID.randomUUID().toString();
            CommonUtils.setCookie(response, new Cookie("user-key", userkey));
        }
        
        //检查客户端是否处于登录状态
        if (user != null) { 
            //已登录，商品加入用户专属的购物车里
            userkey = user.getCart();
        }
        
        String key = "demo5.cart." + userkey;
        Redis.execute( jedis -> {
            /*
             * 1)key存在: 该购物车已经属于数据库用户(已经取消TTL), 或者已经设置了TTL
             * 2)key不存在: 新购物车, 30天有效期
             */
            boolean isNew = !jedis.exists(key);
            if (count != null) {
                if (count == 0) {
                    jedis.hdel(key, id);  //删除该购物车项
                } else {
                    jedis.hset(key, id, count.toString());
                }
            } else {
                jedis.hincrBy(key, id, num);
            }
            if (isNew) {
                jedis.expire(key, 86400 * 30);
            }
        });
        return "redirect:/cart.html"; //response.sendRedirect("cart.html");
    }
    
    
    /**
     * 支付成功后前台会被回调到这个地址
        //支付宝的前台回调参数
        total_amount=36.00
        timestamp=2017-06-28+16%3A35%3A35
        sign=u%2BKs8Klk0rUl6g%2BtVEnaW0%2FB67z1zlveNbIbz9EMY%2F44GAYQO7hap7ZtvSHG2FbdABuzpj8hOEh83PrhzEjvavhpGxVDCA6FPXWCQ%2Fe1Z2l79vZEDw58ho4UBaQv3GJYlikUGgeMez56AM6quHM7dZxyUlncwniiZO5Qtxc4440%3D
        trade_no=2017062821001004690200311600
        sign_type=RSA
        auth_app_id=2016080600183315
        charset=UTF-8
        seller_id=2088102170257722
        method=alipay.trade.page.pay.return
        app_id=2016080600183315
        out_trade_no=8bd22ed9d0934daa8c485cb855f803ec
        version=1.0
     */
    @GetMapping("/notify/pay_success")
    public String success(
            HttpServletResponse response) {
        // TODO 简单不做验证,直接重定向到个人信息页面
        return "redirect:/pay_success.html";
    }
    
    @GetMapping("/signout")
    public String signout(
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            session.removeAttribute("user");
        }
        return "redirect:/";
    }
}
