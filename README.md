> 借鉴 @杰锅锅（Jackie）大神 [《Java豆瓣电影爬虫——小爬虫成长记（附源码）》](http://www.cnblogs.com/bigdataZJ/p/doubanmovie3.html) 中的爬取思路和代码 [JewelCrawler](https://github.com/DMinerJackie/JewelCrawler) ,采用自己熟悉的Spring-data-jpa进行改写，简化数据库操作和事务。

爬取核心代码200行左右。
1. persistence.xml 
- 配置数据库连接
- 根据实体 POJO 类自动建表。如不需要改为 false.
```
<property name="hibernate.hbm2ddl.auto" value="update" />
```

2. Entrance
- 入口文件

3. log4j.properties
- 日志文件默认存放在F盘
```
log4j.appender.logfile.File=F:/movieCrawler.log
```

4. 思路
- 从一个电影详情页面，抓取其中的电影详情。并把该页面中链接到其他电影页的URL记录下来用于抓取