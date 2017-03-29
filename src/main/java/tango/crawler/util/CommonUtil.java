package tango.crawler.util;

import tango.crawler.entity.Record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by TANQX3 on 2017-3-25.
 */
public class CommonUtil {
    public static final String USEFUL_RE = "https://movie.douban.com/subject/\\d+(.*)";

    //public static final String COMMENT_RE = "https://movie.douban.com/subject/\\d{8}/comments";

    public static String whichType(String url) {
        Pattern p = Pattern.compile(USEFUL_RE);
        Matcher m = p.matcher(url);
        if (m.find()) {
            String suffix = m.group(1);
            if (suffix.equals("") || suffix.equals("/") || suffix.startsWith("/?from=")) {
                return Record.TYPE_MOVIE;
            }
            if (suffix.startsWith("/comments")) {
                return Record.TYPE_COMMENT;
            }
            return Record.TYPE_OTHER;
        }
        return null;
    }

    public static String delUtf8mb4Chars(String param) {
        if (param == null) {
            return null;
        }
        return param.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
    }

    public static String extractSubjectId(String url) {
        return url.split("/")[4];
    }

    public static String truncateString(String origin) {
        return truncateString(origin,Constant.DEFAULT_DATA_LENGTH);
    }

    public static String truncateString(String origin, int length) {
        if (origin.length() <= length) {
            return origin;
        }else {
            return origin.substring(0,length);
        }

    }
}
