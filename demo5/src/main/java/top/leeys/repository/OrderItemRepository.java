package top.leeys.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import top.leeys.domain.Book;
import top.leeys.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer>{
    
    @Query(value = "select book.* from tb_order_item, tb_book book where book.id = book_id group by book_id order by sum(num) asc limit ?1", nativeQuery = true)
    List<Book> getHotBook(int limit);
}
