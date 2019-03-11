/**
 * * Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */
package com.orientechnologies.lucene.tests;


import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.junit.Test;


/**
 * Created by Enrico Risa on 10/08/15.
 */
public class OLuceneInheritanceQueryTest extends OLuceneBaseTest {
    @Test
    public void testQuery() {
        ODocument doc = new ODocument("C2");
        doc.field("name", "abc");
        db.save(doc);
        // List<ODocument> vertices = db.query(new OSQLSynchQuery<ODocument>("select from C1 where name lucene \"abc\" "));
        // 
        // Assert.assertEquals(1, vertices.size());
        OResultSet resultSet = db.query("select from C1 where search_class(\"abc\")=true ");
        assertThat(resultSet).hasSize(1);
        resultSet.close();
    }
}
