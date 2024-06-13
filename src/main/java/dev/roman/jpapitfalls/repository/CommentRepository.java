package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import dev.roman.jpapitfalls.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
