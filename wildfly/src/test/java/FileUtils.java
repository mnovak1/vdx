import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for copying files.
 * <p>
 * Created by mnovak on 11/1/16.
 */
public class FileUtils {

    public void copyFileFromResourcesToServerIfItDoesNotExist(String fileToCopy) throws Exception {
        if (fileToCopy == null || "".equals(fileToCopy)) {
            return;
        }

        Path targetPath = Paths.get(Server.JBOSS_HOME, OperatingMode.isDomain() ? Server.DOMAIN_DIRECTORY : Server.STANDALONE_DIRECTORY, "configuration", fileToCopy);
        if (Files.exists(targetPath)) {
            // file already exists in config directory so do nothing
            return;
        }

        // this will throw exception because we know that such does not exist anywhere - in resources
        // and $JBOSS_HOME/standalone|domain/configuration directory
        Path sourcePath = getResourceFile(fileToCopy);
        if (sourcePath == null) {
            throw new Exception("Configuration file " + fileToCopy + " does not exist.");
        }

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING); // we do not replace but there is no other option
    }

    private Path getResourceFile(String file) {

        ClassLoader classLoader = getClass().getClassLoader();
        URL url = null;
        if (OperatingMode.isDomain()) {
            url = classLoader.getResource("examples/domain/" + file);
        } else {
            url = classLoader.getResource("examples/standalone/" + file);
        }

        if (url == null) {
            return null;
        } else {
            return Paths.get(url.getPath());
        }
    }

    // just for local testing
//    public static void main(String[] args) {
//        System.out.println(new FileUtils().getResourceFile("duplicaste-attribute.xml"));
//    }

}
