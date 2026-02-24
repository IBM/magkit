package de.ibmix.magkit.ui.templates;

/*-
 * #%L
 * IBM iX Magnolia Kit UI
 * %%
 * Copyright (C) 2023 IBM iX
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

import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RedirectionExceptionMapper} ensuring that the original redirect {@link Response}
 * contained in a {@link RedirectionException} is passed through unchanged (status code + Location header).
 *
 * Test strategy: Mock redirect responses (FOUND, MOVED_PERMANENTLY, TEMPORARY_REDIRECT) with distinct Location values,
 * wrap them in {@link RedirectionException} instances and assert that {@link RedirectionExceptionMapper#toResponse(RedirectionException)}
 * returns the identical response instance exposing the same status, Location header and custom header values.
 *
 * Edge cases: Different redirect status codes validated (302, 301, 307). Null response is not constructed because
 * {@link RedirectionException} requires a non-null response by contract.
 *
 * @author frank.sommer
 * @since 2025-10-30
 */
public class RedirectionExceptionMapperTest {

    @Test
    public void testToResponseReturnsOriginalResponseForFoundRedirect() {
        URI targetUri = URI.create("https://example.org/target1");
        Response original = mock(Response.class);
        when(original.getStatus()).thenReturn(Response.Status.FOUND.getStatusCode());
        when(original.getStatusInfo()).thenReturn(Response.Status.FOUND);
        when(original.getLocation()).thenReturn(targetUri);
        RedirectionException exception = new RedirectionException(original);
        RedirectionExceptionMapper mapper = new RedirectionExceptionMapper();
        Response mapped = mapper.toResponse(exception);
        assertNotNull(mapped);
        assertSame(original, mapped);
        assertEquals(Response.Status.FOUND.getStatusCode(), mapped.getStatus());
        assertEquals(targetUri, mapped.getLocation());
    }

    @Test
    public void testToResponseReturnsOriginalResponseForMovedPermanentlyRedirect() {
        URI targetUri = URI.create("https://example.org/permanent");
        Response original = mock(Response.class);
        when(original.getStatus()).thenReturn(Response.Status.MOVED_PERMANENTLY.getStatusCode());
        when(original.getStatusInfo()).thenReturn(Response.Status.MOVED_PERMANENTLY);
        when(original.getLocation()).thenReturn(targetUri);
        RedirectionException exception = new RedirectionException(original);
        RedirectionExceptionMapper mapper = new RedirectionExceptionMapper();
        Response mapped = mapper.toResponse(exception);
        assertNotNull(mapped);
        assertSame(original, mapped);
        assertEquals(Response.Status.MOVED_PERMANENTLY.getStatusCode(), mapped.getStatus());
        assertEquals(targetUri, mapped.getLocation());
    }

    @Test
    public void testToResponsePreservesHeadersAndEntity() {
        URI targetUri = URI.create("https://example.org/with-headers");
        Response original = mock(Response.class);
        when(original.getStatus()).thenReturn(Response.Status.TEMPORARY_REDIRECT.getStatusCode());
        when(original.getStatusInfo()).thenReturn(Response.Status.TEMPORARY_REDIRECT);
        when(original.getLocation()).thenReturn(targetUri);
        when(original.getHeaderString("X-Custom")).thenReturn("abc123");
        when(original.getEntity()).thenReturn("redirect-body");
        RedirectionException exception = new RedirectionException(original);
        RedirectionExceptionMapper mapper = new RedirectionExceptionMapper();
        Response mapped = mapper.toResponse(exception);
        assertNotNull(mapped);
        assertSame(original, mapped);
        assertEquals(Response.Status.TEMPORARY_REDIRECT.getStatusCode(), mapped.getStatus());
        assertEquals(targetUri, mapped.getLocation());
        assertEquals("abc123", mapped.getHeaderString("X-Custom"));
        assertEquals("redirect-body", mapped.getEntity());
    }
}
