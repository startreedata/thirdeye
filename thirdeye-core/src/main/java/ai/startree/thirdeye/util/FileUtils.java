package ai.startree.thirdeye.util;

import static com.google.api.client.util.Preconditions.checkArgument;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public class FileUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @NotNull
  public static <T> T readJsonObject(File file, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(file, clazz);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not read file: %s as %s",
          file.getAbsolutePath(),
          clazz.getSimpleName()));
    }
  }

  public static File[] getFilesFromResourcesFolder(String folder, ClassLoader loader) {
    URL url = loader.getResource(folder);
    checkArgument(url != null, String.format("%s folder not found in resources.", folder));
    String path = url.getPath();
    return new File(path).listFiles();
  }
}
