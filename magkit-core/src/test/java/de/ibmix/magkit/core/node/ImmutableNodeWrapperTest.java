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
import org.junit.Before;
import org.junit.Test;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests {@link ImmutableNodeWrapper} ensuring all mutating JCR Node operations throw UnsupportedOperationException
 * while read access is still delegated to the wrapped node.
 *
 * @author wolf.bubenik
 * @since 20.10.2025
 */
public class ImmutableNodeWrapperTest {

    private void assertUnsupported(ThrowingRunnable r) throws Exception {
        boolean unsupported = false;
        try {
            r.run();
        } catch (UnsupportedOperationException e) {
            unsupported = true;
        }
        assertThat("Operation should be unsupported", unsupported, is(true));
    }

    @Before
    public void setup() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void readDelegationWorks() throws Exception {
        Node base = mockNode("base", stubProperty("title", "base-title"));
        ImmutableNodeWrapper wrapper = new ImmutableNodeWrapper(base);
        assertThat(getStringValue(wrapper.getProperty("title")), is("base-title"));
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

        assertUnsupported(() -> imm.addNode("child"));
        assertUnsupported(() -> imm.addNode("child", "nt:unstructured"));
        assertUnsupported(() -> imm.orderBefore("a", "b"));

        assertUnsupported(() -> imm.setProperty("a", value));
        assertUnsupported(() -> imm.setProperty("a", value, 1));
        assertUnsupported(() -> imm.setProperty("a", values));
        assertUnsupported(() -> imm.setProperty("a", values, 1));
        assertUnsupported(() -> imm.setProperty("a", new String[]{"x", "y"}));
        assertUnsupported(() -> imm.setProperty("a", new String[]{"x", "y"}, 1));
        assertUnsupported(() -> imm.setProperty("a", "x"));
        assertUnsupported(() -> imm.setProperty("a", "x", 1));
        assertUnsupported(() -> imm.setProperty("a", new ByteArrayInputStream(new byte[0])));
        assertUnsupported(() -> imm.setProperty("a", binary));
        assertUnsupported(() -> imm.setProperty("a", true));
        assertUnsupported(() -> imm.setProperty("a", 1.0d));
        assertUnsupported(() -> imm.setProperty("a", decimal));
        assertUnsupported(() -> imm.setProperty("a", 2L));
        assertUnsupported(() -> imm.setProperty("a", cal));
        assertUnsupported(() -> imm.setProperty("a", refNode));

        assertUnsupported(() -> imm.setPrimaryType("mgnl:page"));
        assertUnsupported(() -> imm.addMixin("mix:versionable"));
        assertUnsupported(() -> imm.removeMixin("mix:versionable"));

        assertUnsupported(imm::checkin);
        assertUnsupported(imm::checkout);
        assertUnsupported(() -> imm.doneMerge(version));
        assertUnsupported(() -> imm.cancelMerge(version));
        assertUnsupported(() -> imm.update("ws"));
        assertUnsupported(() -> imm.merge("ws", true));

        assertUnsupported(() -> imm.restore("1.0", true));
        assertUnsupported(() -> imm.restore(version, true));
        assertUnsupported(() -> imm.restore(version, "rel", true));
        assertUnsupported(() -> imm.restoreByLabel("label", true));

        assertUnsupported(imm::save);
        assertUnsupported(() -> imm.refresh(false));
        assertUnsupported(imm::remove);
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Exception; }
}

