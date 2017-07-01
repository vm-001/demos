# 前言
本仓库用来记录阿狗所有的独立项目，所有项目均可在线访问。

# demo0 选课小系统
大一暑假使用JavaWeb开发的选课小系统，实现了登录、找回密码与选课功能。Myisam引擎下使用腾讯云最低端服务器实现单秒2000+UPDATE或2000+SELECT。后用框架进行重构与优化，在支持事务的Innodb引擎下实现2000+TPS。重构期间测试了Servlet、Spring MVC与JDBC、ORM混搭的性能并得出一份简单的数据。

新的技术栈：
> Spring Boot + Thymeleaf + JPA

项目地址：http://demo.leeys.top/demo0/

博客地址：[demo0小结](http://leeys.top/2017/04/28/demo0%E5%B0%8F%E7%BB%93/)

性能测试图： http://demo.leeys.top/demo0/performance.html


# demo1 在线游戏——石头剪刀布
原理是把用户的猜拳记录存放在循环队列中每10ms检测队列数量是否为偶数，是则出队两次交换猜拳记录，否则休眠当前线程直到下一玩家接入，即使前一玩家离线服务器也会保存他的猜拳记录。代码比较简陋就不打算提交仓库了。

项目地址：http://demo.leeys.top/demo1/

# demo2 双人实时游戏——滑稽大作战
原名为弹球游戏，与同学在课内作业的基础上改版成在线积分制。大二下学期我重写了整个游戏，添加了滑稽元素，并更名为滑稽大作战。

新的技术栈：
> Websocket + Vue + Canvas + Spring Boot + 新浪OAuth2

项目地址：http://demo.leeys.top/demo2/

# demo5 天天书屋——购物商城


Alipay + Lucene + Redis
后台管理页面：Vue2 + Vue-router + Element-ui

项目地址：http://demo.leeys.top/demo5/

在线演示：

注册 + 登录 + 购买：http://static.leeys.top/demo5_2.gif

后台管理界面：http://static.leeys.top/demo5_3.gif)

Lucene全文搜索：http://static.leeys.top/demo5_4.gif)
