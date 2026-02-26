package de.ibmix.magkit.notfound;

/*-
 * #%L
 * magkit-notfound
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

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PageRenderingErrorMapping} covering constructor component retrieval and response mapping logic.
 * <p>
 * Tested aspects:
 * <ul>
 *   <li>Delegation to {@link ErrorService#createEntity(int, String)} with correct status code and path.</li>
 *   <li>Propagation of HTTP status code from {@link PageRenderingException} to JAX-RS {@link Response}.</li>
 *   <li>Handling of different status codes and paths.</li>
 * </ul>
 * </p>
 * <p>
 * The Magnolia {@link ErrorService} is mocked via component registration to validate interaction without relying
 * on its internal logic.
 * </p>
 *
 * @author wolf.bubenik
 * @since 2025-10-21
 */
public class PageRenderingErrorMappingTest {

    private ErrorService _errorService;
    private PageRenderingErrorMapping _mapper;

    @BeforeEach
    public void setUp() {
        cleanContext();
        _errorService = mockComponentInstance(ErrorService.class);
        when(_errorService.createEntity(anyInt(), anyString())).thenReturn(Map.of("status", -1, "page", "unused"));
        _mapper = new PageRenderingErrorMapping();
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void maps404StatusAndPathUsingErrorService() {
        Map<String, Object> errorEntity = Map.of("status", 404, "page", "/foo/bar");
        when(_errorService.createEntity(404, "/foo/bar")).thenReturn(errorEntity);
        PageRenderingException exception = new PageRenderingException(404, "/foo/bar");
        Response response = _mapper.toResponse(exception);
        assertEquals(404, response.getStatus());
        assertEquals(errorEntity, response.getEntity());
        verify(_errorService).createEntity(404, "/foo/bar");
    }

    @Test
    public void mapsDifferentStatusCodesIndependently() {
        Map<String, Object> entity418 = Map.of("status", 418, "page", "/tea/pot");
        when(_errorService.createEntity(418, "/tea/pot")).thenReturn(entity418);
        PageRenderingException exception418 = new PageRenderingException(418, "/tea/pot");
        Response response418 = _mapper.toResponse(exception418);
        assertEquals(418, response418.getStatus());
        assertEquals(entity418, response418.getEntity());
        verify(_errorService).createEntity(418, "/tea/pot");
    }

    @Test
    public void maps400StatusAndPathUsingErrorService() {
        Map<String, Object> errorEntity = Map.of("status", 400, "page", "/foo/error");
        when(_errorService.createEntity(400, "/foo/error")).thenReturn(errorEntity);
        PageRenderingException exception = new PageRenderingException(400, "/foo/error");
        Response response = _mapper.toResponse(exception);
        assertEquals(400, response.getStatus());
        assertEquals(errorEntity, response.getEntity());
        verify(_errorService).createEntity(400, "/foo/error");
    }
}
