import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

/**
 * Utility class for copying files.
 * <p>
 * Created by mnovak on 11/1/16.
 */
public class FileUtils {

    public void copyFileFromResourcesToServer(String resourceFile, String targetDirectory, boolean override) throws Exception {
        if (resourceFile == null || "".equals(resourceFile)) {
            return;
        }

        Path sourcePath = getResourceFile(resourceFile);
        if (sourcePath == null) {
            throw new Exception("Resource file " + resourceFile + " does not exist.");
        }

        Path targetPath = Paths.get(targetDirectory, sourcePath.getFileName().toString());
        if (Files.exists(targetPath) && !override) {
            // file already exists in config directory so do nothing
            return;
        }

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path getResourceFile(String file) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(file);
        if (url == null) {
            return null;
        } else {
            return Paths.get(url.getPath());
        }
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        File file = new File(path);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    // just for local testing
//    public static void main(String[] args) {
//        System.out.println(new FileUtils().getResourceFile("duplicaste-attribute.xml"));
//    }

}
