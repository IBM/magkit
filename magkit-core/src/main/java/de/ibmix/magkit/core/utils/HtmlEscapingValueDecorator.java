package de.ibmix.magkit.core.utils;

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
 * HTML escaping {@link Value} decorator.
 * <p>
 * Purpose: Wraps a JCR {@link Value} and applies Magnolia's {@link HTMLEscapingContentDecorator} only when its
 * string representation is requested via {@link #getString()}. This closes a gap in Magnolia where HTML encoding
 * of property values is performed at the node/property level but not when raw {@link Value} objects are accessed
 * directly by utility support classes.
 * </p>
 * <p>
 * Main functionalities / key features:
 * <ul>
 *   <li>Provides HTML escaping for {@code String} values via {@link #getString()}.</li>
 *   <li>Pass-through decorator for all non-string accessors (stream, binary, numeric, date, boolean, type).</li>
 *   <li>Immutable and therefore thread-safe: all state is set during construction and never mutated.</li>
 * </ul>
 * </p>
 * <p>
 * Important details:
 * <ul>
 *   <li>Only {@link #getString()} applies escaping; other getters return the unmodified delegated value.</li>
 *   <li>Null arguments to the constructor are rejected with an {@link IllegalArgumentException} (see preconditions).</li>
 *   <li>No caching is performed; escaping is executed on each {@link #getString()} call.</li>
 * </ul>
 * </p>
 * <p><b>Usage preconditions:</b> Provide a non-null {@link Value} and a non-null {@link HTMLEscapingContentDecorator} instance.
 * The wrapped {@link Value} should represent content that may contain HTML characters needing escaping.</p>
 * <p><b>Side effects:</b> None. The decorator does not alter the underlying repository state; it only transforms
 * the returned string value on demand.</p>
 * <p><b>Null and error handling:</b> Constructor validates inputs and throws {@link IllegalArgumentException} if invalid.
 * JCR accessor methods may throw {@link RepositoryException} as per the JCR API contract. No additional exceptions are introduced.</p>
 * <p><b>Thread-safety:</b> This class is thread-safe; it is immutable and delegates to thread-safe Magnolia/JCR components.</p>
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * Value original = node.getProperty("htmlContent").getValue();
 * HTMLEscapingContentDecorator escapingDecorator = new HTMLEscapingContentDecorator();
 * Value safe = new HtmlEscapingValueDecorator(original, escapingDecorator);
 * // Returns escaped HTML string
 * String escaped = safe.getString();
 * }</pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2021-12-18
 */
public class HtmlEscapingValueDecorator implements Value {

    private final Value _wrapped;
    private final HTMLEscapingContentDecorator _decorator;

    /**
     * Constructs an immutable HTML escaping value decorator.
     * <p>
     * Validates that both the {@code wrapped} value and the {@code decorator} are non-null using Guava
     * {@link Preconditions}. Escaping is applied lazily only when {@link #getString()} is invoked.
     * </p>
     *
     * @param wrapped   the underlying JCR {@link Value} to be decorated (must not be null)
     * @param decorator the Magnolia {@link HTMLEscapingContentDecorator} used to escape string content (must not be null)
     * @throws IllegalArgumentException if any argument is null
     */
    public HtmlEscapingValueDecorator(Value wrapped, HTMLEscapingContentDecorator decorator) {
        Preconditions.checkArgument(wrapped != null, "The Value to be wrapped must not be null.");
        Preconditions.checkArgument(decorator != null, "The HTMLEscapingContentDecorator to be used must not be null.");
        _wrapped = wrapped;
        _decorator = decorator;
    }

    /**
     * Returns the string representation of the wrapped value with HTML escaping applied.
     * <p>Delegates to the underlying value, then decorates the result using {@link HTMLEscapingContentDecorator}.</p>
     *
     * @return escaped string form of the underlying value (never null unless the delegate returns null)
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public String getString() throws RepositoryException {
        return _decorator.decorate(_wrapped.getString());
    }

    /**
     * Returns the underlying binary stream unchanged.
     *
     * @return input stream from the wrapped value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public InputStream getStream() throws RepositoryException {
        return _wrapped.getStream();
    }

    /**
     * Returns the underlying binary value unchanged.
     *
     * @return binary from the wrapped value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public Binary getBinary() throws RepositoryException {
        return _wrapped.getBinary();
    }

    /**
     * Returns the underlying long value unchanged.
     *
     * @return long value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public long getLong() throws RepositoryException {
        return _wrapped.getLong();
    }

    /**
     * Returns the underlying double value unchanged.
     *
     * @return double value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public double getDouble() throws RepositoryException {
        return _wrapped.getDouble();
    }

    /**
     * Returns the underlying decimal value unchanged.
     *
     * @return decimal value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public BigDecimal getDecimal() throws RepositoryException {
        return _wrapped.getDecimal();
    }

    /**
     * Returns the underlying date value unchanged.
     *
     * @return calendar date value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public Calendar getDate() throws RepositoryException {
        return _wrapped.getDate();
    }

    /**
     * Returns the underlying boolean value unchanged.
     *
     * @return boolean value
     * @throws RepositoryException if the underlying JCR value retrieval fails
     */
    @Override
    public boolean getBoolean() throws RepositoryException {
        return _wrapped.getBoolean();
    }

    /**
     * Returns the underlying JCR value type.
     *
     * @return JCR type constant
     */
    @Override
    public int getType() {
        return _wrapped.getType();
    }
}
