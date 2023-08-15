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

/**
 * Error mapping for not found redirect handling.
 *
 * @author frank.sommer
 * @since 12.05.14
 */
public class ErrorMapping {
    protected static final String DEF_SITE = "default";

    private String _siteName = DEF_SITE;
    private String _locale;
    private String _errorPath;

    public String getSiteName() {
        return _siteName;
    }

    public void setSiteName(final String siteName) {
        _siteName = siteName;
    }

    public String getLocale() {
        return _locale;
    }

    public void setLocale(final String locale) {
        _locale = locale;
    }

    public String getErrorPath() {
        return _errorPath;
    }

    public void setErrorPath(final String errorPath) {
        _errorPath = errorPath;
    }
}
