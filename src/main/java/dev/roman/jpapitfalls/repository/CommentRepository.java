package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import dev.roman.jpapitfalls.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("from Comment c join fetch c.article")
    List<Comment> findAllFetchArticles();

}
