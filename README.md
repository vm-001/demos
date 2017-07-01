## 说在前面的话

此仓库用来记录阿狗的独立项目，所有项目均可在线访问。

起初以demo命名只是为了缓解命名的痛苦，事实上我并不喜欢demo式的东西，相反，完整，健全才是我常常追求的东西。如果这里每一个完整的demo都或多或少能帮到你，那它除了给我某些实际的便利以外，还多出来了你赋予给它的意义。

出于安全顾虑，我会清除某些配置(像数据库密码这类重要信息)，Spring Boot项目一般会在AppConfig.java中。

## demo0 选课小系统
大一暑假使用JavaWeb开发的选课小系统，实现了登录、找回密码与选课功能。

Myisam引擎下使用腾讯云最低端服务器实现单秒2000+UPDATE或2000+SELECT。

后用框架进行重构与优化，在支持事务的Innodb引擎下实现2000+TPS。重构期间测试了Servlet、Spring MVC与JDBC、ORM混搭的性能并得出一份简单的性能数据。

技术栈：
- Spring Boot
- Thymeleaf
- JPA

项目地址：http://demo.leeys.top/demo0/ （内存不足已经取消--2017-07-01）

博客地址：[demo0小结](http://leeys.top/2017/04/28/demo0%E5%B0%8F%E7%BB%93/)

性能测试图： http://demo.leeys.top/demo0/performance.html


## demo1 在线游戏——石头剪刀布

原理是把用户的猜拳记录存放在循环队列中每10ms检测队列数量是否为偶数，是则出队两次交换猜拳记录，否则休眠当前线程直到下一玩家接入，即使前一玩家离线服务器也会保存他的猜拳记录。代码比较简陋就不提交仓库了。

项目地址：http://demo.leeys.top/demo1/

## demo2 双人实时游戏——滑稽大作战

原名弹球游戏，是在大二上学期与同学在课内作业的基础上改版而成的。在下学期重写，添加了贴吧滑稽元素，并改名为滑稽大作战。

技术栈：
- Websocket
- Vue2
- Canvas
- 新浪OAuth2

项目地址：http://demo.leeys.top/demo2/

博客地址：


## demo5 天天书屋——购物商城

期末PHP作业。

独立完成天天书屋的设计、前端、后台、后端等所有工作。

因为是作业，所以后台写得比较 Quick and Dirty ，比如取消了Service层，只有Controller与Repository层，不过使用Java8的Lambda与自己封装的Redis回调，Controller层的代码十分简洁毫不臃肿。

购物车：模拟了京东的购物车，使用Redis实现了登录后离线购物车与在线购物车的合并操作。

Lucene：都知道中文的全文索引十分麻烦，MySQL5.7自带的中文全文索引又十分鸡肋。Lucene是一款非常高效的Java中文全文索引库，是Java系公司的必备搜索，像阿里与美团都在使用。Lucene的API在不同的版本差异巨大，官方也没有很好的教程或文档，学习曲线初期非常陡峭。在demo5里我造了一个还不算方的轮子，只需要继承我写的抽象父类，再用十几行代码重写几个方法，即可为某张表实现全文索引，效果非常棒。

支付宝：使用了支付宝的提供的沙箱环境，能够对进行支付。



技术栈：

- 后端：Alipay + Lucene + Redis
- 后台管理页面：Vue2 + Vue-router + Element-ui

项目地址：http://demo.leeys.top/demo5/

在线演示：

1. [注册+登录+购买](http://static.leeys.top/demo5_2.gif)
2. [后台管理](http://static.leeys.top/demo5_3.gif)
3. [Lucene全文搜索](http://static.leeys.top/demo5_4.gif)
