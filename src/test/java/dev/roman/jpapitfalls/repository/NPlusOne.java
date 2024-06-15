package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
import dev.roman.jpapitfalls.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The N+1 query problem occurs when one query is executed to fetch some data,
 * and then for each fetched object, an additional query is executed to fetch its related data.
 * This can lead to a significant increase in the number of database queries.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NPlusOne {

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

    @BeforeEach
    void setupTestData() {
        setupArticlesAndComments(3, 5);

        // Clearing the Hibernate session
        // to ensure all subsequent queries go directly to the database
        entityManager.clear();
    }

    /**
     *  In this method, for each article, a separate query will be executed to fetch its
     *  related comments, which is the N+1 query problem.
     */
    @Test
    @Transactional
    void showNPlusOneProblemOneToMany() {
        // Fetching all articles
        List<Article> foundArticles = articleRepository.findAll();

        // For each article, fetch and print all comments
        for (Article article : foundArticles) {
            System.out.println("Article: " + article.getName());
            // This will execute one query per article
            for (Comment comment : article.getComments()) {
                System.out.println("Comment: " + comment.getText());
            }
        }
    }

    /**
     * In this method, a subselect will be executed to avoid the N+1 problem
     */
    @Test
    @Transactional
    void solveNPlusOneWithSubSelectOneToMany() {
        // Fetching all articles
        List<Article> foundArticles = articleRepository.findAll();

        // For each article, fetch and print all comments
        for (Article article : foundArticles) {
            System.out.println("Article: " + article.getName());
            // This will execute only one query for all articles
            for (Comment comment : article.getCommentsSubSelect()) {
                System.out.println("Comment: " + comment.getText());
            }
        }
    }

    /**
     * In this code, a subselect will be executed
     */
    @Test
    @Transactional
    void showSubSelectForIndividualArticlesOneToMany() {
        // Fetching all articles
        List<Article> foundArticles = articleRepository.findAll();

        // This executes subselect for all articles that were selected earlier
        System.out.println(foundArticles.get(0).getCommentsSubSelect());
        System.out.println(foundArticles.get(1).getCommentsSubSelect());
    }

    /**
     * In this method, every comment will be fetched for an article automatically in a single query
     */
    @Test
    @Transactional
    void solveNPlusOneWithEntityGraph() {
        // Fetching all articles with @EntityGraph(attributePaths = "comments") on findAll()
        List<Article> foundArticles = articleRepositoryEntityGraph.findAll();

        // For each article, fetch and print all comments
        for (Article article : foundArticles) {
            System.out.println("Article: " + article.getName());
            for (Comment comment : article.getComments()) {
                System.out.println("Comment: " + comment.getText());
            }
        }
    }


    /**
     * In this method, every comment will be fetched for an article in separate queries
     */
    @Test
    @Transactional
    void showNPlusOneProblemManyToOne() {
        List<Comment> foundComments = commentRepository.findAll();
    }

    /**
     * In this method, every comment will be fetched for an article in a single query
     */
    @Test
    @Transactional
    void solveNPlusOneManyToOne() {
        List<Comment> foundComments = commentRepository.findAllFetchArticles();
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