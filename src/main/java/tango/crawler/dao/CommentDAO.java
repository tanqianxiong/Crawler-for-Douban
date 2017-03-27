package tango.crawler.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tango.crawler.entity.Comment;

/**
 * Created by TANQX3 on 2017-3-25.
 */
@Repository
public interface CommentDAO extends JpaRepository<Comment,Integer> {
}
