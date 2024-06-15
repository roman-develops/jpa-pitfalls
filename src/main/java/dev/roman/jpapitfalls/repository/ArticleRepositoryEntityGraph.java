package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepositoryEntityGraph extends JpaRepository<Article, Long> {

    @EntityGraph(attributePaths = "comments")
    List<Article> findAll();

}
