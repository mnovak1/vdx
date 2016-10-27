import org.jboss.arquillian.container.test.api.ContainerController;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    protected void copyConfigFilesFromResourcesIfItDoesNotExist() throws IOException {

        String serverConfiguration = getServerConfig().configuration();
        Path targetPath = Paths.get(JBOSS_HOME, "standalone", "configuration", serverConfiguration);

        if (Files.exists(targetPath)) {
            // file already exists in config directory so do nothing
            return;
        } else {
            // find xml in resources directory
            ClassLoader classLoader = getClass().getClassLoader();
            Path sourcePath = Paths.get(classLoader.getResource("examples/standalone/" + serverConfiguration).getPath());
            Files.copy(sourcePath,targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public void stop() {
        controller.stop(TestBase.STANDALONE_ARQUILLIAN_CONTAINER);
    }

}
