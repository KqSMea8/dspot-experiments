/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.confluent.ksql.rest.server;


import io.confluent.common.utils.IntegrationTest;
import io.confluent.ksql.integration.IntegrationTestHarness;
import io.confluent.ksql.rest.server.computation.ConfigStore;
import io.confluent.ksql.util.KsqlException;
import io.confluent.ksql.util.KsqlStatementException;
import io.confluent.ksql.version.metrics.VersionCheckerAgent;
import java.nio.file.Path;
import org.apache.kafka.connect.data.Schema;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@Category({ IntegrationTest.class })
@RunWith(MockitoJUnitRunner.class)
public class StandaloneExecutorFunctionalTest {
    @ClassRule
    public static final IntegrationTestHarness TEST_HARNESS = IntegrationTestHarness.build();

    @ClassRule
    public static final TemporaryFolder TMP = new TemporaryFolder();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final String AVRO_TOPIC = "avro-topic";

    private static final String JSON_TOPIC = "json-topic";

    private static int DATA_SIZE;

    private static Schema DATA_SCHEMA;

    @Mock
    private VersionCheckerAgent versionChecker;

    @Mock
    private ConfigStore configStore;

    private Path queryFile;

    private StandaloneExecutor standalone;

    private String s1;

    private String s2;

    private String t1;

    @Test
    public void shouldHandleJsonWithSchemas() {
        // Given:
        givenScript((((((((((((((((((((((((((((("" + ("CREATE STREAM S (ORDERTIME BIGINT)" + "    WITH (kafka_topic='")) + (StandaloneExecutorFunctionalTest.JSON_TOPIC)) + "\', value_format=\'json\');\n") + "\n") + "CREATE TABLE T (ORDERTIME BIGINT) ") + "    WITH (kafka_topic='") + (StandaloneExecutorFunctionalTest.JSON_TOPIC)) + "\', value_format=\'json\', key=\'ORDERTIME\');\n") + "\n") + "SET 'auto.offset.reset' = 'earliest';") + "\n") + "CREATE STREAM ") + (s1)) + " AS SELECT * FROM S;\n") + "\n") + "INSERT INTO ") + (s1)) + " SELECT * FROM S;\n") + "\n") + "CREATE TABLE ") + (t1)) + " AS SELECT * FROM T;\n") + "\n") + "UNSET 'auto.offset.reset';") + "\n") + "CREATE STREAM ") + (s2)) + " AS SELECT * FROM S;\n"));
        // When:
        standalone.start();
        // Then:
        // CSAS and INSERT INTO both input into S1:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableRows(s1, ((StandaloneExecutorFunctionalTest.DATA_SIZE) * 2), JSON, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
        // CTAS only into T1:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableUniqueRows(t1, StandaloneExecutorFunctionalTest.DATA_SIZE, JSON, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
        // S2 should be empty as 'auto.offset.reset' unset:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableUniqueRows(s2, 0, JSON, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
    }

    @Test
    public void shouldHandleAvroWithSchemas() {
        // Given:
        givenScript((((((((((((((((((((((((((((("" + ("CREATE STREAM S (ORDERTIME BIGINT)" + "    WITH (kafka_topic='")) + (StandaloneExecutorFunctionalTest.AVRO_TOPIC)) + "\', value_format=\'avro\');\n") + "\n") + "CREATE TABLE T (ORDERTIME BIGINT) ") + "    WITH (kafka_topic='") + (StandaloneExecutorFunctionalTest.AVRO_TOPIC)) + "\', value_format=\'avro\', key=\'ORDERTIME\');\n") + "\n") + "SET 'auto.offset.reset' = 'earliest';") + "\n") + "CREATE STREAM ") + (s1)) + " AS SELECT * FROM S;\n") + "\n") + "INSERT INTO ") + (s1)) + " SELECT * FROM S;\n") + "\n") + "CREATE TABLE ") + (t1)) + " AS SELECT * FROM T;\n") + "\n") + "UNSET 'auto.offset.reset';") + "\n") + "CREATE STREAM ") + (s2)) + " AS SELECT * FROM S;\n"));
        // When:
        standalone.start();
        // Then:
        // CSAS and INSERT INTO both input into S1:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableRows(s1, ((StandaloneExecutorFunctionalTest.DATA_SIZE) * 2), AVRO, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
        // CTAS only into T1:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableUniqueRows(t1, StandaloneExecutorFunctionalTest.DATA_SIZE, AVRO, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
        // S2 should be empty as 'auto.offset.reset' unset:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableUniqueRows(s2, 0, AVRO, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
    }

    @Test
    public void shouldInferAvroSchema() {
        // Given:
        givenScript(((((((("" + (("SET 'auto.offset.reset' = 'earliest';" + "") + "CREATE STREAM S WITH (kafka_topic='")) + (StandaloneExecutorFunctionalTest.AVRO_TOPIC)) + "\', value_format=\'avro\');\n") + "") + "CREATE STREAM ") + (s1)) + " AS SELECT * FROM S;"));
        // When:
        standalone.start();
        // Then:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableRows(s1, StandaloneExecutorFunctionalTest.DATA_SIZE, AVRO, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
    }

    @Test
    public void shouldFailOnAvroWithoutSchemasIfSchemaNotAvailable() {
        // Given:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.ensureTopics("topic-without-schema");
        givenScript(("" + (("SET 'auto.offset.reset' = 'earliest';" + "") + "CREATE STREAM S WITH (kafka_topic='topic-without-schema', value_format='avro');")));
        // Then:
        expectedException.expect(KsqlException.class);
        expectedException.expectMessage("Schema registry fetch for topic topic-without-schema request failed");
        // When:
        standalone.start();
    }

    @Test
    public void shouldFailOnAvroWithoutSchemasIfSchemaNotEvolvable() {
        // Given:
        StandaloneExecutorFunctionalTest.givenIncompatibleSchemaExists(s1);
        givenScript(((((((("" + (("SET 'auto.offset.reset' = 'earliest';" + "") + "CREATE STREAM S WITH (kafka_topic='")) + (StandaloneExecutorFunctionalTest.AVRO_TOPIC)) + "\', value_format=\'avro\');\n") + "") + "CREATE STREAM ") + (s1)) + " AS SELECT * FROM S;"));
        // Then:
        expectedException.expect(KsqlStatementException.class);
        expectedException.expectMessage("schema evolution issues");
        // When:
        standalone.start();
    }

    @Test
    public void shouldHandleComments() {
        // Given:
        givenScript(((((((("" + ((((((((("-- Single line comment\n" + "") + "/*\n") + "Multi-line comment\n") + " */\n") + "") + "SET 'auto.offset.reset' = 'earliest';") + "") + "CREATE STREAM S /*inline comment*/ (ID int)") + "    with (kafka_topic='")) + (StandaloneExecutorFunctionalTest.JSON_TOPIC)) + "\',value_format=\'json\');\n") + "\n") + "CREATE STREAM ") + (s1)) + "  AS SELECT * FROM S;"));
        // When:
        standalone.start();
        // Then:
        StandaloneExecutorFunctionalTest.TEST_HARNESS.verifyAvailableRows(s1, StandaloneExecutorFunctionalTest.DATA_SIZE, JSON, StandaloneExecutorFunctionalTest.DATA_SCHEMA);
    }
}
