package top.leeys.pojo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.leeys.domain.Book;

/**
 * 购物车商品POJO
 * 客户端POST时只需上传id与num字段
 * 
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CartItem {
    @JsonUnwrapped
    Book book;
    int num;
    
    public int getId() {
        return book.getId();
    }
}
