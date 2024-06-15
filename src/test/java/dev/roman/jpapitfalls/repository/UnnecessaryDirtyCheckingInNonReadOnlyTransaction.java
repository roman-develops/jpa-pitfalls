package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import dev.roman.jpapitfalls.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

/**
 * This class demonstrates the potential performance issues
 * that can occur when a non-read-only transaction is used. In such cases, Hibernate performs dirty checking
 * after each transaction, which can be unnecessary and lead to performance degradation when we only need to read data.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UnnecessaryDirtyCheckingInNonReadOnlyTransaction {

    @Container
    public static PostgreSQLContainer database = new PostgreSQLContainer("postgres:latest");

    @DynamicPropertySource
    public static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleRepositoryEntityGraph articleRepositoryEntityGraph;

    @Autowired
    CommentRepository commentRepository;

    @PersistenceContext
    EntityManager entityManager;

    /**
     *  In this method, the transaction is not read-only (readOnly = false).
     *  Therefore, dirty checking is enabled, which may not be necessary in this case.
     */
    @Test
    void showProblemWithNonReadOnlyTransaction() {
        setupArticlesAndComments(100, 10);
        selectWithNonReadOnly();
    }

    /**
     *  In this method, the transaction is read-only (readOnly = true).
     *  This disables dirty checking, which can improve performance when we only need to read data.
     */
    @Test
    void showSolutionWithReadOnlyTransaction() {
        setupArticlesAndComments(100, 10);
        selectWithReadOnly();
    }

    @Transactional(readOnly = false)
    public void selectWithNonReadOnly() {
        articleRepository.findAll();
        commentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public void selectWithReadOnly() {
        articleRepository.findAll();
        commentRepository.findAll();
    }

    @Transactional
    public void setupArticlesAndComments(int numArticles, int numCommentsPerArticle) {
        List<Article> articles = new ArrayList<>();

        for (int i = 1; i <= numArticles; i++) {
            Article article = Article.builder()
                    .name("Article " + i)
                    .build();
            List<Comment> comments = new ArrayList<>();
            article.setComments(comments);
            for (int j = 1; j <= numCommentsPerArticle; j++) {
                Comment comment = Comment.builder()
                        .article(article)
                        .text("Comment " + j + " for article " + i)
                        .build();
                comments.add(comment);
            }

            articles.add(article);
            articleRepository.saveAndFlush(article);
            commentRepository.saveAllAndFlush(comments);
        }
    }

}