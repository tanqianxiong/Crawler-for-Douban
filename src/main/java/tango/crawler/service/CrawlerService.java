package tango.crawler.service;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tango.crawler.dao.CommentDAO;
import tango.crawler.dao.MovieDAO;
import tango.crawler.dao.RecordDAO;
import tango.crawler.entity.Comment;
import tango.crawler.entity.Movie;
import tango.crawler.entity.Record;
import tango.crawler.util.CommonUtil;
import tango.crawler.util.Constant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by TANQX3 on 2017-3-21.
 */
@Service
public class CrawlerService {
    @Autowired
    private MovieDAO movieDAO;
    @Autowired
    private CommentDAO commentDAO;
    @Autowired
    private RecordDAO recordDAO;

    private Logger LOG = LoggerFactory.getLogger("CommonLog");

    @Transactional
    public void crawlOnePage() throws Exception {
        Record record = getOneRecordToCrawl();
        String url = record.getUrl();
        LOG.info("Crawling url:" + url);
        try {
            Document doc = Jsoup.connect(url).get();

            //1、抓取该page里是有用href的地址存到record里
            crawlValuableRecordInPage(doc);

            //2、抓取该page里的电影或影评
            if (Record.TYPE_MOVIE.equals(record.getType())) {
                crawlMovieInfo(doc, record);
            } else if (Record.TYPE_COMMENT.equals(record.getType())) {
                crawlCommentInfo(doc, record);
            }

            record.setCrawled(Record.STATUS_CRAWLED);
        } catch (HttpStatusException e) {
            record.setCrawled(Record.STATUS_ERROR);
            LOG.info(e.getMessage());
        }
        recordDAO.save(record);
    }

    private Record getOneRecordToCrawl() {
        Record r = recordDAO.getFirstByCrawled(Record.STATUS_UNCRAWLED);
        if (r == null) {
            r = new Record();
            r.setUrl(Constant.MAIN_URL);
            r.setCrawled(Record.STATUS_UNCRAWLED);
            r.setType(Record.TYPE_OTHER);
            recordDAO.save(r);
        }
        return r;
    }

    @Transactional
    private void crawlValuableRecordInPage(Document doc) {
        Elements hrefs = doc.select("a[href^='" + Constant.BASE_URL + "']");
        for (Element e : hrefs) {
            String href = e.attr("href").trim();
            String type = CommonUtil.whichType(href);
            if (type == null || Record.TYPE_OTHER.equals(type) || recordDAO.getByUrl(href) != null) {
                continue;
            }
            Record record = new Record();
            record.setUrl(href);
            record.setCrawled(Record.STATUS_UNCRAWLED);
            record.setType(type);
            recordDAO.save(record);
        }
    }

    @Transactional
    private void crawlMovieInfo(Document doc, Record record) {
        Element infoDiv = doc.getElementById("info");
        if (infoDiv == null) {
            return;
        }
        String subjectId = CommonUtil.extractSubjectId(record.getUrl());
        if (movieDAO.findBySubjectId(subjectId) != null) {
            return;
        }

        Elements subInfos = infoDiv.children();
        Movie movie = new Movie();
        for (Element subInfo : subInfos) {
            if (subInfo.childNodeSize() > 0) {
                String key = subInfo.getElementsByAttributeValue("class", "pl").text();
                if (key == null || "".equals(key)) {
                    continue;
                }
                if ("导演".equals(key)) {
                    movie.setDirector(subInfo.getElementsByAttributeValue("class", "attrs").text());
                } else if ("编剧".equals(key)) {
                    movie.setScenarist(subInfo.getElementsByAttributeValue("class", "attrs").text());
                } else if ("主演".equals(key)) {
                    movie.setActors(subInfo.getElementsByAttributeValue("class", "attrs").text());
                }
            }
        }
        Pattern pattern = Pattern.compile("制片国家/地区:</span>(.*?)\n");
        Matcher matcher = pattern.matcher(infoDiv.html());
        if (matcher.find()) {
            movie.setCountry(matcher.group(1).trim());
        }
        pattern = pattern.compile("语言:</span>(.*?)\n");
        matcher = pattern.matcher(infoDiv.html());
        if (matcher.find()) {
            movie.setLanguage(matcher.group(1).trim());
        }
        movie.setType(infoDiv.getElementsByAttributeValue("property", "v:genre").text());
        movie.setReleaseDate(infoDiv.getElementsByAttributeValue("property", "v:initialReleaseDate").text());
        movie.setRuntime(infoDiv.getElementsByAttributeValue("property", "v:runtime").text());
        movie.setTags(doc.getElementsByClass("tags-body").text());
        movie.setName(doc.getElementsByAttributeValue("property", "v:itemreviewed").text());
        movie.setRatingNum(doc.getElementsByAttributeValue("property", "v:average").text());
        movie.setSubjectId(subjectId);
        movie.setRecordId(record.getId());
        movie.setSummary(doc.getElementsByAttributeValue("property", "v:summary").text().trim());
        LOG.info("Movie :《" + movie.getName() + "》 Points: " + movie.getRatingNum() + "\n" + "Summary:" + movie.getSummary());
        movieDAO.save(movie);
    }

    @Transactional
    private void crawlCommentInfo(Document doc, Record record) {
        Element el = doc.getElementById("comments");
        if (el != null) {
            String[] movies = doc.getElementsByTag("h1").text().replace(" ", "").split("短评");
            String movieName = movies[0];

            Elements items = el.select(".comment-item");
            for (Element item : items) {
                if (item.getElementsByClass("fold-bd").size() < 1 && item.children().get(1).getElementsByTag("p").size() > 0) {
                    // to make sure the current item is the comment item rather than other info item      &&      检测fold-bd是查看是否有折叠，如果是折叠的评论则有fold-bd，折叠评论是指账号有异常的
                    Comment comm = new Comment();
                    comm.setMovie(movieName);
                    //对评论内容去除4字节utf-8字符（包括Emoji表情），因为mysql utf-8编码不支持
                    //（另一方式：mysql 改用 utf8mb4）
                    String content = item.children().get(1).getElementsByTag("p").text().trim();
                    if (content.length() > Comment.ContentLength) {
                        content = content.substring(0, Comment.ContentLength);
                    }
                    content = CommonUtil.delUtf8mb4Chars(content);

                    comm.setContent(content);//use "comment.children().get(1).text()" can get all commentInfo like "1819 有用 桃桃淘电影 2016-10-29 即便评分再高也完全喜欢不来。我们还是太热衷主题与意义了，以至于忽视了传递主题的方式与合理性。影片为了所谓的人性深度，而刻意设计剧情和人物转折，忽视基本的人物行为轨迹，都非常让人不舒服。喜欢有深度的电影，但希望能以更巧妙的方式讲出来，而不该是现在这样。以及形式上，这不就是舞台搬演么"

                    comm.setVote(Integer.parseInt(item.getElementsByAttributeValue("class", "votes").text()));
                    String author = item.getElementsByAttribute("href").get(2).text();
                    comm.setAuthor(CommonUtil.delUtf8mb4Chars(author));
                    comm.setAuthorImg(item.getElementsByAttribute("href").get(2).attr("href"));
                    comm.setRecordId(record.getId());
                    comm.setSubjectId(CommonUtil.extractSubjectId(record.getUrl()));

                    LOG.info("Comment for 《" + movieName + "》:" + comm.getContent());
                    commentDAO.save(comm);
                }
            }
        }
    }
}
