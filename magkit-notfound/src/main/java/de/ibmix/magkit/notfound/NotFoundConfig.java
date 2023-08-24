package de.ibmix.magkit.notfound;

/*-
 * #%L
 * IBM iX Magnolia Kit
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

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of the 404 not found redirects.
 *
 * @author frank.sommer
 * @since 12.05.14
 */
public class NotFoundConfig {
    private String _default = "/en/toolbox/404";
    private List<ErrorMapping> _errorMappings = new ArrayList<ErrorMapping>();


    public String getDefault() {
        return _default;
    }

    public void setDefault(final String defaultPath) {
        _default = defaultPath;
    }

    public List<ErrorMapping> getErrorMappings() {
        return _errorMappings;
    }

    public void setErrorMappings(final List<ErrorMapping> errorMappings) {
        _errorMappings = errorMappings;
    }
}
