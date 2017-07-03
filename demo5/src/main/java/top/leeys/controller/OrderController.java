package top.leeys.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.Admin;
import top.leeys.config.ErrorCode;
import top.leeys.domain.Book;
import top.leeys.domain.Order;
import top.leeys.domain.OrderItem;
import top.leeys.domain.User;
import top.leeys.pojo.Data;
import top.leeys.pojo.Message;
import top.leeys.pojo.OrderCreateForm;
import top.leeys.redis.Redis;

@RestController
@RequestMapping(value = "api/order", name ="订单接口")
@Slf4j
public class OrderController extends BaseController{
    
    @PostMapping
    public ResponseEntity<Message> createOrder(
            @SessionAttribute(required = false) User user,
            @Valid @RequestBody OrderCreateForm form,
            BindingResult result, HttpServletResponse response) throws IOException {
        if (user == null) {
            log.debug("未登录");
            return ResponseEntity.ok(new Message(ErrorCode.NOT_SIGNIN, "请重新登录"));
        }
        if (result.hasErrors()) {
            log.error("创建订单失败：" + result.getFieldError().getDefaultMessage());
            return ResponseEntity.ok(new Message(ErrorCode.PARAMS_ERROR, result.getFieldError().getDefaultMessage()));
        }
        log.debug("订单创建:" + form.toString());
        
        //取出并清空购物车
        Map<String, String> cartItems = new HashMap<>();
        Redis.execute( jedis -> {
            String key = "demo5.cart." + user.getCart();
            cartItems.putAll(jedis.hgetAll(key));
            if (cartItems.size() != 0) {
                jedis.hdel(key, cartItems.keySet().toArray(new String[0]));
            }
        });
        
        if (cartItems.size() == 0) {
            //调试期间可能会出现的bug
            return ResponseEntity.ok(new Message(ErrorCode.UNKNOWN_ERROR, "购物车不能为空"));
        }
        
        List<Integer> ids = new ArrayList<>();
        cartItems.keySet().forEach( k -> ids.add(new Integer(k)));
        List<Book> books = bookRepository.findAll(ids);
        
        //创建订单
        Order order = new Order();
        order.setAddress(form.getAddress());
        order.setConsignee(form.getConsignee());
        order.setPhone(form.getPhone());
        order.setCreatedTime(new Date());
        order.setUser(user);
        
        List<OrderItem> items = new ArrayList<>();
        books.forEach( book -> {
            items.add(new OrderItem(order, book, new Integer(cartItems.get(book.getId() + ""))));
        });
        
        //设置金额并保存订单
        order.setOriginPrice(order.countOriginPrice(items));
        order.setPrice(order.countPrice(items));
        orderRepository.save(order);
        
        //保存订单项
        items.forEach( item -> item.setOrder(order));
        orderItemRepository.save(items);
        
        
        Object data = new Data()
                .add("uuid", order.getUuid())
                .add("price", order.getPrice())
                .data;
        return ResponseEntity.ok(Message.ok(data));
    }
    
    
    /*==============管理员接口 =============*/
    
    @Admin
    @GetMapping
    public ResponseEntity<Message> getOrderList(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<Order> orderList = orderRepository.findByPosition(offset, limit);
        Object data = new Data()
                .add("total", orderRepository.count())
                .add("orderList", orderList)
                .data;
        return ResponseEntity.ok(Message.ok(data));
    }

    @Admin(true)
    @PutMapping
    public ResponseEntity<Message> updateOrder(
            @RequestBody Order order) {
        orderRepository.save(order);
        return ResponseEntity.ok(Message.ok());
    }

    @Admin(true)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Message> deleteOrder(
            @PathVariable String uuid) {
        orderRepository.delete(uuid);
        return ResponseEntity.ok(Message.ok());
    }
}
