package tango.crawler.entity;

import javax.persistence.*;

/**
 * Created by TANQX3 on 2017-3-22.
 */
@Entity
@Table(name = "record")
public class Record {
    public static final String TYPE_MOVIE = "Movie";
    public static final String TYPE_COMMENT = "Comment";
    public static final String TYPE_OTHER = "Other";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String url;
    private Integer crawled;
    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getCrawled() {
        return crawled;
    }

    public void setCrawled(Integer crawled) {
        this.crawled = crawled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
