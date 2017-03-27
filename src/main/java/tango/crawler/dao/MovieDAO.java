package tango.crawler.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tango.crawler.entity.Movie;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by TANQX3 on 2017-3-22.
 */
@Repository
public interface MovieDAO extends JpaRepository<Movie,Integer>{
    Movie findBySubjectId(String subjectId);
}
