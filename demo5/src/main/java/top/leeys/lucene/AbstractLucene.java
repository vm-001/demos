package top.leeys.lucene;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.leeys.config.AppConfig;
import top.leeys.config.ErrorCode;
import top.leeys.exception.BaseException;

/**
 * Lucene的抽象父类
 * 
 * 注意:
 * 保存索引的方法子类不用重写, 父类已经能很好的完成.
 * update,delete方法的实现因实体对象主键类型的不同而略有不同,需子类重写并改为public修饰符
 * 
 * @author leeys.top@gmail.com
 */
@Slf4j
public abstract class AbstractLucene<E, ID extends Serializable> {
    @Autowired JdbcTemplate jdbcTemplate;
    
    protected static final String BASE_DIR = AppConfig.CLASSPATH + "lucene";
    
    /*分析器*/
    protected static final SmartChineseAnalyzer CHINA_ANALYZER = new SmartChineseAnalyzer();  //中文分词
    protected static final WhitespaceAnalyzer WHITE_ANALYZER = new WhitespaceAnalyzer();      //空格分词
    
    /*用于排序*/
    protected static final FieldType FLOAT_FIELD_TYPE_STORED_SORTED = new FieldType(FloatField.TYPE_STORED);
    protected static final FieldType LONG_FIELD_TYPE_STORED_SORTED = new FieldType(LongField.TYPE_STORED);
    
    static {
        FLOAT_FIELD_TYPE_STORED_SORTED.setDocValuesType(DocValuesType.NUMERIC);
        FLOAT_FIELD_TYPE_STORED_SORTED.freeze();
        LONG_FIELD_TYPE_STORED_SORTED.setDocValuesType(DocValuesType.NUMERIC);
        LONG_FIELD_TYPE_STORED_SORTED.freeze();
    }
    
    public void save(E e) {
        Document doc = convert(e);
        execute( writer ->  writer.addDocument(doc));
    }
    
    public void saveAll(@NonNull List<E> list) {
        if (list.size() > 0) {
            List<Document> docs = new ArrayList<>();
            list.forEach( e -> docs.add(convert(e)));
            execute( writer ->  writer.addDocuments(docs));
        }
    }
    
    public void saveDocuments(@NonNull List<Document> docs) {
        if (docs.size() > 0) {
            execute( writer ->  writer.addDocuments(docs));
        }
    }
    
    /**
     * 根据实体对象主键
     * String: Query query = new TermQuery(new Term("id", "value"))
     * Number: Query query = NumericRangeQuery.newXXXRange(field, min, max, includeMin, includeMax)
     */
    protected void delete(Query query) {
        execute( writer -> writer.deleteDocuments(query));
    }
    /**
     * 删除所有索引
     */
    public void deleteAll() {
        execute( writer -> writer.deleteAll());
    }
    
    /**
     * 更新索引
     * 只适用主键为String的实体对象
     */
    protected void update(E e, Term term) {
        Document doc = convert(e);
        execute( writer -> writer.updateDocument(term, doc));
    }
    
    protected void close(IndexWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 简单多字段搜索
     */
    public List<E> MultiFieldsearch(String keyword, int offset, int limit, String... fields) {
        List<E> list = new ArrayList<>();
        try {
            IndexReader reader = DirectoryReader.open(getDirectory());
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, getAnalyzer());
            Query query = parser.parse(keyword);
            log.debug(query.toString());
            ScoreDoc[] hits = searcher.search(query, offset + limit).scoreDocs; 
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
    
    /**
     * 通过文件夹里是否有文件来判断索引是否为空(粗糙判断)
     */
    public boolean hasIndex() throws IOException {
        Directory directory = getDirectory();
        return directory.listAll().length > 0;
    }
    
    /**
     * 合并索引优化索引性能
     */
    public void merge(int maxNumSegments) {
        execute( writer -> writer.forceMerge(maxNumSegments));
    }
    
    
    /**
     * 允许子类调用回调获取属于子类的IndexWriter
     */
    protected static interface Callback {
        void callback(IndexWriter writer) throws IOException;
    }
    protected void execute(Callback callback) {
        IndexWriter writer = null;
        try {
            IndexWriterConfig config = new IndexWriterConfig(getAnalyzer());  
            writer = new IndexWriter(getDirectory(), config);
            try {
                callback.callback(writer);
            } catch (IOException e) {
                log.error("索引操作异常", e);
                throw new BaseException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
            }
        } catch (IOException ex) {
            log.error("文件不可读取或不存在", ex);
        } finally {
            close(writer);
        }
    }

   
    
    /*由子类实现的抽象方法 */
    protected abstract void buildIndex();
    protected abstract Analyzer getAnalyzer();
    protected abstract Document convert(E e);
    protected abstract E convert(Document doc);
    protected abstract Directory getDirectory();
    protected abstract String[] getSearchFields();
    //因为实体主键的不同，需要由子类实现
    protected abstract void delete(ID id);
    protected abstract void update(ID id, E e);
}
