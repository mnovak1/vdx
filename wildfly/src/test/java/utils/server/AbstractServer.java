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

package utils.server;

import org.junit.Assert;
import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import transformations.DoNothing;
import utils.FileUtils;
import utils.OperatingMode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractServer implements Server {

    protected ConfigurationFileBackup configurationFileBackup = new ConfigurationFileBackup();
    ;

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
        applyXmlTransformation();

        try {
            // tryStartAndWaitForFail - this must throw exception due invalid xml
            startServer();

            // fail the test if server starts
            Assert.fail("Server started succesfully - probably xml was not invalidated/crippled correctly.");

        } catch (Exception ex) {
            System.out.println("Start of the server failed. This is expected.");
        } finally {
            // restore original config if it exists
            restoreConfigIfBackupExists();
        }
    }

    @Override
    public abstract Path getServerLog();

    @Override
    public String getErrorMessageFromServerStart() throws Exception {
        return FileUtils.readFile(Paths.get(ERRORS_LOG_FILE_NAME).toString());
    }

    /**
     * This will copy file from resources directory to $JBOSS_HOME/<profile>/configuration directory and only if
     * this file does not exist in this configuration directory.
     * <p>
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
        System.out.println("Restoring server configuration. Configuration to be restored " + getServerConfig());
        getOfflineManangementClient().apply(configurationFileBackup.restore());
    }

    protected abstract OfflineManagementClient getOfflineManangementClient() throws Exception;

    protected void backupConfiguration() throws Exception {
        // todo creaper most likely does not backup/restore host.xml - double check this
        // destroy any existing backup config
        getOfflineManangementClient().apply(configurationFileBackup.destroy());
        // backup any existing config
        getOfflineManangementClient().apply(configurationFileBackup.backup());
    }

    /**
     * Copies logging.properties which will log ERROR messages to $JBOSS_HOME/bin/errors.log file
     *
     * @throws Exception
     */
    protected abstract void copyLoggingPropertiesToConfiguration() throws Exception;

    /**
     * Cripples xml config file only if config file had valid syntax. It cannot cripple invalid xml file.
     * <p>
     * IT THROWS EXCEPTION IF CONFIG FILE IS NOT XML VALID.
     *
     * @throws Exception if file not xml valid
     */
    private void applyXmlTransformation() throws Exception {
        ServerConfig serverConfig = getServerConfig();

        if (serverConfig.xmlTransformationGroovy().equals("")) {
            if (DoNothing.class.equals(serverConfig.xmlTransformationClass())) {
                return;
            }

            getOfflineManangementClient().apply(GroovyXmlTransform.of(serverConfig.xmlTransformationClass()).build());

        } else {

            if (serverConfig.subtreeName().equals("")) {  // case without subtree
                getOfflineManangementClient()
                        .apply(GroovyXmlTransform.of(DoNothing.class, serverConfig.xmlTransformationGroovy()).build());
            }
            if (serverConfig.profileName().equals("")) {  // standalone case with subtree
                getOfflineManangementClient()
                        .apply(GroovyXmlTransform.of(DoNothing.class, serverConfig.xmlTransformationGroovy()).
                                subtree(serverConfig.subtreeName(), Subtree.subsystem(serverConfig.subsystemName())).build());

            } else {  // domain case  with subtree
                getOfflineManangementClient()
                        .apply(GroovyXmlTransform.of(DoNothing.class, serverConfig.xmlTransformationGroovy()).
                                subtree(serverConfig.subtreeName(),
                                        Subtree.subsystemInProfile(serverConfig.profileName(), serverConfig.subsystemName()))
                                .build());

            }
        }

    }

    /**
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
                if (serverConfig != null) {
                    break;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return serverConfig;
    }


}
