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
package org.apache.flink.metrics.prometheus.tests;


import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.prometheus.PrometheusReporter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.tests.util.AutoClosableProcess;
import org.apache.flink.tests.util.CommandLineWrapper;
import org.apache.flink.tests.util.FlinkDistribution;
import org.apache.flink.util.OperatingSystem;
import org.apache.flink.util.TestLogger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * End-to-end test for the PrometheusReporter.
 */
public class PrometheusReporterEndToEndITCase extends TestLogger {
    private static final Logger LOG = LoggerFactory.getLogger(PrometheusReporterEndToEndITCase.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String PROMETHEUS_VERSION = "2.4.3";

    private static final String PROMETHEUS_FILE_NAME;

    static {
        final String base = ("prometheus-" + (PrometheusReporterEndToEndITCase.PROMETHEUS_VERSION)) + '.';
        switch (OperatingSystem.getCurrentOperatingSystem()) {
            case MAC_OS :
                PROMETHEUS_FILE_NAME = base + "darwin-amd64";
                break;
            case WINDOWS :
                PROMETHEUS_FILE_NAME = base + "windows-amd64";
                break;
            default :
                PROMETHEUS_FILE_NAME = base + "linux-amd64";
                break;
        }
    }

    private static final Pattern LOG_REPORTER_PORT_PATTERN = Pattern.compile(".*Started PrometheusReporter HTTP server on port ([0-9]+).*");

    @Rule
    public final FlinkDistribution dist = new FlinkDistribution();

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testReporter() throws Exception {
        dist.copyOptJarsToLib("flink-metrics-prometheus");
        final Configuration config = new Configuration();
        config.setString((((ConfigConstants.METRICS_REPORTER_PREFIX) + "prom.") + (ConfigConstants.METRICS_REPORTER_CLASS_SUFFIX)), PrometheusReporter.class.getCanonicalName());
        config.setString(((ConfigConstants.METRICS_REPORTER_PREFIX) + "prom.port"), "9000-9100");
        dist.appendConfiguration(config);
        final Path tmpPrometheusDir = tmp.newFolder().toPath().resolve("prometheus");
        final Path prometheusArchive = tmpPrometheusDir.resolve(((PrometheusReporterEndToEndITCase.PROMETHEUS_FILE_NAME) + ".tar.gz"));
        final Path prometheusBinDir = tmpPrometheusDir.resolve(PrometheusReporterEndToEndITCase.PROMETHEUS_FILE_NAME);
        final Path prometheusConfig = prometheusBinDir.resolve("prometheus.yml");
        final Path prometheusBinary = prometheusBinDir.resolve("prometheus");
        Files.createDirectory(tmpPrometheusDir);
        runBlocking("Download of Prometheus", Duration.ofMinutes(5), CommandLineWrapper.wget(((("https://github.com/prometheus/prometheus/releases/download/v" + (PrometheusReporterEndToEndITCase.PROMETHEUS_VERSION)) + '/') + (prometheusArchive.getFileName()))).targetDir(tmpPrometheusDir).build());
        runBlocking("Extraction of Prometheus archive", CommandLineWrapper.tar(prometheusArchive).extract().zipped().targetDir(tmpPrometheusDir).build());
        runBlocking("Set Prometheus scrape interval", CommandLineWrapper.sed("s/\\(scrape_interval:\\).*/\\1 1s/", prometheusConfig).inPlace().build());
        dist.startFlinkCluster();
        final List<Integer> ports = dist.searchAllLogs(PrometheusReporterEndToEndITCase.LOG_REPORTER_PORT_PATTERN, ( matcher) -> matcher.group(1)).map(Integer::valueOf).collect(Collectors.toList());
        final String scrapeTargets = ports.stream().map(( port) -> ("'localhost:" + port) + "'").collect(Collectors.joining(", "));
        runBlocking((("Set Prometheus scrape targets to (" + scrapeTargets) + ")"), CommandLineWrapper.sed((("s/\\(targets:\\).*/\\1 [" + scrapeTargets) + "]/"), prometheusConfig).inPlace().build());
        try (AutoClosableProcess prometheus = runNonBlocking("Start Prometheus server", prometheusBinary.toAbsolutePath().toString(), ("--config.file=" + (prometheusConfig.toAbsolutePath())), ("--storage.tsdb.path=" + (prometheusBinDir.resolve("data").toAbsolutePath())))) {
            final OkHttpClient client = new OkHttpClient();
            PrometheusReporterEndToEndITCase.checkMetricAvailability(client, "flink_jobmanager_numRegisteredTaskManagers");
            PrometheusReporterEndToEndITCase.checkMetricAvailability(client, "flink_taskmanager_Status_Network_TotalMemorySegments");
        }
    }
}
