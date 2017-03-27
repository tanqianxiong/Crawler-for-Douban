> 借鉴 @杰锅锅（Jackie）大神 [《Java豆瓣电影爬虫——小爬虫成长记（附源码）》](http://www.cnblogs.com/bigdataZJ/p/doubanmovie3.html) 中的爬取思路和代码 [JewelCrawler](https://github.com/DMinerJackie/JewelCrawler) ,采用自己熟悉的JPA进行改写，简化数据库操作和事务。

1.Hibernate 实现的 JPA 可以根据实体 POJO 类自动建表。在 persistence.xml 中设置，如不需要改为 false.
```
<property name="hibernate.hbm2ddl.auto" value="update" />
```
