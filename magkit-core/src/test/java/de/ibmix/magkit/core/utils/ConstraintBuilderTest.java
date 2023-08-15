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

import de.ibmix.magkit.query.xpath.ConstraintBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static de.ibmix.magkit.query.xpath.ConstraintBuilder.Operator.AND;
import static de.ibmix.magkit.query.xpath.ConstraintBuilder.Operator.OR;
import static org.junit.Assert.assertEquals;

/**
 * @author philipp.guettler
 * @since 27.09.13
 */
public class ConstraintBuilderTest {

    private static final String STRING_AND = Operator.AND.toXpathString();
    private static final String TEST_CONSTRAINT = "testConstraint";
    private static final String TEST_TEMPLATE_NAME = "test-case:pages/testTemplate";
    private static final String TEST_TEMPLATE_PLACEHOLDER = "test-case:pages/testTemplate%";
    private static final String TEST_UUID = "1234567890";

    @Test
    public void testOpenGroup() throws Exception {
        Assert.assertEquals("(", new ConstraintBuilder().openGroup().build());
        Assert.assertEquals(STRING_AND + " (", new ConstraintBuilder().openGroup(Operator.AND).build());
    }

    @Test
    public void testCloseGroup() throws Exception {
        Assert.assertEquals(")", new ConstraintBuilder().closeGroup().build());
        Assert.assertEquals("()", new ConstraintBuilder().openGroup().closeGroup().build());
        Assert.assertEquals(STRING_AND + " ()", new ConstraintBuilder().openGroup(Operator.AND).closeGroup().build());
    }

    @Test
    public void testAppend() throws Exception {
        Assert.assertEquals(TEST_CONSTRAINT, new ConstraintBuilder().add(TEST_CONSTRAINT).build());
        Assert.assertEquals(STRING_AND + " " + TEST_CONSTRAINT, new ConstraintBuilder().add(Operator.AND, TEST_CONSTRAINT).build());
        Assert.assertEquals(TEST_CONSTRAINT + " " + STRING_AND + " " + TEST_CONSTRAINT, new ConstraintBuilder().add(TEST_CONSTRAINT).add(Operator.AND, TEST_CONSTRAINT).build());
        Assert.assertEquals(TEST_CONSTRAINT + " " + STRING_AND + " (" + TEST_CONSTRAINT + " " + STRING_AND + " " + TEST_CONSTRAINT + ")", new ConstraintBuilder().add(TEST_CONSTRAINT).openGroup(Operator.AND).add(Operator.AND, TEST_CONSTRAINT).add(Operator.AND, TEST_CONSTRAINT).closeGroup().build());
    }

    @Test
    public void testAppendUuidConstraint() throws Exception {
        Assert.assertEquals(String.format("@jcr:uuid='%s'", TEST_UUID), new ConstraintBuilder().addUuidConstraint(TEST_UUID).build());
        Assert.assertEquals(STRING_AND + " " + String.format("@jcr:uuid='%s'", TEST_UUID), new ConstraintBuilder().addUuidConstraint(Operator.AND, TEST_UUID).build());
    }

    @Test
    public void testTemplateName() throws Exception {
        Assert.assertEquals(String.format("@mgnl:template='%s'", TEST_TEMPLATE_NAME), new ConstraintBuilder().addTplNameConstraint(TEST_TEMPLATE_NAME).build());
        Assert.assertEquals(STRING_AND + " " + String.format("@mgnl:template='%s'", TEST_TEMPLATE_NAME), new ConstraintBuilder().addTplNameConstraint(Operator.AND, TEST_TEMPLATE_NAME).build());
        Assert.assertEquals(STRING_AND + " " + String.format("jcr:like(@mgnl:template, '%s')", TEST_TEMPLATE_PLACEHOLDER), new ConstraintBuilder().addTplNameConstraint(Operator.AND, TEST_TEMPLATE_PLACEHOLDER).build());
    }

    @Test
    public void testLive() {
        final ConstraintBuilder constraint = new ConstraintBuilder().addTplNameConstraint(TEST_TEMPLATE_NAME);

        constraint.openGroup(Operator.AND);
        for (String id : Arrays.asList("1", "2", "3", "4", "5")) {
            constraint.addUuidConstraint(Operator.OR, id);
        }
        constraint.closeGroup();

        constraint.add(Operator.AND, "jcr:like(./*/*/@mgnl:template, '" + TEST_TEMPLATE_PLACEHOLDER + "')");
        // we have to include those nodes, where no property was set (which counts as isPrivateBrand='false')
        constraint.openGroup(Operator.AND);
        constraint.add(String.format("./*/*/@isPrivateBrand='%s'", "false"));
        constraint.add(Operator.OR, "not(./*/*/@isPrivateBrand='false' or ./*/*/@isPrivateBrand='true')");
        constraint.closeGroup();
        constraint.add(Operator.AND, String.format("./*/*/@categories='%s'", "categoryId"));

        Assert.assertEquals("@mgnl:template='test-case:pages/testTemplate' and (@jcr:uuid='1' or @jcr:uuid='2' or @jcr:uuid='3' or @jcr:uuid='4' or @jcr:uuid='5') and jcr:like(./*/*/@mgnl:template, 'test-case:pages/testTemplate%') and (./*/*/@isPrivateBrand='false' or not(./*/*/@isPrivateBrand='false' or ./*/*/@isPrivateBrand='true')) and ./*/*/@categories='categoryId'", constraint.build());
    }
}
