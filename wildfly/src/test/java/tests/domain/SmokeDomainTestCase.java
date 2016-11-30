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
        String expectedErrorMessage = "OPVDX001: Validation error in duplicate-attribute.xml --------------------------\n" +
                "|\n" +
                "|  95: <job-repository name=\"in-memory\">\n" +
                "|  96:   <jdbc data-source=\"foo\"\n" +
                "|  97:     data-source=\"bar\"/>\n" +
                "|          ^^^^ 'data-source' can't appear more than once on this element\n" +
                "|\n" +
                "|  98: </job-repository>\n" +
                "|  99: <thread-pool name=\"batch\">\n" +
                "| 100:     <max-threads count=\"10\"/>\n" +
                "|\n" +
                "| A 'data-source' attribute first appears here:\n" +
                "|\n" +
                "|  94: <default-thread-pool name=\"batch\"/>\n" +
                "|  95: <job-repository name=\"in-memory\">\n" +
                "|  96:   <jdbc data-source=\"foo\"\n" +
                "|              ^^^^\n" +
                "|\n" +
                "|  97:     data-source=\"bar\"/>\n" +
                "|  98: </job-repository>\n" +
                "|  99: <thread-pool name=\"batch\">\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
        assertExpectedError(StringRegexUtils.convertStringLinesToOneRegex(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "domain-to-cripple.xml", xmlTransformationClass = TypoInExtensions.class)
    public void testWithDynamicCripplingOfXmlWithExistingConfigInResources() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in domain-to-cripple.xml ----------------------------\n" +
                "|\n" +
                "|  1: <?xml version=\"1.0\" encoding=\"UTF-8\"?><domain xmlns=\"urn:jboss:domain:5.0\">\n" +
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
                "|  6:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }

    @Test
    @RunAsClient
    @ServerConfig(configuration = "domain.xml", xmlTransformationClass = AddNonExistentElementToMessagingSubsystem.class)
    public void testWithDynamicCrippling() throws Exception {
        container().tryStartAndWaitForFail();
        // assert that log contains bad message
        String expectedErrorMessage = "OPVDX001: Validation error in domain-to-cripple.xml ----------------------------\n" +
                "|\n" +
                "|  1: <?xml version=\"1.0\" encoding=\"UTF-8\"?><domain xmlns=\"urn:jboss:domain:5.0\">\n" +
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
                "|  6:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n";

        assertExpectedError(StringRegexUtils.addLinesToListAndEscapeRegexChars(StringRegexUtils.removeLineNumbersWithDoubleDotFromString(expectedErrorMessage)),
                container().getErrorMessageFromServerStart());
    }
}
