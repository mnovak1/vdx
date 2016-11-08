import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@RunWith(Arquillian.class)
public class TestBase {

    protected static final String STANDALONE_ARQUILLIAN_CONTAINER = "jboss";
    protected static final String DOMAIN_ARQUILLIAN_CONTAINER = "jboss-domain";

    @ArquillianResource
    private ContainerController controller;

    @Rule
    public TestName testName = new TestName();

    private Path testDirectory;

    public Server container() {
        return Server.create(controller);
    }

    @Before
    public void setUp() {
        System.out.println("----------------------------------------- " + this.getClass().getSimpleName()
                + " - " + testName.getMethodName() + " -----------------------------------------");

        testDirectory = Paths.get("server-logs", this.getClass().getSimpleName(), testName.getMethodName());

    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "duplicate-attribute.xml") //, xmlTransformationClass= TypoInExtensions.class)
    public void test() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        System.out.println(container().getErrorMessageFromServerStart());
        String pattern = ".*OPVDX001: Validation error in duplicate-attribute.xml ==========================\n.*" +
                "\n" +
                "  .*: <job-repository name=\"in-memory\">\n" +
                "  .*:   <jdbc data-source=\"foo\"\n" +
                "  .*:         data-source=\"bar\"/>\n" +
                "               ^^^^ 'data-source' can't appear more than once on this element\n" +
                "\n" +
                "  .*: </job-repository>\n" +
                "  .*: <thread-pool name=\"batch\">\n" +
                "  .*:     <max-threads count=\"10\"/>\n" +
                "\n" +
                " A 'data-source' attribute first appears here:\n" +
                "\n" +
                "  .*: <default-thread-pool name=\"batch\"/>\n" +
                "  .*: <job-repository name=\"in-memory\">\n" +
                "  .*:   <jdbc data-source=\"foo\"\n" +
                "               ^^^^\n" +
                "\n" +
                "  .*:         data-source=\"bar\"/>\n" +
                "  .*: </job-repository>\n" +
                "  .*: <thread-pool name=\"batch\">\n" +
                "\n" +
                " The underlying error message was:\n" +
                " > Duplicate attribute 'data-source'.\n" +
                " >  at [row,col {unknown-source}]:.*\n.*";

        assertExpectedError(pattern, container().getErrorMessageFromServerStart());
    }

    private void assertExpectedError(String regex, String errorMessage) {
        Pattern expectedError = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = expectedError.matcher(errorMessage);
        Assert.assertTrue("Error log message does not match the pattern. Failing the test. \n" +
                "########################## Pattern ##############################\n" +
                regex + " \n" +
                "########################## Error log ##############################\n" +
                errorMessage + " \n" +
                "########################################################\n" +
                "########################################################\n", matcher.matches());
    }

    @After
    public void archiveServerLog() {
        container().archiveLogs();
    }
}