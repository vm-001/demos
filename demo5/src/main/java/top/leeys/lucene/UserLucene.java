package top.leeys.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import top.leeys.domain.User;

/**
 * 当数据表的主键为String类型时更加简单
 * 这是一个简单的例子
 * 
 * @author leeys.top@gmail.com
 */
public class UserLucene extends AbstractLucene<User, String> {
    private static final String FILE_DIR = BASE_DIR + "/user";
    
    private static Directory lucene_directory;
    
    //搜索字段
    private static String[] SEARCH_FIELD = new String[]{"username", "email"};
    
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
    protected void buildIndex() {
        //TODO 参照 BookLucene 的写法，从数据库获取记录行创建索引
        //留心OOM与NULL值
    }

    @Override
    protected Analyzer getAnalyzer() {
        return CHINA_ANALYZER;
    }

    @Override
    protected Document convert(User e) {
        Document doc = new Document();
        doc.add(new StringField("uuid", e.getUuid(), Store.YES));
        doc.add(new StringField("email", e.getEmail(), Store.YES));
        //TextField 表示该字段会被 getAnalyzer() 提供的分词器进行分词，该字段能够进行全文搜索
        //而StringField则不会被切词，确保当做一个整体被搜索
        doc.add(new TextField("username", e.getUsername(), Store.YES));
        // ... 根据需要自行增加
        return doc;
    }

    @Override
    protected User convert(Document doc) {
        User user = new User(doc.get("uuid"));
        user.setEmail(doc.get("email"));
        user.setUsername(doc.get("usernaem"));
        return user;
    }

    @Override
    protected Directory getDirectory() {
        return lucene_directory;
    }

    @Override
    protected String[] getSearchFields() {
        return SEARCH_FIELD;
    }

    
    /**
     * 主键类型为String操作起来非常简单
     */
    @Override
    public void delete(String id) {
        TermQuery query = new TermQuery(new Term("uuid", id));
        super.delete(query);
    }
    @Override
    public void update(String id, User e) {
        super.update(e, new Term("uuid", id));
    }
    
}
