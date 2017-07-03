package top.leeys.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import top.leeys.domain.Book;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    
    @Query(value = "select * from tb_book order by id desc limit ?1, ?2", nativeQuery = true)
    List<Book> findByPosition(int offset, int limit);
    
    
    @Query(value = "select count(id) from tb_book", nativeQuery = true)
    int countNum();
    
    /**
     * 书名的前缀查询
     */
    @Query(value = "select * from tb_book where name like ?1% limit ?2, ?3", nativeQuery = true)
    List<Book> findByNamePrefix(String keyword, int offset, int limit);
    
    @Query(value = "select * from tb_book where name like ?1% order by price asc limit ?2, ?3", nativeQuery = true)
    List<Book> findByNamePrefixOrderByPriceAsc(String keyword, int offset, int limit);
    
    @Query(value = "select * from tb_book where name like ?1% order by price desc limit ?2, ?3", nativeQuery = true)
    List<Book> findByNamePrefixOrderByPriceDesc(String keyword, int offset, int limit);
    
    @Query(value = "select * from tb_book where name like ?1% order by publish_date asc limit ?2, ?3", nativeQuery = true)
    List<Book> findByNamePrefixOrderByPubDateAsc(String keyword, int offset, int limit);
    
    @Query(value = "select * from tb_book where name like ?1% order by publish_date desc limit ?2, ?3", nativeQuery = true)
    List<Book> findByNamePrefixOrderByPubDateDesc(String keyword, int offset, int limit);
    
    
    
    @Query(value = "select * from tb_book where price between ?1 and ?2 limit ?3, ?4", nativeQuery = true)
    List<Book> findByPrice(float lowPrice, float hightPrice, int offset, int limit);
    
    @Query(value = "select sum(num) sales, book.* from tb_order_item, tb_book book where book.id = book_id group by book_id order by sales desc limit ?1", nativeQuery = true)
    List<Book> getHotBook(int limit);
    
    @Modifying
    @Query(value = "update tb_book set img_url = ?1 where id = ?2", nativeQuery = true)
    int UpdateImgUrl(String url, Integer id);
} 
