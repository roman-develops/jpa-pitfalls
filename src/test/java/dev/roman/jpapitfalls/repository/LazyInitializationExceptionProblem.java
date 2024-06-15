package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import dev.roman.jpapitfalls.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

/**
 * This class demonstrates LazyInitializationException
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LazyInitializationExceptionProblem {

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
    CommentRepository commentRepository;

    @PersistenceContext
    EntityManager entityManager;

    @BeforeEach
    void setupTestData() {
        setupArticlesAndComments(3, 5);

        // Clearing the Hibernate session
        // to ensure all subsequent queries go directly to the database
        entityManager.clear();
    }

    /**
     * This test method demonstrates the occurrence of LazyInitializationException
     * when trying to access uninitialized proxy or collection outside a session.
     */
    @Transactional(propagation = Propagation.NEVER)
    @Test
    public void showLazyInitializationException() {
        Assertions.assertThrows(LazyInitializationException.class, () -> {
            List<Comment> comments = articleRepository.findAll().get(0).getComments();
            System.out.println(comments);
        });
    }

    /**
     * This test method demonstrates how to avoid LazyInitializationException
     * by accessing uninitialized proxy or collection within a session.
     */
    @Transactional
    @Test
    public void showSolutionToLazyInitializationException() {
        List<Comment> comments = articleRepository.findAll().get(0).getComments();
        System.out.println(comments);
    }

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