import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import transformations.DoNothing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by mnovak on 10/24/16.
 */
public abstract class AbstractServer implements Server {

    protected ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();;

    @Override
    public void start() throws Exception {

        // if configuration file is not in configuration directory then copy from resources directory (never override)
        copyConfigFilesFromResourcesIfItDoesNotExist();
        // backup config
        backupConfiguration();
        // modify config - only valid configuration files can be crippled
        applyXmlTransformation(getServerConfig().xmlTransformationClass());
        // start
        startServer();
        // restore original config if it exists
        restoreConfigIfBackupExists();
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
                    "startServer() call. Check start() sequence that backupConfiguration() was called.");
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
     * Cripples config file only if config file had correct syntax.
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
     * @return returns Search stackstrace for @ServerConfig annotation and return it, returns null if there is none
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
