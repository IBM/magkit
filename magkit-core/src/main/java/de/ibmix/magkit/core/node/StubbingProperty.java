package de.ibmix.magkit.core.node;

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

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.jackrabbit.value.BaseValue;
import org.apache.jackrabbit.value.BinaryValue;
import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.DecimalValue;
import org.apache.jackrabbit.value.DoubleValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.ReferenceValue;
import org.apache.jackrabbit.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.function.Function;

/**
 * Read-only {@link Property} implementation exposing synthetic (stubbed) values without persisting them to JCR.
 * <p>Purpose: Provide an in-memory overlay of property data for wrapper nodes (e.g. {@link AlteringNodeWrapper}) to
 * simulate content changes, multi-value expansions or type conversions while leaving the underlying repository
 * untouched.</p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Supports stubbing of multiple JCR value types (String, Boolean, Long, Double, Date, Binary, Reference, Decimal).</li>
 *   <li>Automatically exposes multi-valued property semantics when more than one value is provided.</li>
 *   <li>Maintains parent hierarchy metadata (path, depth, ancestor traversal).</li>
 *   <li>Strictly read-only: all mutating operations throw {@link UnsupportedOperationException}.</li>
 *   <li>Convenience constructors for each supported type.</li>
 * </ul>
 * <p>Usage example:</p>
 * <pre>{@code
 * Node parent = ...;
 * Property p = new StubbingProperty(parent, "tags", "one", "two", "three");
 * boolean multi = p.isMultiple(); // true
 * String first = p.getString(); // "one"
 * }</pre>
 * <p>Null and error handling: Constructors ignore null value arrays (no values set). Repository exceptions during
 * reference value creation are logged (WARN) and do not propagate. Getter methods may throw standard
 * {@link RepositoryException} if underlying value conversion fails.</p>
 * <p>Thread-safety: Not thread-safe; instances are effectively immutable after construction unless external code
 * replaces internal arrays (which it cannot). Safe for concurrent read access.</p>
 * <p>Side effects: None – property does not save, remove or modify repository state.</p>
 *
 * @author noreply@aperto.com
 * @since 2024-01-04
 */
public class StubbingProperty implements Property {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubbingProperty.class);

    private static final String READ_ONLY_MESSAGE = "A " + StubbingProperty.class.getSimpleName() + " is read only.";

    private Value _value;
    private Value[] _values;
    private Node _nodeValue;
    private Node[] _nodeValues;
    private final String _name;
    private final Node _parent;

    /**
     * Create a reference property stubbing node references (single or multi valued).
     *
     * @param parent parent node (must not be null)
     * @param name property name
     * @param values referenced nodes (may be empty)
     */
    public StubbingProperty(Node parent, String name, Node... values) {
        this(parent, name);
        try {
            if (values != null && values.length > 0) {
                _value = new ReferenceValue(values[0]);
                _nodeValue = values[0];
                _nodeValues = values;
                _values = new Value[values.length];
                for (int i = 0; i < values.length; i++) {
                    _values[i] = new ReferenceValue(values[i]);
                }
            }
        } catch (RepositoryException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Create a boolean property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values boolean values
     */
    public StubbingProperty(Node parent, String name, Boolean... values) {
        this(parent, name);
        init(values, BooleanValue::new);
    }

    /**
     * Create a long property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values long values
     */
    public StubbingProperty(Node parent, String name, Long... values) {
        this(parent, name);
        init(values, LongValue::new);
    }

    /**
     * Create a double property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values double values
     */
    public StubbingProperty(Node parent, String name, Double... values) {
        this(parent, name);
        init(values, DoubleValue::new);
    }

    /**
     * Create a date property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values calendar values
     */
    public StubbingProperty(Node parent, String name, Calendar... values) {
        this(parent, name);
        init(values, DateValue::new);
    }

    /**
     * Create a string property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values string values
     */
    public StubbingProperty(Node parent, String name, String... values) {
        this(parent, name);
        init(values, StringValue::new);
    }

    /**
     * Create a binary property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values binary values
     */
    public StubbingProperty(Node parent, String name, Binary... values) {
        this(parent, name);
        init(values, BinaryValue::new);
    }

    /**
     * Create a decimal property (single or multi valued).
     *
     * @param parent parent node
     * @param name property name
     * @param values decimal values
     */
    public StubbingProperty(Node parent, String name, BigDecimal... values) {
        this(parent, name);
        init(values, DecimalValue::new);
    }

    private <T, R extends BaseValue> void init(T[] values, Function<T, R> toValue) {
        if (values != null && values.length > 0) {
            _value = toValue.apply(values[0]);
            _values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                _values[i] = toValue.apply(values[i]);
            }
        }
    }

    /**
     * Internal base constructor storing parent and property name. All public constructors delegate here.
     * Avoids duplication of assignment logic.
     *
     * @param parent parent node (may be null for some synthetic scenarios but usually non-null)
     * @param name property name
     */
    private StubbingProperty(Node parent, String name) {
        _parent = parent;
        _name = name;
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value new value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(Value value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param values new values
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(Value[] values) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value new string value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(String value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param values new string values
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(String[] values) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value input stream value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(InputStream value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value binary value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(Binary value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value long value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(long value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value double value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(double value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value decimal value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(BigDecimal value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value calendar value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(Calendar value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported write: property is read-only.
     *
     * @param value boolean value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(boolean value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Get the first stubbed value.
     *
     * @return first value or null if no values present
     * @throws RepositoryException on value access errors
     */
    @Override
    public Value getValue() throws RepositoryException {
        return _value;
    }

    /**
     * Unsupported write: property is read-only (node reference).
     *
     * @param value node value
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void setValue(Node value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Return all stubbed values (multi-valued) or array containing single value.
     *
     * @return array of values (never null if at least one value was provided)
     * @throws RepositoryException on access issues
     */
    @Override
    public Value[] getValues() throws RepositoryException {
        return _values != null ? _values : new Value[]{_value};
    }

    /**
     * Convenience string getter of first value.
     *
     * @return string representation
     * @throws RepositoryException on conversion errors
     */
    @Override
    public String getString() throws RepositoryException {
        return _value.getString();
    }

    /**
     * Get content stream of first value (deprecated API delegated).
     *
     * @return input stream or null
     * @throws RepositoryException on conversion errors
     */
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getStream() throws RepositoryException {
        return _value.getStream();
    }

    /**
     * Get binary value.
     *
     * @return binary or null if not binary
     * @throws RepositoryException on conversion errors
     */
    @Override
    public Binary getBinary() throws RepositoryException {
        return _value.getBinary();
    }

    /**
     * Get long value.
     *
     * @return long
     * @throws RepositoryException on conversion errors
     */
    @Override
    public long getLong() throws RepositoryException {
        return _value.getLong();
    }

    /**
     * Get double value.
     *
     * @return double
     * @throws RepositoryException on conversion errors
     */
    @Override
    public double getDouble() throws RepositoryException {
        return _value.getDouble();
    }

    /**
     * Get decimal value.
     *
     * @return decimal
     * @throws RepositoryException on conversion errors
     */
    @Override
    public BigDecimal getDecimal() throws RepositoryException {
        return _value.getDecimal();
    }

    /**
     * Get date value.
     *
     * @return calendar
     * @throws RepositoryException on conversion errors
     */
    @Override
    public Calendar getDate() throws RepositoryException {
        return _value.getDate();
    }

    /**
     * Get boolean value.
     *
     * @return boolean
     * @throws RepositoryException on conversion errors
     */
    @Override
    public boolean getBoolean() throws RepositoryException {
        return _value.getBoolean();
    }

    /**
     * Get referenced node if this is a reference property.
     *
     * @return referenced node or null
     * @throws RepositoryException on access errors
     */
    @Override
    public Node getNode() throws RepositoryException {
        return _nodeValue;
    }

    /**
     * Return all referenced nodes (only for reference properties).
     *
     * @return node array or null if not multi reference
     */
    public Node[] getNodes() {
        return _nodeValues;
    }

    /**
     * Unsupported operation: property referencing another property is not implemented.
     *
     * @return never returns normally
     * @throws NotImplementedException always
     */
    @Override
    public Property getProperty() throws RepositoryException {
        throw new NotImplementedException();
    }

    /**
     * Length of single value or binary size.
     *
     * @return length
     * @throws RepositoryException on access errors
     */
    @Override
    public long getLength() throws RepositoryException {
        return getType() == PropertyType.BINARY ? getBinary().getSize() : getString().length();
    }

    /**
     * Unsupported multi-length operation.
     *
     * @return never returns normally
     * @throws NotImplementedException always
     */
    @Override
    public long[] getLengths() throws RepositoryException {
        throw new NotImplementedException();
    }

    /**
     * Property definition is not backed; returns null.
     *
     * @return null always
     */
    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        return null;
    }

    /**
     * Return JCR type of first value.
     *
     * @return type id
     * @throws RepositoryException on access errors
     */
    @Override
    public int getType() throws RepositoryException {
        return _value.getType();
    }

    /**
     * Indicates whether multiple values exist.
     *
     * @return true if multi valued
     * @throws RepositoryException on access errors
     */
    @Override
    public boolean isMultiple() throws RepositoryException {
        return _values != null && _values.length > 1;
    }

    /**
     * Synthetic absolute property path below parent.
     *
     * @return path
     * @throws RepositoryException on access errors
     */
    @Override
    public String getPath() throws RepositoryException {
        return _parent.getPath() + '/' + _name;
    }

    /**
     * Property name.
     *
     * @return name
     * @throws RepositoryException on access errors
     */
    @Override
    public String getName() throws RepositoryException {
        return _name;
    }

    /**
     * Resolve ancestor item at given depth relative to root.
     *
     * @param depth requested depth
     * @return ancestor item or null if invalid depth
     * @throws RepositoryException on access errors
     */
    @Override
    public Item getAncestor(int depth) throws RepositoryException {
        Item result = null;
        if (depth > getDepth()) {
            result = null;
        } else if (depth == getDepth()) {
            result = this;
        } else if (depth >= 0) {
            result = _parent.getAncestor(depth);
        }
        return result;
    }

    /**
     * Parent node.
     *
     * @return parent node
     * @throws RepositoryException on access errors
     */
    @Override
    public Node getParent() throws RepositoryException {
        return _parent;
    }

    /**
     * Depth relative to root (parent depth + 1).
     *
     * @return depth
     * @throws RepositoryException on access errors
     */
    @Override
    public int getDepth() throws RepositoryException {
        return _parent.getDepth() + 1;
    }

    /**
     * Session from parent node.
     *
     * @return JCR session
     * @throws RepositoryException on access errors
     */
    @Override
    public Session getSession() throws RepositoryException {
        return _parent.getSession();
    }

    /**
     * Always false – this is a property implementation.
     *
     * @return false
     */
    @Override
    public boolean isNode() {
        return false;
    }

    /**
     * Always false – synthetic property not transiently new.
     *
     * @return false
     */
    @Override
    public boolean isNew() {
        return false;
    }

    /**
     * Always false – synthetic property does not track modifications.
     *
     * @return false
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Equality based on value equality for other stub properties.
     *
     * @param otherItem other item
     * @return true if same underlying value
     * @throws RepositoryException on access errors
     */
    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        return otherItem instanceof StubbingProperty && getValue().equals(((StubbingProperty) otherItem).getValue());
    }

    /**
     * Unsupported visitor: read-only.
     *
     * @param visitor item visitor
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported save: read-only.
     *
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void save() throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported refresh: read-only.
     *
     * @param keepChanges ignored
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Unsupported remove: read-only.
     *
     * @throws RepositoryException always (unsupported)
     */
    @Override
    public void remove() throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    /**
     * Reflection based string representation for debugging.
     *
     * @return string view
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
