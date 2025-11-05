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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.version.Version;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import static de.ibmix.magkit.core.utils.PropertyUtils.getStringValue;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link ImmutableNodeWrapper} ensuring all mutating JCR Node operations throw UnsupportedOperationException
 * while read access is still delegated to the wrapped node.
 *
 * @author wolf.bubenik
 * @since 20.10.2025
 */
public class ImmutableNodeWrapperTest {

    @BeforeEach
    public void setup() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void readDelegationWorks() throws Exception {
        Node base = mockNode("base", stubProperty("title", "base-title"));
        ImmutableNodeWrapper wrapper = new ImmutableNodeWrapper(base);
        assertEquals("base-title", getStringValue(wrapper.getProperty("title")));
        // underlying still unchanged (delegation test is implicit as we read the original value)
    }

    @Test
    public void allMutationsAreRejected() throws Exception {
        Node base = mockNode("immutBase", stubProperty("p", "v"));
        ImmutableNodeWrapper imm = new ImmutableNodeWrapper(base);

        Value value = Mockito.mock(Value.class);
        Value[] values = new Value[]{value};
        Binary binary = Mockito.mock(Binary.class);
        Calendar cal = Calendar.getInstance();
        BigDecimal decimal = new BigDecimal("1.23");
        Version version = Mockito.mock(Version.class);
        Node refNode = mockNode("refNode");

        assertThrows(UnsupportedOperationException.class, () -> imm.addNode("child"));
        assertThrows(UnsupportedOperationException.class, () -> imm.addNode("child", "nt:unstructured"));
        assertThrows(UnsupportedOperationException.class, () -> imm.orderBefore("a", "b"));

        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", value));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", value, 1));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", values));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", values, 1));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", new String[]{"x", "y"}));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", new String[]{"x", "y"}, 1));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", "x"));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", "x", 1));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", new ByteArrayInputStream(new byte[0])));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", binary));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", true));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", 1.0d));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", decimal));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", 2L));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", cal));
        assertThrows(UnsupportedOperationException.class, () -> imm.setProperty("a", refNode));

        assertThrows(UnsupportedOperationException.class, () -> imm.setPrimaryType("mgnl:page"));
        assertThrows(UnsupportedOperationException.class, () -> imm.addMixin("mix:versionable"));
        assertThrows(UnsupportedOperationException.class, () -> imm.removeMixin("mix:versionable"));

        assertThrows(UnsupportedOperationException.class, imm::checkin);
        assertThrows(UnsupportedOperationException.class, imm::checkout);
        assertThrows(UnsupportedOperationException.class, () -> imm.doneMerge(version));
        assertThrows(UnsupportedOperationException.class, () -> imm.cancelMerge(version));
        assertThrows(UnsupportedOperationException.class, () -> imm.update("ws"));
        assertThrows(UnsupportedOperationException.class, () -> imm.merge("ws", true));

        assertThrows(UnsupportedOperationException.class, () -> imm.restore("1.0", true));
        assertThrows(UnsupportedOperationException.class, () -> imm.restore(version, true));
        assertThrows(UnsupportedOperationException.class, () -> imm.restore(version, "rel", true));
        assertThrows(UnsupportedOperationException.class, () -> imm.restoreByLabel("label", true));

        assertThrows(UnsupportedOperationException.class, imm::save);
        assertThrows(UnsupportedOperationException.class, () -> imm.refresh(false));
        assertThrows(UnsupportedOperationException.class, imm::remove);
    }
}