/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import transformations.AddNonExistentElementToMessagingSubsystem;
import transformations.TypoInExtensions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test
    @RunAsClient
    @ServerConfig(configuration = "duplicate-attribute.xml")
    public void testWithExistingConfigInResources() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in duplicate-attribute.xml ==========================\n" +
                "\n" +
                "  123: <job-repository name=\"in-memory\">\n" +
                "  124:   <jdbc data-source=\"foo\"\n" +
                "  125:         data-source=\"bar\"/>\n" +
                "               ^^^^ 'data-source' can't appear more than once on this element\n" +
                "\n" +
                "  126: </job-repository>\n" +
                "  127: <thread-pool name=\"batch\">\n" +
                "  128:     <max-threads count=\"10\"/>\n" +
                "\n" +
                " A 'data-source' attribute first appears here:\n" +
                "\n" +
                "  122: <default-thread-pool name=\"batch\"/>\n" +
                "  123: <job-repository name=\"in-memory\">\n" +
                "  124:   <jdbc data-source=\"foo\"\n" +
                "               ^^^^\n" +
                "\n" +
                "  125:         data-source=\"bar\"/>\n" +
                "  126: </job-repository>\n" +
                "  127: <thread-pool name=\"batch\">\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
        assertExpectedError(StringRegexUtils.convertStringLinesToOneRegex(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "standalone-full-ha-to-cripple.xml", xmlTransformationClass= TypoInExtensions.class)
    public void testWithDynamicCripplingOfXmlWithExistingConfigInResources() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in standalone-full-ha-to-cripple.xml ================\n" +
                "\n" +
                "  1: <?xml version=\"1.0\" encoding=\"UTF-8\"?><server xmlns=\"urn:jboss:domain:5.0\">\n" +
                "  2:   <extensions>\n" +
                "  3:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "                    ^^^^ 'modules' isn't an allowed attribute for the 'extension' element\n" +
                "                         Attributes allowed here are: module\n" +
                "                         Did you mean 'module'?\n" +
                "\n" +
                "  4:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "  5:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "  6:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "standalone-full-ha.xml", xmlTransformationClass= AddNonExistentElementToMessagingSubsystem.class)
    public void testWithDynamicCrippling() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in standalone-full-ha.xml ===========================\n" +
                "\n" +
                "  370: <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.1\">\n" +
                "  371:   <server name=\"default\">\n" +
                "  372:     <cluster id=\"3\"/>\n" +
                "                    ^^^^ 'id' isn't an allowed attribute for the 'cluster' element\n" +
                "                         Attributes allowed here are: password, user\n" +
                "\n" +
                "  373:     <journal min-files=\"10\" compact-min-files=\"0\" type=\"ASYNCIO\"/>\n" +
                "  374:     <security enabled=\"false\"/>\n" +
                "  375:     <security-setting name=\"#\">\n" +
                "\n" +
                " 'id' is allowed on elements: \n" +
                " - server > profile > subsystem urn:jboss:domain:resource-adapters:4.0 > resource-adapters > resource-adapter\n" +
                " - server > profile > subsystem urn:jboss:domain:resource-adapters:4.0 > resource-adapters > resource-adapter > module\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    /**
     * Asserts that error message from server contains all regular expressions. If one fails then test fails.
     * This is useful because it can provide exact place where the pattern does not match.
     *
     * @param regexs       list of regular expressions
     * @param errorMessage error message
     */
    protected void assertExpectedError(List<String> regexs, String errorMessage) {
        for (String regex : regexs) {
            assertExpectedError(regex, errorMessage);
        }
    }

    /**
     * Asserts that error message from server contains regular expression. Fails test if it does not contain it.
     *
     * @param regex        regular expression
     * @param errorMessage error message
     */
    protected void assertExpectedError(String regex, String errorMessage) {
        Pattern expectedError = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = expectedError.matcher(errorMessage);
        Assert.assertTrue("Error log message does not match the pattern. Failing the test. \n" +
                "########################## Pattern ##############################\n" +
                expectedError.toString() + " \n" +
                "########################## Error log ##############################\n" +
                errorMessage + " \n" +
                "########################################################\n" +
                "########################################################\n", matcher.matches());
    }

    @Before
    public void setUp() {
        System.out.println("----------------------------------------- Start " + this.getClass().getSimpleName()
                + " - " + testName.getMethodName() + " -----------------------------------------");
        testDirectory = Paths.get("target", "server-logs", this.getClass().getSimpleName(), testName.getMethodName());
    }

    @After
    public void archiveServerLog() throws Exception {
        System.out.println("----------------------------------------- Stop " + this.getClass().getSimpleName()
                + " - " + testName.getMethodName() + " -----------------------------------------");
        archiveServerLogAndDeleteIt(testDirectory);
    }

    protected void archiveServerLogAndDeleteIt(Path pathToArchiveDirectory) throws Exception {
        // create directory with name of the test in target directory
        File archiveDirectory = pathToArchiveDirectory.toFile();
        if (!archiveDirectory.exists()) {
            archiveDirectory.mkdirs();
        }
        // copy server.log files for standalone or host-controller.log for domain
        new FileUtils().copyFileToDirectory(container().getServerLog(), archiveDirectory.toPath());
        container().getServerLog().toFile().delete();
    }
}
