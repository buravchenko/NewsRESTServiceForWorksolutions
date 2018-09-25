package ru.worksolutions.newsrestservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import ru.worksolutions.newsrestservice.models.MeduzaNews;

import java.util.List;

public interface MeduzaNewsRepository extends MongoRepository<MeduzaNews, String> {

    List<MeduzaNews> findNewsByDay(@Param("day") String day);

    MeduzaNews findNewsByNewsID(@Param("newsID") String newsID);
}
