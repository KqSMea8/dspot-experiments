/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.io.gcp.bigquery;


import Validation.Required;
import java.util.Map;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.ExperimentalOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.testing.TestPipelineOptions;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Integration test for BigQueryIORead. It reads from a specified table and then asserts that the
 * number of records read equals the given expected number of records.
 */
@RunWith(JUnit4.class)
public class BigQueryIOReadIT {
    private BigQueryIOReadIT.BigQueryIOReadOptions options;

    private String project;

    private static final String datasetId = "big_query_import_export";

    private static final String tablePrefix = "export_";

    private static final Map<String, Long> numOfRecords = ImmutableMap.<String, Long>of("empty", 0L, "1M", 10592L, "1G", 11110839L, "1T", 11110839000L);

    /**
     * Customized PipelineOption for BigQueryIORead Pipeline.
     */
    public interface BigQueryIOReadOptions extends ExperimentalOptions , TestPipelineOptions {
        @Description("The table to be read")
        @Validation.Required
        String getInputTable();

        void setInputTable(String filename);

        @Description("The expected number of records")
        @Validation.Required
        long getNumRecords();

        void setNumRecords(long numRecords);
    }

    @Test
    public void testBigQueryReadEmpty() throws Exception {
        setupTestEnvironment("empty", false);
        runBigQueryIOReadPipeline();
    }

    @Test
    public void testBigQueryRead1M() throws Exception {
        setupTestEnvironment("1M", false);
        runBigQueryIOReadPipeline();
    }

    @Test
    public void testBigQueryRead1G() throws Exception {
        setupTestEnvironment("1G", false);
        runBigQueryIOReadPipeline();
    }

    @Test
    public void testBigQueryRead1T() throws Exception {
        setupTestEnvironment("1T", false);
        runBigQueryIOReadPipeline();
    }

    @Test
    public void testBigQueryReadEmptyCustom() throws Exception {
        setupTestEnvironment("empty", true);
        runBigQueryIOReadPipeline();
    }

    @Test
    public void testBigQueryRead1TCustom() throws Exception {
        setupTestEnvironment("1T", true);
        runBigQueryIOReadPipeline();
    }
}
