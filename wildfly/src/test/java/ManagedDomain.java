import org.jboss.arquillian.container.test.api.ContainerController;

import java.util.HashMap;
import java.util.Map;

public class ManagedDomain extends AbstractServer {

//    private static final String DEFAULT_VM_ARGUMENTS =  "-server -Xms64m -Xmx512m -XX:MaxMetaspaceSize=256m " +
//            "-Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true";

    ContainerController controller;

    public ManagedDomain(ContainerController controller)    {
        this.controller = controller;
    }

    @Override
    public void start() {

        ServerConfig serverConfig = getServerConfig();
        Map<String, String> containerProperties = new HashMap<>();
        if (serverConfig != null) {
            containerProperties.put("serverConfig", serverConfig.configuration());
            containerProperties.put("hostConfig", serverConfig.hostConfig());
        } else { // if no server config was specified return arquillian to default // todo take this from arquillian.xml
            containerProperties.put("serverConfig", "domain.xml");
            containerProperties.put("hostConfig", "host.xml");
        }
        controller.start(TestBase.DOMAIN_ARQUILLIAN_CONTAINER, containerProperties);
    }

    @Override
    public void stop()  {
        controller.stop(TestBase.DOMAIN_ARQUILLIAN_CONTAINER);
    }
}
