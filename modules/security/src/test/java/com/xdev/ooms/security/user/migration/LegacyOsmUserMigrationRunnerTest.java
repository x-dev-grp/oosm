package com.xdev.ooms.security.user.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyOsmUserMigrationRunnerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private LegacyOsmUserMigrationRunner runner;

    @BeforeEach
    void setUp() {
        runner = new LegacyOsmUserMigrationRunner(jdbcTemplate);
    }

    @Test
    void run_skipsWhenLegacyTableMissing() {
        when(jdbcTemplate.queryForObject(contains("information_schema.tables"), eq(Integer.class), eq("osmuser")))
                .thenReturn(0);

        runner.run();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO oosmuser"));
    }

    @Test
    void run_renamesTableWhenOosmUserMissing() {
        when(jdbcTemplate.queryForObject(contains("information_schema.tables"), eq(Integer.class), eq("osmuser")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("information_schema.tables"), eq(Integer.class), eq("oosmuser")))
                .thenReturn(0);

        runner.run();

        verify(jdbcTemplate).execute("ALTER TABLE osmuser RENAME TO oosmuser");
        verify(jdbcTemplate, never()).update(contains("INSERT INTO oosmuser"));
    }

    @Test
    void run_copiesUsersWithExplicitColumnList() {
        when(jdbcTemplate.queryForObject(contains("information_schema.tables"), eq(Integer.class), eq("osmuser")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("information_schema.tables"), eq(Integer.class), eq("oosmuser")))
                .thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO oosmuser")))
                .thenReturn(3);
        when(jdbcTemplate.query(contains("pg_constraint"), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(contains("SELECT COUNT(*)::int"), eq(Integer.class)))
                .thenReturn(0);

        runner.run();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture());
        String insertSql = sqlCaptor.getValue();
        assertThat(insertSql).contains("photo_content_type, photo_data, username, role_id");
        assertThat(insertSql).contains("FROM osmuser o");
        assertThat(insertSql).doesNotContain("SELECT o.*");
    }
}
