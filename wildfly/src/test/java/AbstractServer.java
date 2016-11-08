import org.junit.Assert;
import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import transformations.DoNothing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by mnovak on 10/24/16.
 */
public abstract class AbstractServer implements Server {

    protected ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();;

    @Override
    public void tryStartAndWaitForFail() throws Exception {

        // stop server if running
        stop();

        // copy logging.properties
        copyLoggingPropertiesToConfiguration();

        // if configuration file is not in configuration directory then copy from resources directory (never override)
        copyConfigFilesFromResourcesIfItDoesNotExist();

        // backup config
        backupConfiguration();

        // modify config - only valid configuration files can be crippled
        applyXmlTransformation(getServerConfig().xmlTransformationClass());

        try {
            // tryStartAndWaitForFail - this must throw exception due invalid xml
            startServer();

            // fail the test if server starts
            Assert.fail("Server started succesfully - probably xml was not invalidated/crippled correctly.");

        } catch (Exception ex)  {
            System.out.println("Start of the server failed.");
        } finally {
            // restore original config if it exists
            restoreConfigIfBackupExists();
        }
    }

    @Override
    public void archiveLogs() {
        // create directory with name of the test


        // copy there all server.log files for standalone or domain/log/host-controller.log for domain
    }

    @Override
    public Path getServerLog() {
        Path pathToLog = null;
        if (OperatingMode.isDomain())   {
            pathToLog = Paths.get(JBOSS_HOME, DOMAIN_DIRECTORY, "log", "host-controller.log");
        } else {
            pathToLog = Paths.get(JBOSS_HOME, STANDALONE_DIRECTORY, "log", "server.log");
        }
        return pathToLog;
    }

    @Override
    public String getErrorMessageFromServerStart() throws Exception {
        return FileUtils.readFile(Paths.get("errors.log").toString(), StandardCharsets.UTF_8);
    }


    /**
     * This will copy file from resources directory to $JBOSS_HOME/<profile>/configuration directory and only if
     * this file does not exist in this configuration directory.
     *
     * This never overrides existing files.
     *
     * @throws IOException
     */
    protected abstract void copyConfigFilesFromResourcesIfItDoesNotExist() throws Exception;
    protected abstract void startServer() throws Exception;

    protected void restoreConfigIfBackupExists() throws Exception {
        if (configurationFileBackup == null) {
            throw new Exception("Backup config is null. This can happen if this method is called before " +
                    "startServer() call. Check tryStartAndWaitForFail() sequence that backupConfiguration() was called.");
        }
        getOfflineManangementClient().apply(configurationFileBackup.restore());
    }

    protected abstract OfflineManagementClient getOfflineManangementClient() throws Exception;

    protected void backupConfiguration() throws Exception {
        // destroy any existing backup config
        getOfflineManangementClient().apply(configurationFileBackup.destroy());
        // backup any existing config
        getOfflineManangementClient().apply(configurationFileBackup.backup());
    }

    /**
     * Copies logging.properties which will log ERROR messages to $JBOSS_HOME/bin/errors.log file
     * @throws Exception
     */
    protected abstract void copyLoggingPropertiesToConfiguration() throws Exception;

    /**
     * Cripples xml config file only if config file had valid syntax. It cannot cripple invalid xml file.
     *
     * IT THROWS EXCEPTION IF CONFIG FILE IS NOT XML VALID.
     *
     * @param xmlTransformationClass
     * @throws Exception if file not xml valid
     */
    protected void applyXmlTransformation(Class xmlTransformationClass) throws Exception {
        if (DoNothing.class.equals(xmlTransformationClass)) {
            return;
        }
        getOfflineManangementClient().apply(GroovyXmlTransform.of(xmlTransformationClass).build());
    }

    /**
     *
     * @return returns Search stacktrace for @ServerConfig annotation and return it, returns null if there is none
     */
    static ServerConfig getServerConfig() {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String callerMethodName;
        String callerClassName;
        ServerConfig serverConfig = null;

        for (int level = 1; level < elements.length; level++) {
            try {
                callerClassName = elements[level].getClassName();
                callerMethodName = elements[level].getMethodName();
                Method method = Class.forName(callerClassName).getMethod(callerMethodName);
                serverConfig = method.getAnnotation(ServerConfig.class);
                if (serverConfig != null)   {
                    break;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return serverConfig;
    }



}
