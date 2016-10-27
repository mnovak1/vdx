import org.jboss.arquillian.container.test.api.ContainerController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;

public interface Server {

    public static final String JBOSS_HOME = System.getProperty("jboss.home", "jboss-as");
    public static final String DEFAULT_SERVER_CONFIG = OperatingMode.isDomain() ? "domain.xml" : "standalone.xml";
    public static final String DEFAULT_HOST_CONFIG = "host.xml";

    static final Server server = null;

    /**
     * Starts the server. If @ServerConfig annotation is present on method in calling stacktrace (for example test) then
     * it's applied before the server is started.
     *
     * @throws Exception
     */
    public void start() throws Exception;

    public void stop();

    /**
     * Creates instance of server. If -Ddomain=true system property is specified it will be domain server,
     * otherwise standalone server will be used.
     *
     * @param controller arquillian container controller
     * @return Server instance - standalone by default or domain if -Ddomain=true is set
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
