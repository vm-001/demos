package top.leeys.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单封装HashMap用于链式操作
 * 
 * @author leeys.top@gmail.com
 */
public class Data {
    public Map<String, Object> data;
    
    public Data() {
        this.data = new HashMap<>();
    }
    
    public Data add(String key, Object value) {
        data.put(key, value);
        return this;
    }
}
