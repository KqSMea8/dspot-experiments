/**
 * SonarQube
 * Copyright (C) 2009-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.version.v62;


import java.sql.SQLException;
import java.sql.Types;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.db.CoreDbTester;


public class CreateTableWebhookDeliveriesTest {
    private static final String TABLE = "webhook_deliveries";

    @Rule
    public final CoreDbTester dbTester = CoreDbTester.createForSchema(CreateTableWebhookDeliveriesTest.class, "empty.sql");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private CreateTableWebhookDeliveries underTest = new CreateTableWebhookDeliveries(dbTester.database());

    @Test
    public void creates_table_on_empty_db() throws SQLException {
        underTest.execute();
        assertThat(dbTester.countRowsOfTable(CreateTableWebhookDeliveriesTest.TABLE)).isEqualTo(0);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "uuid", Types.VARCHAR, 40, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "component_uuid", Types.VARCHAR, 40, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "ce_task_uuid", Types.VARCHAR, 40, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "name", Types.VARCHAR, 100, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "url", Types.VARCHAR, 2000, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "success", Types.BOOLEAN, null, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "http_status", Types.INTEGER, null, true);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "duration_ms", Types.INTEGER, null, true);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "payload", Types.CLOB, null, false);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "error_stacktrace", Types.CLOB, null, true);
        dbTester.assertColumnDefinition(CreateTableWebhookDeliveriesTest.TABLE, "created_at", Types.BIGINT, null, false);
        dbTester.assertPrimaryKey(CreateTableWebhookDeliveriesTest.TABLE, ("pk_" + (CreateTableWebhookDeliveriesTest.TABLE)), "uuid");
        dbTester.assertIndex(CreateTableWebhookDeliveriesTest.TABLE, "component_uuid", "component_uuid");
        dbTester.assertIndex(CreateTableWebhookDeliveriesTest.TABLE, "ce_task_uuid", "ce_task_uuid");
    }

    @Test
    public void migration_is_not_reentrant() throws SQLException {
        underTest.execute();
        expectedException.expect(IllegalStateException.class);
        underTest.execute();
    }
}
