package de.ibmix.magkit.core.filter;

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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TemplateNameVoter} validating template collection handling, root path filtering
 * and template matching scenarios.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-01-05
 */
public class TemplateNameVoterTest {

    private TemplateNameVoter _voter;

    @BeforeEach
    public void setUp() throws Exception {
        cleanContext();
        mockWebContext(stubJcrSession(WEBSITE));
        _voter = new TemplateNameVoter();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void addTemplate() {
        assertNotNull(_voter.getTemplates());
        assertEquals(0, _voter.getTemplates().length);

        _voter.addTemplate("template-1");
        assertEquals(1, _voter.getTemplates().length);
        assertEquals("template-1", _voter.getTemplates()[0]);

        _voter.addTemplate("template-2");
        assertEquals(2, _voter.getTemplates().length);
        assertEquals("template-1", _voter.getTemplates()[0]);
        assertEquals("template-2", _voter.getTemplates()[1]);

        _voter.addTemplate(null);
        _voter.addTemplate("");
        _voter.addTemplate("  \n\t  ");
        _voter.addTemplate("  template-3   ");
        assertEquals(3, _voter.getTemplates().length);
        assertEquals("template-3", _voter.getTemplates()[2]);
    }

    @Test
    public void boolVoteNoRootPath() throws RepositoryException {
        assertFalse(_voter.boolVote(null));
        assertFalse(_voter.boolVote(""));
        assertFalse(_voter.boolVote("   "));
        // test not existing page node
        assertFalse(_voter.boolVote("/root/page.html"));
        // test page without template
        Node page = mockPageNode("/root/page");
        assertFalse(_voter.boolVote("/root/page.html"));
        // test page with not matching template
        stubTemplate("test-template").of(page);
        assertFalse(_voter.boolVote("/root/page.html"));
        // test page with matching template
        _voter.addTemplate("template-1");
        _voter.addTemplate("test-template");
        assertTrue(_voter.boolVote("/root/page.html"));
    }

    @Test
    public void boolVoteWithRootPath() throws RepositoryException {
        // test wrong root path:
        _voter.setRootPath("/wrong");
        assertFalse(_voter.boolVote(null));
        assertFalse(_voter.boolVote(""));
        assertFalse(_voter.boolVote("   "));
        // test not existing page node
        assertFalse(_voter.boolVote("/root/page.html"));
        // test page without template
        Node page = mockPageNode("/root/page");
        assertFalse(_voter.boolVote("/root/page.html"));
        // test page with not matching template
        stubTemplate("test-template").of(page);
        assertFalse(_voter.boolVote("/root/page.html"));
        // test page with matching template
        _voter.addTemplate("template-1");
        _voter.addTemplate("test-template");
        assertFalse(_voter.boolVote("/root/page.html"));

        // test correct root path:
        _voter.setRootPath("/root");
        assertTrue(_voter.boolVote("/root/page.html"));
    }
}