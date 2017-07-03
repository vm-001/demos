package top.leeys.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import top.leeys.domain.Book;

/**
 * 通常父类已经能完成很多工作.
 * 但如果实体对象的主键不是String类型, 比如Book是Integer, 此时需要提供
 * 
 * @author leeys.top@gmail.com
 */
@Component
@Slf4j
public class BookLucene extends AbstractLucene<Book, Integer> {
    private static final String FILE_DIR = BASE_DIR + "/book";
    
    private static Directory lucene_directory;
    private static String[] SEARCH_FIELD = new String[]{"name", "author", "publisher"};
    
    static {
        try {
            lucene_directory = FSDirectory.open(Paths.get(FILE_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @PostConstruct
    public void init() throws IOException {
        if (!hasIndex()) {
            buildIndex();
        }
    }
    @Override
    public void delete(Integer id) {
        Query query = NumericRangeQuery.newIntRange("id", id, id, true, true);
        super.delete(query);
    }
    
    @Override
    public void update(Integer id, Book e) {
        //主键为Integer的实体对象比较麻烦，先删再创建
        delete(id);
        save(e);
    }


//    public List<Book> search(String keyword, int offset, int limit) {
//        return super.MultiFieldsearch(keyword, offset, limit, SEARCH_FIELD);
//    }
    
    public List<Book> MultiFieldsearch(String keyword, int offset, int limit) {
        
        return super.MultiFieldsearch(keyword, offset, limit, SEARCH_FIELD);
    }
    
//    public List<Book> searchWithPrefix(String keyword, String field, int offset, int limit) {
//        List<Book> list = new ArrayList<>();
//        try {
//            IndexReader reader = DirectoryReader.open(getDirectory());
//            IndexSearcher searcher = new IndexSearcher(reader);
//            Term term = new Term(field, keyword);
//            PrefixQuery query = new PrefixQuery(term); 
//            log.debug(query.toString());
//            Sort sort = new Sort(new SortField("af", SortField.Type.STRING));
//            ScoreDoc[] hits = searcher.search(query, offset + limit).scoreDocs; 
//            int end = Math.min(offset + limit, hits.length);
//            for (int i = offset; i < end; i++) {
//                Document hitdoc = searcher.doc(hits[i].doc);
//                list.add(convert(hitdoc));
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
    
    public List<Book> searchWithPrefix(String keyword, int offset, int limit, Sort sort) {
        List<Book> list = new ArrayList<>();
        try {
            IndexReader reader = DirectoryReader.open(getDirectory());
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(getSearchFields(), getAnalyzer());
            Query query = parser.parse(keyword);
            log.debug(query.toString());
            ScoreDoc[] hits = searcher.search(query, offset + limit, sort).scoreDocs; 
            int end = Math.min(offset + limit, hits.length);
            for (int i = offset; i < end; i++) {
                Document hitdoc = searcher.doc(hits[i].doc);
                list.add(convert(hitdoc));
            }
            reader.close();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    
   
    
    @Override
    protected Document convert(Book book) {
        Document doc = new Document();
        doc.add(new IntField("id", book.getId(), Store.YES));
//        doc.add(new StringField("marc_no", book.getMarcNo(), Store.YES));
        doc.add(new StringField("isbn", book.getIsbn(), Store.YES));
        doc.add(new StringField("img_url", book.getImgUrl(), Store.YES));
        doc.add(new TextField("name", book.getName(), Store.YES));
        doc.add(new TextField("author", book.getAuthor(), Store.YES));
        doc.add(new TextField("publisher", book.getPublisher(), Store.YES)); 
        doc.add(new FloatField("price", book.getPrice(), FLOAT_FIELD_TYPE_STORED_SORTED));
        doc.add(new FloatField("discount", book.getDiscount(), Store.YES));
        doc.add(new LongField("publish_date", book.getPublishDate() == null ? 0 : book.getPublishDate().getTime(), LONG_FIELD_TYPE_STORED_SORTED));
        
        return doc;
    }

    @Override
    protected Book convert(Document doc) {
        Book book = new Book();
        book.setId(Integer.valueOf(doc.get("id")));
        book.setIsbn(doc.get("isbn"));
//        book.setMarcNo(doc.get("marc_no"));
        book.setImgUrl(doc.get("img_url"));
        book.setName(doc.get("name"));
        book.setAuthor(doc.get("author"));
        book.setPublisher(doc.get("publisher"));
        book.setPrice(doc.get("price") == null ? 0 : Float.valueOf(doc.get("price")));
        book.setDiscount(Float.valueOf(doc.get("discount")));
        book.setPublishDate(new Date(Long.valueOf(doc.get("publish_date"))));
        
        return book;
    }

    @Override
    public Directory getDirectory() {
        return lucene_directory;
    }
    
    @Override
    protected String[] getSearchFields() {
        return SEARCH_FIELD;
    }
    

    /**
     * 从数据库读获取数据建立索引
     * 当 lucene_directory 目录下的索引为空时，可调用次方法初始化索引
     * 使用偏底层的jdbc操作防止内存溢出
     */
    @Override
    public void buildIndex() {
        System.err.println("---从数据库读取数据初始化索引---");
        final List<Document> docs = new ArrayList<>();
        String sql = "select * from tb_book";
        jdbcTemplate.query(sql, rs -> {
            Document doc = new Document();
            doc.add(new IntField("id", rs.getInt("id"), Store.YES));
//            doc.add(new StringField("marc_no", rs.getString("marc_no"), Store.YES));
            doc.add(new StringField("isbn", rs.getString("isbn"), Store.YES));
            doc.add(new StringField("img_url", rs.getString("img_url"), Store.YES));
            doc.add(new TextField("name", rs.getString("name"), Store.YES));
            doc.add(new TextField("author", rs.getString("author"), Store.YES));
            doc.add(new TextField("publisher", rs.getString("publisher"), Store.YES)); 
            doc.add(new FloatField("price", rs.getFloat("price"), FLOAT_FIELD_TYPE_STORED_SORTED));
            doc.add(new FloatField("discount", rs.getFloat("discount"), Store.YES));
            Date pubdate = rs.getDate("publish_date");
            doc.add(new LongField("publish_date", pubdate == null ? 0 : pubdate.getTime(), LONG_FIELD_TYPE_STORED_SORTED));
            
            docs.add(doc); 
            if (docs.size() > 20000) { //根据服务器内存调节
                log.debug("批量保存");
                saveDocuments(docs);
                docs.clear(); //辅助GC, 谨防OOM
            }
        });
        if (docs.size() > 0) {  //保存剩余部分
            saveDocuments(docs); 
        }
        System.err.println("---索引初始化完成---");
    }
    
    @Override
    protected Analyzer getAnalyzer() {
        return CHINA_ANALYZER;
    }
}
