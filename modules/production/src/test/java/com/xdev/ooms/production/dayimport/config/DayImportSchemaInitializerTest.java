package com.xdev.ooms.production.dayimport.config;

import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class DayImportSchemaInitializerTest {

    @Test
    void appliesIdempotentScriptOnStartup() throws Exception {
        Statement statement = mock(Statement.class);
        DataSource dataSource = dataSourceWith(statement);

        new DayImportSchemaInitializer(dataSource, true).ensureSchema();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(statement, atLeastOnce()).execute(sql.capture());
        List<String> executed = sql.getAllValues();
        assertThat(executed).anyMatch(s -> s.contains("CREATE TABLE IF NOT EXISTS day_import_run"));
        assertThat(executed).anyMatch(s -> s.contains("CREATE TABLE IF NOT EXISTS day_import_operation"));
        for (String table : List.of("storage_unit", "oil_container", "unified_delivery")) {
            assertThat(executed).anyMatch(s -> s.contains("ALTER TABLE IF EXISTS " + table)
                    && s.contains("ADD COLUMN IF NOT EXISTS balance_version"));
        }
        assertThat(executed).allMatch(s -> !s.toUpperCase().startsWith("CREATE TABLE ")
                || s.toUpperCase().startsWith("CREATE TABLE IF NOT EXISTS"));
    }

    @Test
    void doesNothingWhenDisabled() {
        DataSource dataSource = mock(DataSource.class);

        new DayImportSchemaInitializer(dataSource, false).ensureSchema();

        verifyNoInteractions(dataSource);
    }

    @Test
    void databaseFailureDoesNotStopStartup() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        assertThatCode(() -> new DayImportSchemaInitializer(dataSource, true).ensureSchema())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(classes = {StorageUnit.class, OilContainer.class, UnifiedDelivery.class})
    void balanceVersionColumnHasDatabaseDefault(Class<?> entity) throws Exception {
        Column column = entity.getDeclaredField("balanceVersion").getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo("balance_version");
        assertThat(column.columnDefinition()).containsIgnoringCase("default 0");
    }

    private static DataSource dataSourceWith(Statement statement) throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.createStatement()).thenReturn(statement);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }
}
