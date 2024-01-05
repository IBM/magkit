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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubTemplate;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test TemplateNameVoter.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-01-05
 */
public class TemplateNameVoterTest {

    private TemplateNameVoter _voter;

    @Before
    public void setUp() throws Exception {
        cleanContext();
        mockWebContext(stubJcrSession(WEBSITE));
        _voter = new TemplateNameVoter();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void addTemplate() {
        assertThat(_voter.getTemplates(), notNullValue());
        assertThat(_voter.getTemplates().length, is(0));

        _voter.addTemplate("template-1");
        assertThat(_voter.getTemplates().length, is(1));
        assertThat(_voter.getTemplates()[0], is("template-1"));

        _voter.addTemplate("template-2");
        assertThat(_voter.getTemplates().length, is(2));
        assertThat(_voter.getTemplates()[0], is("template-1"));
        assertThat(_voter.getTemplates()[1], is("template-2"));

        _voter.addTemplate(null);
        _voter.addTemplate("");
        _voter.addTemplate("  \n\t  ");
        _voter.addTemplate("  template-3   ");
        assertThat(_voter.getTemplates().length, is(3));
        assertThat(_voter.getTemplates()[2], is("template-3"));
    }

    @Test
    public void boolVoteNoRootPath() throws RepositoryException {
        assertThat(_voter.boolVote(null), is(false));
        assertThat(_voter.boolVote(""), is(false));
        assertThat(_voter.boolVote("   "), is(false));
        // test not existing page node
        assertThat(_voter.boolVote("/root/page.html"), is(false));
        // test page without template
        Node page = mockPageNode("/root/page");
        assertThat(_voter.boolVote("/root/page.html"), is(false));
        // test page with not matching template
        stubTemplate("test-template").of(page);
        assertThat(_voter.boolVote("/root/page.html"), is(false));
        // test page with matching template
        _voter.addTemplate("template-1");
        _voter.addTemplate("test-template");
        assertThat(_voter.boolVote("/root/page.html"), is(true));
    }

    @Test
    public void boolVoteWithRootPath() throws RepositoryException {
        // test wrong root path:
        _voter.setRootPath("/wrong");
        assertThat(_voter.boolVote(null), is(false));
        assertThat(_voter.boolVote(""), is(false));
        assertThat(_voter.boolVote("   "), is(false));
        // test not existing page node
        assertThat(_voter.boolVote("/root/page.html"), is(false));
        // test page without template
        Node page = mockPageNode("/root/page");
        assertThat(_voter.boolVote("/root/page.html"), is(false));
        // test page with not matching template
        stubTemplate("test-template").of(page);
        assertThat(_voter.boolVote("/root/page.html"), is(false));
        // test page with matching template
        _voter.addTemplate("template-1");
        _voter.addTemplate("test-template");
        assertThat(_voter.boolVote("/root/page.html"), is(false));

        // test correct root path:
        _voter.setRootPath("/root");
        assertThat(_voter.boolVote("/root/page.html"), is(true));
    }
}