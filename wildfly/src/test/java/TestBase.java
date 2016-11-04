import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import transformations.AddNonExistentElementToMessagingSubsystem;
import transformations.TypoInExtensions;

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

    public Server container()   {
        return Server.create(controller);
    }

    @Test
    @ServerConfig(configuration="duplicate-attribute.xml") //, xmlTransformationClass= TypoInExtensions.class)
    public void test() throws Exception {
        container().start();
        //container().stop();

        // container().archiveServerLogToDirectory(path_to_log_directory_for_this_test)
        // parse files in archived log directory that contain given error  - use regular expression to verify error log
    }
}