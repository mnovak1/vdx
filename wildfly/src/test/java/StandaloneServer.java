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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StandaloneServer extends AbstractServer {

//    private static final String DEFAULT_VM_ARGUMENTS = "-server -Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m " +
//            "-Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true";

    private ContainerController controller;

    protected StandaloneServer(ContainerController controller) {
        this.controller = controller;
    }

    protected void startServer() throws Exception {
        ServerConfig serverConfig = getServerConfig();
        Map<String, String> containerProperties = new HashMap<>();
        if (serverConfig != null) {
            containerProperties.put("serverConfig", serverConfig.configuration());
            //containerProperties.put("vmArguments", DEFAULT_VM_ARGUMENTS.concat(" -Djboss.server.log.dir=" + logDirectory));
            // apply xml transformation defined in ServerConfig
        } else { // if no server config was specified return arquillian to default
            containerProperties.put("serverConfig", DEFAULT_SERVER_CONFIG);
        }

        controller.start(TestBase.STANDALONE_ARQUILLIAN_CONTAINER, containerProperties);
    }

    @Override
    protected OfflineManagementClient getOfflineManangementClient() throws Exception {
        return ManagementClient.offline(OfflineOptions
                .standalone()
                .rootDirectory(new File(JBOSS_HOME))
                .configurationFile(getServerConfig() == null ? DEFAULT_SERVER_CONFIG : getServerConfig().configuration())
                .build());
    }

    @Override
    public Path getServerLog() {
        return Paths.get(JBOSS_HOME, STANDALONE_DIRECTORY, "log", "server.log");
    }

    @Override
    protected void copyConfigFilesFromResourcesIfItDoesNotExist() throws Exception {
        if (!FileUtils.isPathExists(Paths.get(PATH_TO_STANDALONE_DIRECTORY.toString(), getServerConfig().configuration()))) {
            new FileUtils().copyFileFromResourcesToServer(STANDALONE_RESOURCES_DIRECTORY + getServerConfig().configuration(), PATH_TO_STANDALONE_DIRECTORY, false);
        }
    }

    @Override
    public void stop() {
        controller.stop(TestBase.STANDALONE_ARQUILLIAN_CONTAINER);
    }

    protected void copyLoggingPropertiesToConfiguration() throws Exception {
        String loggingPropertiesInResources = STANDALONE_RESOURCES_DIRECTORY + LOGGING_PROPERTIES_FILE_NAME;
        new FileUtils().copyFileFromResourcesToServer(loggingPropertiesInResources, PATH_TO_STANDALONE_DIRECTORY, true);
    }

}
