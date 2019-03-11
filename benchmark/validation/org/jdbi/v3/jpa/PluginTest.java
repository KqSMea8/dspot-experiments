/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.jpa;


import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import org.jdbi.v3.core.rule.H2DatabaseRule;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.junit.Rule;
import org.junit.Test;


public class PluginTest {
    @Rule
    public H2DatabaseRule dbRule = new H2DatabaseRule().withSomething().withPlugins();

    @Entity
    static class Thing {
        private int id;

        private String name;

        Thing() {
        }

        Thing(int id, String name) {
            setId(id);
            setName(name);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PluginTest.Thing) {
                PluginTest.Thing thing = ((PluginTest.Thing) (o));
                return ((id) == (thing.id)) && (Objects.equals(name, thing.name));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    public interface ThingDao {
        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        void insert(@BindJpa
        PluginTest.Thing thing);

        @SqlQuery("select id, name from something")
        List<PluginTest.Thing> list();
    }

    @Test
    public void testPluginInstallsJpaMapper() {
        PluginTest.Thing brian = new PluginTest.Thing(1, "Brian");
        PluginTest.Thing keith = new PluginTest.Thing(2, "Keith");
        PluginTest.ThingDao dao = dbRule.getSharedHandle().attach(PluginTest.ThingDao.class);
        dao.insert(brian);
        dao.insert(keith);
        List<PluginTest.Thing> rs = dao.list();
        assertThat(rs).containsOnlyOnce(brian, keith);
    }
}
