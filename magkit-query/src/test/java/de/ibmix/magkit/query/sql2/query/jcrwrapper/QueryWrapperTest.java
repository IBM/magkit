package de.ibmix.magkit.query.sql2.query.jcrwrapper;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link QueryWrapper} covering fluent binding convenience methods and
 * delegation of metadata retrieval to the underlying {@link Query}.
 * <p>Test goals:</p>
 * <ul>
 *   <li>Verify each convenience method calls {@link Query#bindValue(String, Value)} with proper variable name and a value instance of expected type and content.</li>
 *   <li>Verify fluent API returns the same wrapper instance (covariant self pattern).</li>
 *   <li>Verify propagation of {@link RepositoryException} and {@link IllegalArgumentException} from underlying query.</li>
 *   <li>Verify delegation of {@link Query#getBindVariableNames()} and {@link Query#getStatement()}.</li>
 *   <li>Verify direct {@link QueryWrapper#bindValue(String, Value)} behavior including null value handling.</li>
 * </ul>
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
class QueryWrapperTest {

    /**
     * Verifies that all convenience bind methods delegate to the underlying query and keep fluent chaining.
     */
    @Test
    @DisplayName("Convenience bind methods delegate and are fluent")
    void testConvenienceBindMethods() throws RepositoryException {
        Query query = mock(Query.class);
        Calendar date = Calendar.getInstance();
        TestQueryWrapper wrapper = new TestQueryWrapper(query);

        TestQueryWrapper returned = wrapper
            .bindString("sVar", "stringValue")
            .bindLong("lVar", 123L)
            .bindDouble("dVar", 1.5d)
            .bindBoolean("bVar", true)
            .bindDate("dateVar", date);

        assertSame(wrapper, returned);

        ArgumentCaptor<Value> captor = ArgumentCaptor.forClass(Value.class);
        verify(query, times(5)).bindValue(any(String.class), captor.capture());

        Value stringValue = captor.getAllValues().get(0);
        assertEquals("stringValue", stringValue.getString());

        Value longValue = captor.getAllValues().get(1);
        assertEquals(123L, longValue.getLong());

        Value doubleValue = captor.getAllValues().get(2);
        assertEquals(1.5d, doubleValue.getDouble());

        Value booleanValue = captor.getAllValues().get(3);
        assertTrue(booleanValue.getBoolean());

        Value dateValue = captor.getAllValues().get(4);
        assertEquals(date.getTimeInMillis(), dateValue.getDate().getTimeInMillis());
    }

    /**
     * Verifies direct bindValue returns same wrapper instance and delegates.
     */
    @Test
    @DisplayName("bindValue delegates and is fluent")
    void testBindValueFluent() throws RepositoryException {
        Query query = mock(Query.class);
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        Value value = mock(Value.class);
        TestQueryWrapper returned = wrapper.bindValue("var", value);
        assertSame(wrapper, returned);
        verify(query).bindValue("var", value);
    }

    /**
     * Verifies that a null value is passed through to underlying query.
     */
    @Test
    @DisplayName("bindValue passes null value through")
    void testBindValueNullValue() throws RepositoryException {
        Query query = mock(Query.class);
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        wrapper.bindValue("nullVar", null);
        verify(query).bindValue("nullVar", null);
    }

    /**
     * Verifies RepositoryException from underlying query is propagated unchanged.
     */
    @Test
    @DisplayName("bindValue propagates RepositoryException")
    void testBindValueRepositoryExceptionPropagation() throws RepositoryException {
        Query query = mock(Query.class);
        doThrow(new RepositoryException("failing")).when(query).bindValue(eq("fail"), any(Value.class));
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        RepositoryException ex = assertThrows(RepositoryException.class, () -> wrapper.bindString("fail", "x"));
        assertEquals("failing", ex.getMessage());
    }

    /**
     * Verifies IllegalArgumentException from underlying query is propagated unchanged.
     */
    @Test
    @DisplayName("bindValue propagates IllegalArgumentException")
    void testBindValueIllegalArgumentExceptionPropagation() throws RepositoryException {
        Query query = mock(Query.class);
        doThrow(new IllegalArgumentException("bad")).when(query).bindValue(eq("illegal"), any(Value.class));
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> wrapper.bindLong("illegal", 1L));
        assertEquals("bad", ex.getMessage());
    }

    /**
     * Verifies getBindVariableNames delegates to underlying query.
     */
    @Test
    @DisplayName("getBindVariableNames delegates")
    void testGetBindVariableNames() throws RepositoryException {
        Query query = mock(Query.class);
        String[] names = new String[]{"a", "b"};
        when(query.getBindVariableNames()).thenReturn(names);
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        assertArrayEquals(names, wrapper.getBindVariableNames());
    }

    /**
     * Verifies getStatement delegates to underlying query.
     */
    @Test
    @DisplayName("getStatement delegates")
    void testGetStatement() {
        Query query = mock(Query.class);
        when(query.getStatement()).thenReturn("SELECT * FROM [nt:base]");
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        assertEquals("SELECT * FROM [nt:base]", wrapper.getStatement());
    }

    /**
     * Verifies that the protected getQuery() method delegates correctly to the underlying query.
     */
    @Test
    @DisplayName("getQuery protected method delegates correctly")
    void testProtectedGetQueryDelegation() {
        Query query = mock(Query.class);
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        assertSame(query, wrapper.query());
    }

    /**
     * Verifies IllegalArgumentException propagation when null varName is passed.
     */
    @Test
    @DisplayName("bindValue propagates IllegalArgumentException for null varName")
    void testBindValueNullVarNamePropagation() throws RepositoryException {
        Query query = mock(Query.class);
        doThrow(new IllegalArgumentException("null name"))
            .when(query).bindValue(eq(null), any(Value.class));
        TestQueryWrapper wrapper = new TestQueryWrapper(query);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> wrapper.bindString(null, "x"));
        assertEquals("null name", ex.getMessage());
    }

    /**
     * Test specific concrete implementation of QueryWrapper for unit testing.
     */
    private static final class TestQueryWrapper extends QueryWrapper<TestQueryWrapper> {

        TestQueryWrapper(Query query) {
            super(query);
        }

        @Override
        TestQueryWrapper me() {
            return this;
        }

        Query query() {
            return getQuery();
        }
    }
}
