package de.ibmix.magkit.notfound;

/*-
 * #%L
 * magkit-notfound
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

import lombok.Getter;
import lombok.NonNull;

import javax.jcr.RepositoryException;

/**
 * PageRenderingException for 404 errors on page rest service.
 *
 * @author frank.sommer
 * @since 14.09.2023
 */
public class PageRenderingException extends RepositoryException {

    @Getter
    private final String _path;
    @Getter
    private final int _statusCode;

    public PageRenderingException(final int statusCode, @NonNull final String path) {
        _statusCode = statusCode;
        _path = path;
    }

}