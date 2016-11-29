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

package tests;import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import utils.FileUtils;
import utils.server.Server;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Do not inherit from this class as it's common for standalone and domain tests! For standalone tests inherit from
 * @see tests.standalone.StandaloneTestBase and for domain tests inherit from @see tests.domain.DomainTestBase.
 */
@RunWith(Arquillian.class)
public class TestBase {

    public static final String STANDALONE_ARQUILLIAN_CONTAINER = "jboss";
    public static final String DOMAIN_ARQUILLIAN_CONTAINER = "jboss-domain";

    @ArquillianResource
    private ContainerController controller;

    @Rule
    public TestName testName = new TestName();

    private Path testDirectory;

    public Server container() {
        return Server.create(controller);
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
        try {
            new FileUtils().copyFileToDirectory(container().getServerLog(), archiveDirectory.toPath());
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
        container().getServerLog().toFile().delete();
    }

    @Test
    public void mockTest()  {
        // ignore me, this is to prevent "java.lang.Exception: No runnable methods" thrown from junit
    }
}
