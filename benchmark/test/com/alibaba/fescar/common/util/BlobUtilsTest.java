/**
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.alibaba.fescar.common.util;


import java.nio.charset.Charset;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.Test;


/**
 * The type Blob utils test.
 *
 * @author Otis.z
 * @unknown 2019 /2/26
 */
public class BlobUtilsTest {
    /**
     * Test string 2 blob.
     *
     * @throws SQLException
     * 		the sql exception
     */
    @Test
    public void testString2blob() throws SQLException {
        assertThat(BlobUtils.string2blob("123abc")).isEqualTo(new SerialBlob("123abc".getBytes(Charset.forName("UTF-8"))));
    }

    /**
     * Test blob 2 string.
     *
     * @throws SQLException
     * 		the sql exception
     */
    @Test
    public void testBlob2string() throws SQLException {
        assertThat(BlobUtils.blob2string(new SerialBlob("123absent".getBytes(Charset.forName("UTF-8"))))).isEqualTo("123absent");
    }
}
