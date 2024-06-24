package com.news.scanner.repositories;

import com.news.scanner.entity.News;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsRepository extends MongoRepository<News, String> {
    Optional<News> findByUrl(String url);
}
