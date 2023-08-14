package com.aperto.magkit.utils;

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

import com.google.common.base.Preconditions;
import info.magnolia.jcr.wrapper.HTMLEscapingContentDecorator;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Decorator to escape HTML in String Values.
 * We are bypassing the magnolia html encoding of nodes if we work on values instead of the node properties.
 * This util class fixes the issue for our PropertySupport classes.
 * It provides HTML encoding for values that is missing in magnolia implementation.
 *
 * @author wolf.bubenik
 * @since 21.12.18.
 */
public class HtmlEscapingValueDecorator implements Value {

    private Value _wrapped;
    private HTMLEscapingContentDecorator _decorator;

    public HtmlEscapingValueDecorator(Value wrapped, HTMLEscapingContentDecorator decorator) {
        Preconditions.checkArgument(wrapped != null, "The Value to be wrapped must not be null.");
        Preconditions.checkArgument(decorator != null, "The HTMLEscapingContentDecorator to be used must not be null.");
        _wrapped = wrapped;
        _decorator = decorator;
    }

    @Override
    public String getString() throws RepositoryException {
        return _decorator.decorate(_wrapped.getString());
    }

    @Override
    public InputStream getStream() throws RepositoryException {
        return _wrapped.getStream();
    }

    @Override
    public Binary getBinary() throws RepositoryException {
        return _wrapped.getBinary();
    }

    @Override
    public long getLong() throws RepositoryException {
        return _wrapped.getLong();
    }

    @Override
    public double getDouble() throws RepositoryException {
        return _wrapped.getDouble();
    }

    @Override
    public BigDecimal getDecimal() throws RepositoryException {
        return _wrapped.getDecimal();
    }

    @Override
    public Calendar getDate() throws RepositoryException {
        return _wrapped.getDate();
    }

    @Override
    public boolean getBoolean() throws RepositoryException {
        return _wrapped.getBoolean();
    }

    @Override
    public int getType() {
        return _wrapped.getType();
    }
}
