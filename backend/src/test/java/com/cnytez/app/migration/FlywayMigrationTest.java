package com.cnytez.app.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("migration")
@SpringBootTest
class FlywayMigrationTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine");

    static {
        POSTGRES.start();
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        )) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/legacy-schema.sql")
            );
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.baseline-version", () -> "1");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("image-server.url", () -> "http://localhost:8123");
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesAndValidatesEveryMigrationAfterTheLegacyBaseline() {
        var validation = flyway.validateWithResult();
        var allDiscovered = flyway.info().all();
        var applied = flyway.info().applied();
        var pending = flyway.info().pending();

        assertThat(validation.validationSuccessful).isTrue();
        assertThat(pending).isEmpty();
        assertThat(applied)
                .isNotEmpty()
                .hasSameSizeAs(allDiscovered);
        assertThat(applied[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(applied[applied.length - 1].getVersion())
                .isEqualTo(allDiscovered[allDiscovered.length - 1].getVersion());
    }

    @Test
    void preservesLegacyDataAndRemovesLegacyColumns() {
        String profilePhoto = jdbcTemplate.queryForObject(
                "SELECT profile_photo_url FROM users WHERE username = 'legacy-user'",
                String.class
        );
        Integer filterId = jdbcTemplate.queryForObject(
                "SELECT filter_id FROM posts WHERE title = 'Legacy post'",
                Integer.class
        );
        Integer tokenVersion = jdbcTemplate.queryForObject(
                "SELECT token_version FROM users WHERE username = 'legacy-user'",
                Integer.class
        );
        Integer legacyColumnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND column_name IN ('profile_photourl', 'filter', 'creation_date', 'deletion_date')
                """,
                Integer.class
        );

        assertThat(profilePhoto).isEqualTo("https://legacy.example/profile.png");
        assertThat(filterId).isEqualTo(2);
        assertThat(tokenVersion).isZero();
        assertThat(legacyColumnCount).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM filters",
                Integer.class
        )).isEqualTo(6);
    }
}

