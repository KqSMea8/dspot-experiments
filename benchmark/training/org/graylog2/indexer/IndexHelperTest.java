/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.indexer;


import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class IndexHelperTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testGetOldestIndices() {
        final Map<String, Integer> indexNumbers = ImmutableMap.<String, Integer>builder().put("graylog_production_1", 1).put("graylog_production_7", 7).put("graylog_production_0", 0).put("graylog_production_2", 2).put("graylog_production_4", 4).put("graylog_production_6", 6).put("graylog_production_3", 3).put("graylog_production_5", 5).put("graylog_production_8", 8).put("graylog_production_9", 9).put("graylog_production_10", 10).put("graylog_production_110", 110).put("graylog_production_125", 125).put("graylog_production_20", 20).put("graylog_production_21", 21).build();
        final IndexSet indexSet = Mockito.mock(IndexSet.class);
        Mockito.when(indexSet.extractIndexNumber(ArgumentMatchers.anyString())).thenAnswer(( invocationOnMock) -> Optional.ofNullable(indexNumbers.get(invocationOnMock.<String>getArgument(0))));
        Mockito.when(indexSet.getManagedIndices()).thenReturn(indexNumbers.keySet().toArray(new String[0]));
        Mockito.when(indexSet.getIndexPrefix()).thenReturn("graylog_production");
        assertThat(IndexHelper.getOldestIndices(indexSet, 7)).containsOnly("graylog_production_0", "graylog_production_1", "graylog_production_2", "graylog_production_3", "graylog_production_4", "graylog_production_5", "graylog_production_6");
        assertThat(IndexHelper.getOldestIndices(indexSet, 1)).containsOnly("graylog_production_0");
    }

    @Test
    public void testGetOldestIndicesWithEmptySetAndTooHighOffset() {
        final IndexSet indexSet = Mockito.mock(IndexSet.class);
        Mockito.when(indexSet.getManagedIndices()).thenReturn(new String[0]);
        assertThat(IndexHelper.getOldestIndices(indexSet, 9001)).isEmpty();
    }
}
