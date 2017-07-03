package top.leeys.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import top.leeys.annotation.Admin;
import top.leeys.config.ErrorCode;
import top.leeys.domain.Book;
import top.leeys.pojo.Data;
import top.leeys.pojo.Message;
import top.leeys.redis.Redis;

@RestController
@RequestMapping(value = "api/book", name = "图书接口")
@Slf4j
public class BookController extends BaseController {
    
    /**
     * 按创建时间从新到旧显示
     */
    @GetMapping
    public ResponseEntity<Message> getBookList(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<Book> bookList = bookRepository.findByPosition(offset, limit);
        Object data = new Data()
                .add("total", bookRepository.count())
                .add("bookList", bookList)
                .data;
        return ResponseEntity.ok(Message.ok(data));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Message> getBookInfo(
            @PathVariable int id) {
        Book book = bookRepository.findOne(id);
        return ResponseEntity.ok(Message.ok(book));
    }
    
    
    /**
     * 图书查询
     * type: 所有类型的查询都可以带上offset(默认0)和limit(默认10)参数
     *     all(模糊查询)  : keyword, [offset, limit]
     *     name(书名查询) : keyword, [offset, limit, prefix, sort]
     *     price(价格查询): min, max
     * 注: []里为可选参数:
     *     offset: 起始位置, 默认为0
     *     limit: 返回数量, 默认为10
     *     prefix: 是否为前缀匹配, 取值0或1
     *     sort: 1价格升序, 2价格降序, 3出版时间升序, 4出版时间降序, 5销量升序, 6销量降序
     *     
     */
    @GetMapping("/search/{type}")
    public ResponseEntity<Message> search(
            @PathVariable String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(value = "min", required = false, defaultValue = "0") float minPrice,
            @RequestParam(value = "max", required = false, defaultValue = "0") float maxPrice,
            @RequestParam(required = false, defaultValue = "0") int prefix,
            @RequestParam(required = false, defaultValue = "0") int sort) {
        if (StringUtils.isEmpty(keyword)) { //防止异常
            return ResponseEntity.ok(Message.ok(new ArrayList<Book>()));
        }
        List<Book> bookList = null;
        long start = System.currentTimeMillis();
        if (type.equals("all")) {
            //lucene搜索结果进行排序相比sql的ORDER BY要麻烦许多.
            Sort s = null;
            if (sort == 1) {        //价格升序
                s = new Sort(new SortField("price", Type.FLOAT, false));
            } else if (sort == 2) {
                s = new Sort(new SortField("price", Type.FLOAT, true));
            } else if (sort == 3) { //出版时间升序
                s = new Sort(new SortField("publish_date", Type.LONG, false));
            } else if (sort == 4) { 
                s = new Sort(new SortField("publish_date", Type.LONG, true));
            } else if (sort == 5) { //销量升序
                //TODO 
            } else if (sort == 6) {
                //TODO
            }
            bookList = s == null ? 
                    bookLucene.MultiFieldsearch(keyword, offset, limit) :
                    bookLucene.searchWithPrefix(keyword, offset, limit, s);    
        } else if (type.equals("price")) {
            bookList = bookRepository.findByPrice(minPrice, maxPrice, offset, limit);
        } else if (type.equals("name")) {
            if (sort == 1) {
                bookList = bookRepository.findByNamePrefixOrderByPriceAsc(keyword, offset, limit);
            } else if (sort == 2) {
                bookList = bookRepository.findByNamePrefixOrderByPriceDesc(keyword, offset, limit);
            } else if (sort == 3) {
                bookList = bookRepository.findByNamePrefixOrderByPubDateAsc(keyword, offset, limit);
            } else if (sort == 4) {
                bookList = bookRepository.findByNamePrefixOrderByPubDateDesc(keyword, offset, limit);
            } else {
                bookList = bookRepository.findByNamePrefix(keyword, offset, limit);
            }
        }
        System.out.println("spend: " + (System.currentTimeMillis() - start));
        return ResponseEntity.ok(Message.ok(bookList));
            
    }

    
    /*=============管理员接口 =============*/
    
    @Admin(true)
    @PostMapping
    public ResponseEntity<Message> createBook(
            @RequestBody Book book) {
        log.debug("创建图书:" + book.toString());
        book = bookRepository.save(book);
        Redis.execute( jedis -> jedis.incr("demo5.book.num")); //改变redis缓存
        bookLucene.save(book);  //新增索引
        return ResponseEntity.ok(Message.ok());
    }

    @Admin(true)
    @PutMapping
    public ResponseEntity<Message> updateBook(
            @RequestBody Book book) {
        book = bookRepository.save(book);
        bookLucene.update(book.getId(), book); //更新索引
        log.debug("索引已更新:" + book.toString());
        return ResponseEntity.ok(Message.ok());
    }

    @Admin(true)
    @DeleteMapping("/{id}")
    public ResponseEntity<Message> deleteBook(
            @PathVariable int id) {
        try {
            bookRepository.delete(id);
            Redis.execute( jedis -> jedis.decr("demo5.book.num")); //改变redis缓存
            bookLucene.delete(id);
            log.debug("索引已删除:" + id);
            return ResponseEntity.ok(Message.ok());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.ok(new Message(ErrorCode.UNKNOWN_ERROR, "外键约束，不能删除"));
        }
    }
}
