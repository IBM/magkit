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

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ResultWrapper} verifying behavior of column and selector accessor methods
 * under normal and exceptional conditions.
 * <p>Test goals:</p>
 * <ul>
 *   <li>Ensure getColumnNames returns provided column names from underlying {@link QueryResult}.</li>
 *   <li>Ensure getSelectorNames returns provided selector names from underlying {@link QueryResult}.</li>
 *   <li>Ensure both accessor methods return an empty array when a {@link RepositoryException} occurs.</li>
 *   <li>Ensure protected getResult() correctly exposes the wrapped {@link QueryResult} to subclasses.</li>
 * </ul>
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-29
 */
class ResultWrapperTest {
    /**
     * Verifies getColumnNames returns underlying column names when no exception is thrown.
     */
    @Test
    @DisplayName("getColumnNames returns underlying values")
    void testGetColumnNamesNormal() throws RepositoryException {
        QueryResult queryResult = mock(QueryResult.class);
        String[] expected = new String[]{"colA", "colB"};
        when(queryResult.getColumnNames()).thenReturn(expected);
        TestResultWrapper wrapper = new TestResultWrapper(queryResult);
        assertArrayEquals(expected, wrapper.getColumnNames());
    }

    /**
     * Verifies getColumnNames returns empty array when RepositoryException is thrown.
     */
    @Test
    @DisplayName("getColumnNames returns empty array on RepositoryException")
    void testGetColumnNamesException() throws RepositoryException {
        QueryResult queryResult = mock(QueryResult.class);
        doThrow(new RepositoryException("failure")).when(queryResult).getColumnNames();
        TestResultWrapper wrapper = new TestResultWrapper(queryResult);
        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY, wrapper.getColumnNames());
    }

    /**
     * Verifies getSelectorNames returns underlying selector names when no exception is thrown.
     */
    @Test
    @DisplayName("getSelectorNames returns underlying values")
    void testGetSelectorNamesNormal() throws RepositoryException {
        QueryResult queryResult = mock(QueryResult.class);
        String[] expected = new String[]{"selA", "selB"};
        when(queryResult.getSelectorNames()).thenReturn(expected);
        TestResultWrapper wrapper = new TestResultWrapper(queryResult);
        assertArrayEquals(expected, wrapper.getSelectorNames());
    }

    /**
     * Verifies getSelectorNames returns empty array when RepositoryException is thrown.
     */
    @Test
    @DisplayName("getSelectorNames returns empty array on RepositoryException")
    void testGetSelectorNamesException() throws RepositoryException {
        QueryResult queryResult = mock(QueryResult.class);
        doThrow(new RepositoryException("failure")).when(queryResult).getSelectorNames();
        TestResultWrapper wrapper = new TestResultWrapper(queryResult);
        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY, wrapper.getSelectorNames());
    }

    /**
     * Verifies protected getResult method exposes the originally wrapped QueryResult instance.
     */
    @Test
    @DisplayName("protected getResult exposes wrapped instance")
    void testProtectedGetResult() {
        QueryResult queryResult = mock(QueryResult.class);
        TestResultWrapper wrapper = new TestResultWrapper(queryResult);
        assertSame(queryResult, wrapper.exposedResult());
    }

    /**
     * Concrete minimal implementation for testing abstract base class behavior.
     */
    private static final class TestResultWrapper extends ResultWrapper {
        TestResultWrapper(QueryResult result) {
            super(result);
        }

        QueryResult exposedResult() {
            return getResult();
        }
    }
}

