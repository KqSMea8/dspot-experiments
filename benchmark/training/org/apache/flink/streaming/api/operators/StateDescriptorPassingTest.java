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
package org.apache.flink.streaming.api.operators;


import TimeCharacteristic.IngestionTime;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import java.io.File;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.junit.Test;


/**
 * Various tests around the proper passing of state descriptors to the operators
 * and their serialization.
 *
 * <p>The tests use an arbitrary generic type to validate the behavior.
 */
@SuppressWarnings("serial")
public class StateDescriptorPassingTest {
    @Test
    public void testFoldWindowState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<String> src = env.fromElements("abc");
        SingleOutputStreamOperator<?> result = src.keyBy(new org.apache.flink.api.java.functions.KeySelector<String, String>() {
            @Override
            public String getKey(String value) {
                return null;
            }
        }).timeWindow(Time.milliseconds(1000)).fold(new File("/"), new org.apache.flink.api.common.functions.FoldFunction<String, File>() {
            @Override
            public File fold(File a, String e) {
                return null;
            }
        });
        validateStateDescriptorConfigured(result);
    }

    @Test
    public void testReduceWindowState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<File> src = env.fromElements(new File("/"));
        SingleOutputStreamOperator<?> result = src.keyBy(new org.apache.flink.api.java.functions.KeySelector<File, String>() {
            @Override
            public String getKey(File value) {
                return null;
            }
        }).timeWindow(Time.milliseconds(1000)).reduce(new org.apache.flink.api.common.functions.ReduceFunction<File>() {
            @Override
            public File reduce(File value1, File value2) {
                return null;
            }
        });
        validateStateDescriptorConfigured(result);
    }

    @Test
    public void testApplyWindowState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<File> src = env.fromElements(new File("/"));
        SingleOutputStreamOperator<?> result = src.keyBy(new org.apache.flink.api.java.functions.KeySelector<File, String>() {
            @Override
            public String getKey(File value) {
                return null;
            }
        }).timeWindow(Time.milliseconds(1000)).apply(new org.apache.flink.streaming.api.functions.windowing.WindowFunction<File, String, String, TimeWindow>() {
            @Override
            public void apply(String s, TimeWindow window, Iterable<File> input, org.apache.flink.util.Collector<String> out) {
            }
        });
        validateListStateDescriptorConfigured(result);
    }

    @Test
    public void testProcessWindowState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<File> src = env.fromElements(new File("/"));
        SingleOutputStreamOperator<?> result = src.keyBy(new org.apache.flink.api.java.functions.KeySelector<File, String>() {
            @Override
            public String getKey(File value) {
                return null;
            }
        }).timeWindow(Time.milliseconds(1000)).process(new org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction<File, String, String, TimeWindow>() {
            @Override
            public void process(String s, Context ctx, Iterable<File> input, org.apache.flink.util.Collector<String> out) {
            }
        });
        validateListStateDescriptorConfigured(result);
    }

    @Test
    public void testProcessAllWindowState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<File> src = env.fromElements(new File("/"));
        SingleOutputStreamOperator<?> result = src.timeWindowAll(Time.milliseconds(1000)).process(new org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction<File, String, TimeWindow>() {
            @Override
            public void process(Context ctx, Iterable<File> input, org.apache.flink.util.Collector<String> out) {
            }
        });
        validateListStateDescriptorConfigured(result);
    }

    @Test
    public void testFoldWindowAllState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<String> src = env.fromElements("abc");
        SingleOutputStreamOperator<?> result = src.timeWindowAll(Time.milliseconds(1000)).fold(new File("/"), new org.apache.flink.api.common.functions.FoldFunction<String, File>() {
            @Override
            public File fold(File a, String e) {
                return null;
            }
        });
        validateStateDescriptorConfigured(result);
    }

    @Test
    public void testReduceWindowAllState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<File> src = env.fromElements(new File("/"));
        SingleOutputStreamOperator<?> result = src.timeWindowAll(Time.milliseconds(1000)).reduce(new org.apache.flink.api.common.functions.ReduceFunction<File>() {
            @Override
            public File reduce(File value1, File value2) {
                return null;
            }
        });
        validateStateDescriptorConfigured(result);
    }

    @Test
    public void testApplyWindowAllState() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(IngestionTime);
        env.registerTypeWithKryoSerializer(File.class, JavaSerializer.class);
        DataStream<File> src = env.fromElements(new File("/"));
        SingleOutputStreamOperator<?> result = src.timeWindowAll(Time.milliseconds(1000)).apply(new org.apache.flink.streaming.api.functions.windowing.AllWindowFunction<File, String, TimeWindow>() {
            @Override
            public void apply(TimeWindow window, Iterable<File> input, org.apache.flink.util.Collector<String> out) {
            }
        });
        validateListStateDescriptorConfigured(result);
    }
}
