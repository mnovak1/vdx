import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by mnovak on 10/24/16.
 */
public abstract class AbstractServer implements Server {

    protected ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();;

    @Override
    public void start() throws Exception {

        // if config not in config  diretory then copy from resources
        copyConfigFilesFromResourcesIfItDoesNotExist();
        // backup config
        backupConfiguration();
        // modify config
        applyXmlTransformation(getServerConfig().xmlTransformationClass());
        // start
        startServer();
        // restore original config if it exists
        restoreConfigIfBackupExists();
    }


    protected abstract void copyConfigFilesFromResourcesIfItDoesNotExist() throws IOException;
    protected abstract void startServer() throws Exception;

    protected void restoreConfigIfBackupExists() throws Exception {
        if (configurationFileBackup == null) {
            throw new Exception("Backup config is null. This can happen if this method is called before " +
                    "startServer() call. Check start() sequence that backupConfiguration() was called.");
        }
        getOfflineManangementClient().apply(configurationFileBackup.backup());
    }

    protected abstract OfflineManagementClient getOfflineManangementClient() throws Exception;

    protected void backupConfiguration() throws Exception {
        // destroy any existing backup config
        getOfflineManangementClient().apply(configurationFileBackup.destroy());
        // backup any existing config
        getOfflineManangementClient().apply(configurationFileBackup.backup());
    }

    protected void applyXmlTransformation(Class xmlTransformationClass) throws Exception {
        getOfflineManangementClient().apply(GroovyXmlTransform.of(xmlTransformationClass).build());
    }

    /**
     *
     * @return returns search calling stackstrace for @ServerConfig annotation and returns it, returns null if there is none
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
