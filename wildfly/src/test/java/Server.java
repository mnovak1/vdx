import org.jboss.arquillian.container.test.api.ContainerController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public interface Server {

    public static final String JBOSS_HOME = System.getProperty("jboss.home", "jboss-as");
    public static final String DEFAULT_SERVER_CONFIG = OperatingMode.isDomain() ? "domain.xml" : "standalone.xml";
    public static final String DEFAULT_HOST_CONFIG = "host.xml";

    public static final String STANDALONE_DIRECTORY = "standalone";
    public static final String DOMAIN_DIRECTORY = "domain";

    public static final String PATH_TO_STANDALONE_DIRECTORY = Paths.get(Server.JBOSS_HOME, STANDALONE_DIRECTORY, "configuration").toString();
    public static final String PATH_TO_DOMAIN_DIRECTORY = Paths.get(Server.JBOSS_HOME, DOMAIN_DIRECTORY, "configuration").toString();

    public static final String STANDALONE_RESOURCES_DIRECTORY = "configurations" + File.separator + "standalone" + File.separator;
    public static final String DOMAIN_RESOURCES_DIRECTORY = "configurations" + File.separator + "domain" + File.separator;

    static final String LOGGING_PROPERTIES_FILE_NAME = "logging.properties";
    static final String ERRORS_LOG_FILE_NAME = "errors.log";

    static final Server server = null;

    /**
     * Starts the server. If @ServerConfig annotation is present on method in calling stacktrace (for example test method) then
     * it's applied before the server is started.
     *
     * Start of the server is expected to fail due to xml syntac error. It does not throw any exception when  tryStartAndWaitForFail of server fails.
     *
     * @throws Exception
     */
    public void tryStartAndWaitForFail() throws Exception;

    /**
     * Stops server.
     *
     * Not really useful for this testing but can be handy.
     */
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

    public void archiveLogs();

    public Path getServerLog();

    public String getErrorMessageFromServerStart() throws Exception;
}
