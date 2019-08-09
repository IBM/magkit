package com.aperto.magkit.utils.node;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * This property implementation allows to make use of stubbed values in order to change the view of a node.
 *
 * @author noreply@aperto.com
 */
public class StubbingProperty implements Property {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubbingProperty.class);

    private static final String READ_ONLY_MESSAGE = "A " + StubbingProperty.class.getSimpleName() + " is read only.";

    private Value _value;
    private Value[] _values;
    private String _name;
    private Node _parent;

    public StubbingProperty(Node parent, String name, Node... values) {
        this(parent, name);
        try {
            if (values != null && values.length > 0) {
                _value = new ReferenceValue(values[0]);
                _values = new Value[values.length];
                for (int i = 0; i < values.length; i++) {
                    _values[i] = new ReferenceValue(values[i]);
                }
            }
        } catch (RepositoryException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public StubbingProperty(Node parent, String name, Boolean... values) {
        this(parent, name);
        if (values != null && values.length > 0) {
            _value = new BooleanValue(values[0]);
            _values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                _values[i] = new BooleanValue(values[i]);
            }
        }
    }

    public StubbingProperty(Node parent, String name, Long... values) {
        this(parent, name);
        if (values != null && values.length > 0) {
            _value = new LongValue(values[0]);
            _values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                _values[i] = new LongValue(values[i]);
            }
        }
    }

    public StubbingProperty(Node parent, String name, Double... values) {
        this(parent, name);
        if (values != null && values.length > 0) {
            _value = new DoubleValue(values[0]);
            _values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                _values[i] = new DoubleValue(values[i]);
            }
        }
    }

    public StubbingProperty(Node parent, String name, Calendar... values) {
        this(parent, name);
        if (values != null && values.length > 0) {
            _value = new DateValue(values[0]);
            _values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                _values[i] = new DateValue(values[i]);
            }
        }
    }

    public StubbingProperty(Node parent, String name, String... values) {
        this(parent, name);
        if (values != null && values.length > 0) {
            _value = new StringValue(values[0]);
            _values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                _values[i] = new StringValue(values[i]);
            }
        }
    }

    private StubbingProperty(Node parent, String name) {
        _parent = parent;
        _name = name;
    }

    @Override
    public void setValue(Value value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(Value[] values) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(String value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(String[] values) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(InputStream value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(Binary value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(long value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(double value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(BigDecimal value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(Calendar value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void setValue(boolean value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public Value getValue() throws RepositoryException {
        return _value;
    }

    @Override
    public void setValue(Node value) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public Value[] getValues() throws RepositoryException {
        return _values != null ? _values : new Value[]{_value};
    }

    @Override
    public String getString() throws RepositoryException {
        return _value.getString();
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputStream getStream() throws RepositoryException {
        return _value.getStream();
    }

    @Override
    public Binary getBinary() throws RepositoryException {
        return _value.getBinary();
    }

    @Override
    public long getLong() throws RepositoryException {
        return _value.getLong();
    }

    @Override
    public double getDouble() throws RepositoryException {
        return _value.getDouble();
    }

    @Override
    public BigDecimal getDecimal() throws RepositoryException {
        return _value.getDecimal();
    }

    @Override
    public Calendar getDate() throws RepositoryException {
        return _value.getDate();
    }

    @Override
    public boolean getBoolean() throws RepositoryException {
        return _value.getBoolean();
    }

    @Override
    public Node getNode() throws RepositoryException {
        return null;
    }

    @Override
    public Property getProperty() throws RepositoryException {
        return null;
    }

    @Override
    public long getLength() throws RepositoryException {
        return 0L;
    }

    @Override
    public long[] getLengths() throws RepositoryException {
        return new long[0];
    }

    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        return null;
    }

    @Override
    public int getType() throws RepositoryException {
        return _value.getType();
    }

    @Override
    public boolean isMultiple() throws RepositoryException {
        return _values != null && _values.length > 1;
    }

    @Override
    public String getPath() throws RepositoryException {
        return _parent.getPath() + '/' + _name;
    }

    @Override
    public String getName() throws RepositoryException {
        return _name;
    }

    @Override
    public Item getAncestor(int depth) throws RepositoryException {
        return null;
    }

    @Override
    public Node getParent() throws RepositoryException {
        return _parent;
    }

    @Override
    public int getDepth() throws RepositoryException {
        return _parent.getDepth() + 1;
    }

    @Override
    public Session getSession() throws RepositoryException {
        return _parent.getSession();
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        return false;
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void save() throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public void remove() throws RepositoryException {
        throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
