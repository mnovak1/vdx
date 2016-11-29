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

package tests.domain;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import tests.TestBase;
import transformations.AddNonExistentElementToMessagingSubsystem;
import transformations.TypoInExtensions;
import utils.StringRegexUtils;
import utils.server.ServerConfig;

/**
 * Created by mnovak on 11/28/16.
 */
public class SmokeDomainTestCase extends DomainTestBase {
    @Test
    @RunAsClient
    @ServerConfig(configuration = "duplicate-attribute.xml")
    public void testWithExistingConfigInResources() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in standalone-full-ha-to-cripple.xml ----------------\n" +
                "|\n" +
                "|  1: <?xml version=\"1.0\" encoding=\"UTF-8\"?><server xmlns=\"urn:jboss:domain:5.0\">\n" +
                "|  2:   <extensions>\n" +
                "|  3:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|                    ^^^^ 'modules' isn't an allowed attribute for the 'extension' element\n" +
                "|                         \n" +
                "|                         Did you mean 'module'?\n" +
                "|                         \n" +
                "|                         Attributes allowed here are: module \n" +
                "|\n" +
                "|  4:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|  5:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|  6:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|\n" +
                "| The primary underlying error message was:\n" +
                "| > ParseError at [row,col]:[3,5]\n" +
                "| > Message: WFLYCTL0197: Unexpected attribute 'modules' encountered\n" +
                "|\n" +
                "|-------------------------------------------------------------------------------\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
        assertExpectedError(StringRegexUtils.convertStringLinesToOneRegex(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "standalone-full-ha-to-cripple.xml", xmlTransformationClass = TypoInExtensions.class)
    public void testWithDynamicCripplingOfXmlWithExistingConfigInResources() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in standalone-full-ha-to-cripple.xml ----------------\n" +
                "|\n" +
                "|  1: <?xml version=\"1.0\" encoding=\"UTF-8\"?><server xmlns=\"urn:jboss:domain:5.0\">\n" +
                "|  2:   <extensions>\n" +
                "|  3:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|                    ^^^^ 'modules' isn't an allowed attribute for the 'extension' element\n" +
                "|                         \n" +
                "|                         Did you mean 'module'?\n" +
                "|                         \n" +
                "|                         Attributes allowed here are: module \n" +
                "|\n" +
                "|  4:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|  5:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|  6:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "|\n" +
                "| The primary underlying error message was:\n" +
                "| > ParseError at [row,col]:[3,5]\n" +
                "| > Message: WFLYCTL0197: Unexpected attribute 'modules' encountered\n" +
                "|\n" +
                "|-------------------------------------------------------------------------------\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "standalone-full-ha.xml", xmlTransformationClass = AddNonExistentElementToMessagingSubsystem.class)
    public void testWithDynamicCrippling() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in standalone-full-ha.xml ---------------------------\n" +
                "|\n" +
                "|  370: <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.1\">\n" +
                "|  371:   <server name=\"default\">\n" +
                "|  372:     <cluster id=\"3\"/>\n" +
                "|                    ^^^^ 'id' isn't an allowed attribute for the 'cluster' element\n" +
                "|                         \n" +
                "|                         Attributes allowed here are: name, password, user \n" +
                "|\n" +
                "|  373:     <journal min-files=\"10\" compact-min-files=\"0\" type=\"ASYNCIO\"/>\n" +
                "|  374:     <security enabled=\"false\"/>\n" +
                "|  375:     <security-setting name=\"#\">\n" +
                "|\n" +
                "| 'id' is allowed on elements: \n" +
                "| - server > profile > {urn:jboss:domain:resource-adapters:4.0}subsystem > resource-adapters > resource-adapter\n" +
                "| - server > profile > {urn:jboss:domain:resource-adapters:4.0}subsystem > resource-adapters > resource-adapter > module\n" +
                "|\n" +
                "|\n" +
                "| The primary underlying error message was:\n" +
                "| > ParseError at [row,col]:[372,9]\n" +
                "| > Message: WFLYCTL0376: Unexpected attribute 'id' encountered. Valid\n" +
                "| >   attributes are: 'user, password, name'\n" +
                "|\n" +
                "|-------------------------------------------------------------------------------";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }
}
