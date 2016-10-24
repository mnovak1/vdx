import org.jboss.arquillian.container.test.api.ContainerController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StandaloneServer extends AbstractServer {

//    private static final String DEFAULT_VM_ARGUMENTS = "-server -Xms1303m -Xmx1303m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m " +
//            "-Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true";

    private ContainerController controller;

    public StandaloneServer(ContainerController controller) {
        this.controller = controller;
    }

    @Override
    public void start() {

        ServerConfig serverConfig = getServerConfig();
        Map<String, String> containerProperties = new HashMap<>();
        if (serverConfig != null) {
            containerProperties.put("serverConfig", serverConfig.configuration());
            //containerProperties.put("vmArguments", DEFAULT_VM_ARGUMENTS.concat(" -Djboss.server.log.dir=" + logDirectory));
        } else { // if no server config was specified return arquillian to default // todo take this from arquillian.xml
            containerProperties.put("serverConfig", "standalone.xml");
        }
        controller.start(TestBase.STANDALONE_ARQUILLIAN_CONTAINER, containerProperties);
    }

    @Override
    public void stop() {
        controller.stop(TestBase.STANDALONE_ARQUILLIAN_CONTAINER);
    }

}
