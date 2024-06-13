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

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContextDoesntRefreshAutomatically {

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

    @Test
    @Transactional
    void updateAllArticlesNamesWithoutCacheRefresh() {
        List<Article> savedArticles = articleRepository
                .saveAll(generateArticles("Old name", 10));

        // update Article set name = :name
        articleRepository.updateAllNames("New name");

        for (Article savedArticle : savedArticles) {
            // We expect that name == 'Old name' instead of 'New name' because persistence context
            // doesn't refresh automatically after update query
            assertEquals("Old name", savedArticle.getName());
        }
    }

    @Test
    @Transactional
    void updateArticleNameWithoutCacheRefresh() {
        Article savedArticle =
                articleRepository.save(Article.builder()
                        .name("Old name")
                        .build());

        // update Article set name = :name
        articleRepository.updateNameById("New name", savedArticle.getId());

        // We expect that name == 'Old name' instead of 'New name' because persistence context
        // doesn't refresh automatically after update query
        assertEquals("Old name", savedArticle.getName());
    }

    @Test
    @Transactional
    void updateAllArticlesNamesWithCacheRefresh() {
        List<Article> savedArticles = articleRepository
                .saveAll(generateArticles("Old name", 10));

        // update Article set name = :name
        articleRepository.updateAllNames("New name");

        for (Article savedArticle : savedArticles) {
            entityManager.refresh(savedArticle);

            // We expect that name == 'New name' because we refresh persistence context
            // We also can use clearAutomatically in modifying annotation and get the articles again
            assertEquals("New name", savedArticle.getName());
        }
    }

    @Test
    @Transactional
    void updateArticleNameWithCacheRefresh() {
        Article savedArticle =
                articleRepository.save(Article.builder()
                        .name("Old name")
                        .build());

        // update Article set name = :name
        articleRepository.updateNameById("New name", savedArticle.getId());

        entityManager.refresh(savedArticle);

        // We expect that name == 'New name' because we refresh persistence context
        // We also can use clearAutomatically in modifying annotation and get the articles again
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
