package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Modifying
    @Query(value = "update Article set name = :name")
    void updateAllNames(String name);

    @Modifying
    @Query(value = "update Article set name = :name where id = :id")
    void updateNameById(String name, Long id);

}
