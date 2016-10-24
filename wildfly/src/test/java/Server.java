import org.jboss.arquillian.container.test.api.ContainerController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;

public interface Server {

    static final Server server = null;

    public void start();

    public void stop();

    /**
     * Creates instance of server. If -Ddomain=true system property is specified it will be domain server,
     * otherwise standalone server will be used.
     *
     * @param controller arquillian container controller
     * @return Server instance
     */
    public static Server create(ContainerController controller) {
        if (server == null) {
            if (OperatingMode.isDomain()) {
                return new ManagedDomain(controller);
            } else {
                return new StandaloneServer(controller);
            }
        }
        return server;
    }
}
