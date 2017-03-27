package tango.crawler.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tango.crawler.entity.Record;

/**
 * Created by TANQX3 on 2017-3-22.
 */
@Repository
public interface RecordDAO extends JpaRepository<Record,Integer> {
    Record getByUrl(String url);

    Record getFirstByCrawled(Integer crawled);
}
