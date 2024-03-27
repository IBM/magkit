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

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

public class ImmutableNodeWrapper extends NullableDelegateNodeWrapper {

    private static final String UNSUPPORTED_METHOD_MESSAGE = "Changing of states are not supported by this implementation.";

    public ImmutableNodeWrapper(final Node wrapped) {
        super(wrapped);
    }

    @Override
    public Node addNode(String relPath) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Value value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Value value, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Value[] values) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, String[] values) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, String[] values, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, String value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, String value, int type) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, InputStream value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Binary value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, boolean value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, double value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, long value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Calendar value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Property setProperty(String name, Node value) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void setPrimaryType(String nodeTypeName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void addMixin(String mixinName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void removeMixin(String mixinName) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public Version checkin() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void checkout() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void doneMerge(Version version) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void cancelMerge(Version version) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void update(String srcWorkspace) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void save() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }

    @Override
    public void remove() throws RepositoryException {
        throw new UnsupportedOperationException(UNSUPPORTED_METHOD_MESSAGE);
    }
}
