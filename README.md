## 写在前面的话

此仓库用来记录阿狗的独立项目，所有项目均可在线访问。

起初以demo命名只是为了缓解命名的痛苦，事实上我并不喜欢demo式的东西，相反，完整，健全才是我常常追求的东西。如果这里每一个完整的demo都或多或少能帮到你，那它除了给我某些实际的便利以外，还多出来了你赋予给它的意义。

出于安全顾虑，我会清除某些配置(像数据库密码这类重要信息)，Spring Boot项目一般会在AppConfig.java中。

----

## demo0 《选课小系统》

大一暑假使用JavaWeb开发的选课小系统，实现了登录、找回密码与选课功能。

在Myisam引擎下使用腾讯云最低端服务器实现单秒2000+UPDATE或2000+SELECT，勉强超过教务系统。

大二使用框架进行重构与优化，在支持事务的Innodb引擎下实现2000+TPS。重构期间测试了Servlet、Spring MVC与JDBC、ORM混搭的性能并得出一份简单的性能数据。

技术栈：
- Spring Boot
- Thymeleaf
- JPA

预览：

![](http://static.leeys.top/demo0/preview/demo0.gif?v=new)

如何运行：

导入Maven项目后在Application.java里

```java
/**
 * 1.修改AppConfig.java里数据库的用户名与密码
 * 2.运行main方法
 * 3.浏览器访问: http://localhost:8080/demo0
 */
public static void main(String args[]) {
    SpringApplication.run(Application.class, args);
}
```

项目地址：http://demo.leeys.top/demo0/ （内存不足已经取消 -- 2017-07-01）

博客地址：[demo0小结](http://leeys.top/2017/04/28/demo0%E5%B0%8F%E7%BB%93/)

性能测试图： http://demo.leeys.top/demo0/performance.html

----

## demo1 在线游戏——《石头剪刀布》

原理是把用户的猜拳记录存放在循环队列中每10ms检测队列数量是否为偶数，是则出队两次交换猜拳记录，否则休眠当前线程直到下一玩家接入，即使前一玩家离线服务器也会保存他的猜拳记录。代码比较简陋就不提交仓库了。

项目地址：http://demo.leeys.top/demo1/


----

## demo2 双人实时游戏——《滑稽大作战》


原名弹球游戏，是在大二上学期与同学在课内作业的基础上改版而成的。在下学期重写，添加了贴吧滑稽元素，并改名为滑稽大作战。

技术栈：
- Websocket
- Vue2
- Canvas
- 新浪OAuth2

项目地址：http://demo.leeys.top/demo2/

博客地址：[demo2小结](http://leeys.top/2017/07/03/demo2%E5%B0%8F%E7%BB%93/)

----

## demo5 图书购物商城——《天天书屋》

期末PHP答辩作业，认为书上与老师的做法略显简陋，自己用Java重写了一份，35W图书数据来自图书馆一老师的慷慨救济。

因为是作业，所以后台写得比较 Quick and Dirty，比如取消了Service层，只有Controller与Repository层，使用大量的Lambda与自己封装的Redis回调，使得删除Service层后的Controller也十分精简。

技术栈：

- 后端：Alipay + Lucene + Redis
- 后台管理页面：Vue2 + Vue-router + Element-ui

**Redis：**

除了邮箱验证码与数据库缓存还用来实现购物车，具体模拟了京东购物车，实现了登录前的离线购物车与登录后的在线购物车的合并操作。

**Lucene：**

为什么不用"%keyword%"，或者MySQL5.7自带的分词索引？后者的理由是精确度不如前者，而前者的理由是不如不用。

我用它用来实现图书的全文索引。它是一款非常高效的Java中文全文索引库，是Java系公司的必备搜索，像阿里与美团都在使用。Lucene的API在不同的版本差异巨大，官方也没有很好的教程或文档，学习曲线初期非常陡峭。通常我们需要为某个实体对象也就是某一张表做索引以提供全文索引能力，当表一多时，写起来十分痛苦，为了缓解模板尴尬，这里我小造了一个轮子，只需要继承我写的`AbstractLucene`抽象父类，再用重写几个方法，即可为某张表实现全文索引，效果非常棒，具体可以参考demo5里的`BookLucene.java`和`UserLucene.java`。至于搜索，抽象父类只实现了由`getSearchFields()`指定字段的多字段搜索，如果需要自定义搜索，重写父类或者增加方法即可。

大概像这样，很简单吧。

```Java
@Component
public class UserLucene extends AbstractLucene<User, String> {
    @Override
    protected void buildIndex() {}
    @Override
    protected Analyzer getAnalyzer() {
        return CHINA_ANALYZER;
    }
    @Override
    protected Document convert(User e) {
        return null;
    }
    @Override
    protected User convert(Document doc) {
        return null;
    }
    @Override
    protected Directory getDirectory() {
        return null;
    }
    @Override
    protected String[] getSearchFields() {
        return null;
    }
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
```

**支付宝：**

使用了支付宝的提供的沙箱环境，能够对进行支付。

项目地址：http://demo.leeys.top/demo5/

在线演示：

1. [注册+登录+购买](http://static.leeys.top/demo5_2.gif)
2. [后台管理](http://static.leeys.top/demo5_3.gif)
3. [Lucene全文搜索](http://static.leeys.top/demo5_4.gif)

----

## What's Next ?


<br>
<br>

----

## 写在后面的话

如果你看到的是一条狗趴在墙上的页面，那么请打开手机按照它说的做，我会非常感谢你。
