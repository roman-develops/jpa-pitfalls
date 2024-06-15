package dev.roman.jpapitfalls.repository;

import dev.roman.jpapitfalls.entity.Article;
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

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This class demonstrates the behavior of the persistence context in JPA after executing an update query.
 * It shows that the persistence context does not automatically refresh after an update query, which can
 * lead to outdated data being present in the persistence context.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContextDoesntRefreshAutomaticallyAfterUpdateQuery {

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

    @PersistenceContext
    EntityManager entityManager;

    /**
     * This test demonstrates that the persistence context does not automatically refresh after an update query.
     * As a result, the old name of the articles is still present in the persistence context, even though it has been updated in the database.
     */
    @Test
    @Transactional
    void showNoAutomaticRefreshAfterUpdate() {
        List<Article> savedArticles = articleRepository
                .saveAll(generateArticles("Old name", 10));

        // Update the name of all articles in the database
        articleRepository.updateAllNames("New name");

        for (Article savedArticle : savedArticles) {
            // The old name is still present in the persistence context
            assertEquals("Old name", savedArticle.getName());
        }
    }

    /**
     * This test demonstrates that the persistence context does not automatically refresh after an update query for a single entity.
     * As a result, the old name of the article is still present in the persistence context, even though it has been updated in the database.
     */
    @Test
    @Transactional
    void showNoAutomaticRefreshAfterSingleEntityUpdate() {
        Article savedArticle =
                articleRepository.save(Article.builder()
                        .name("Old name")
                        .build());

        // Update the name of the article in the database
        articleRepository.updateNameById("New name", savedArticle.getId());

        // The old name is still present in the persistence context
        assertEquals("Old name", savedArticle.getName());
    }

    /**
     * This test demonstrates how to manually refresh the persistence context after an update query.
     * After the refresh, the new name of the articles is present in the persistence context.
     */
    @Test
    @Transactional
    void showManualRefreshAfterUpdate() {
        List<Article> savedArticles = articleRepository
                .saveAll(generateArticles("Old name", 10));

        // Update the name of all articles in the database
        articleRepository.updateAllNames("New name");

        for (Article savedArticle : savedArticles) {
            entityManager.refresh(savedArticle);

            // The new name is now present in the persistence context
            assertEquals("New name", savedArticle.getName());
        }
    }

    /**
     * This test demonstrates how to manually refresh the persistence context after an update query for a single entity.
     * After the refresh, the new name of the article is present in the persistence context.
     */
    @Test
    @Transactional
    void showManualRefreshAfterSingleEntityUpdate() {
        Article savedArticle =
                articleRepository.save(Article.builder()
                        .name("Old name")
                        .build());

        // Update the name of the article in the database
        articleRepository.updateNameById("New name", savedArticle.getId());

        entityManager.refresh(savedArticle);

        // The new name is now present in the persistence context
        assertEquals("New name", savedArticle.getName());
    }

    List<Article> generateArticles(String withName, int count) {
        ArrayList<Article> articles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            articles.add(Article.builder()
                    .name(withName)
                    .build());
        }
        return articles;
    }

}
