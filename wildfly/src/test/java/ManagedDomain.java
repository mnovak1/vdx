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
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ManagedDomain extends AbstractServer {

//    private static final String DEFAULT_VM_ARGUMENTS =  "-server -Xms64m -Xmx512m -XX:MaxMetaspaceSize=256m " +
//            "-Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true";

    ContainerController controller;

    protected ManagedDomain(ContainerController controller)    {
        this.controller = controller;
    }

    @Override
    public void startServer() {

        ServerConfig serverConfig = getServerConfig();
        Map<String, String> containerProperties = new HashMap<>();
        if (serverConfig != null) {
            containerProperties.put("serverConfig", serverConfig.configuration());
            containerProperties.put("hostConfig", serverConfig.hostConfig());
        } else { // if no server config was specified return arquillian to default // todo take this from arquillian.xml
            containerProperties.put("serverConfig", DEFAULT_SERVER_CONFIG);
            containerProperties.put("hostConfig", DEFAULT_HOST_CONFIG);
        }

        controller.start(TestBase.DOMAIN_ARQUILLIAN_CONTAINER, containerProperties);
    }

    @Override
    protected void copyConfigFilesFromResourcesIfItDoesNotExist() throws Exception {
        new FileUtils().copyFileFromResourcesToServer(DOMAIN_RESOURCES_DIRECTORY + getServerConfig().configuration(), PATH_TO_DOMAIN_DIRECTORY, false);
        new FileUtils().copyFileFromResourcesToServer(DOMAIN_RESOURCES_DIRECTORY + getServerConfig().hostConfig(), PATH_TO_DOMAIN_DIRECTORY, false);
    }



    @Override
    protected OfflineManagementClient getOfflineManangementClient() throws Exception {
        return ManagementClient.offline(OfflineOptions
                .domain()
                .forProfile("default").build().baseDirectory(new File(JBOSS_HOME))
                .configurationFile(getServerConfig() == null ? DEFAULT_SERVER_CONFIG : getServerConfig().configuration())
                .build());
    }

    @Override
    public void stop()  {
        controller.stop(TestBase.DOMAIN_ARQUILLIAN_CONTAINER);
    }

    /**
     * Copies logging.properties which will log ERROR messages to $JBOSS_HOME/bin/errors.log file
     * @throws Exception
     */
    protected void copyLoggingPropertiesToConfiguration() throws Exception {
        String loggingPropertiesInResources = DOMAIN_RESOURCES_DIRECTORY + LOGGING_PROPERTIES_FILE_NAME;
        new FileUtils().copyFileFromResourcesToServer(loggingPropertiesInResources, PATH_TO_DOMAIN_DIRECTORY, true);
    }
}
