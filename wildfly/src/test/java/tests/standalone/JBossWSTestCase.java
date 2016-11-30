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

package tests.standalone;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import utils.server.ServerConfig;

import static org.junit.Assert.assertTrue;

/**
 * Created by rsvoboda on 11/30/16.
 */

@RunAsClient public class JBossWSTestCase extends StandaloneTestBase {

    /*
     * <modify-wsdl-address /> instead of <modify-wsdl-address>true</modify-wsdl-address>
     */
    @Test
    @ServerConfig(configuration = "standalone.xml", xmlTransformationGroovy = "webservices/AddWsdlAddressElementWithNoValue.groovy",
            subtreeName = "webservices", subsystemName = "webservices")
    public void addWsdlAddressElementWithNoValue()throws Exception {
        container().tryStartAndWaitForFail();

        String errorLog = container().getErrorMessageFromServerStart();
        assertTrue(errorLog.contains("OPVDX001: Validation error in standalone.xml"));
        assertTrue(errorLog.contains("<modify-wsdl-address/>"));
        assertTrue(errorLog.contains(" ^^^^ Wrong type for 'modify-wsdl-address'. Expected [BOOLEAN] but was"));
        assertTrue(errorLog.contains("|                  STRING"));

    }
}