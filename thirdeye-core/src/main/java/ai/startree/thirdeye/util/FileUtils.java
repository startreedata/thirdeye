package ai.startree.thirdeye.util;

import static com.google.api.client.util.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

  @NotNull
  public static <T> T readJsonObject(InputStream file, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(file, clazz);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not inputStream as %s",
          clazz.getSimpleName()));
    }
  }

  public static boolean isRunInJar(Class<?> clazz) {
    return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath()).isFile();
  }

  public static <T> List<T> readJsonObjectsFromResourcesFolder(String folder, Class<?> loaderClazz,
      Class<T> targetClazz) {
    if (isRunInJar(loaderClazz)) {
      try {
        return readJsonObjectsFromResourcesFolderInJar(folder, loaderClazz, targetClazz);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return readJsonObjectsFromResourcesFolderInIDE(folder, loaderClazz, targetClazz);
    }
  }

  /**
   * This methods reads every files in a jar. Avoid running on a big jar.
   */
  public static <T> List<T> readJsonObjectsFromResourcesFolderInJar(String folder,
      Class<?> loaderClazz, Class<T> targetClazz) throws IOException {
    List<T> elements = new ArrayList<>();
    String folderPrefix = folder.endsWith("/") ? folder : folder + "/";
    final JarFile jar = new JarFile(new File(loaderClazz.getProtectionDomain()
        .getCodeSource()
        .getLocation()
        .getPath()));
    final Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      final String name = entries.nextElement().getName();
      if (name.startsWith(folderPrefix) && !name.endsWith("/")) {
        T loaded = readJsonObject(loaderClazz.getClassLoader().getResourceAsStream(name),
            targetClazz);
        elements.add(loaded);
      }
    }
    jar.close();
    return elements;
  }

  public static <T> List<T> readJsonObjectsFromResourcesFolderInIDE(String folder,
      Class<?> loaderClazz, Class<T> targetClazz) {
    URL url = loaderClazz.getClassLoader().getResource(folder);
    checkArgument(url != null, String.format("%s folder not found in resources.", folder));
    final String folderPath = url.getPath();

    return readJsonObjectsFromFolder(folderPath, targetClazz);
  }

  /**
   * Read all files in a given folder. Does not read recursively.
   */
  public static <T> List<T> readJsonObjectsFromFolder(final String folderPath, Class<T> targetClazz) {
    final File folderFile = new File(folderPath);
    if (!folderFile.exists() || !folderFile.isDirectory()) {
      throw new IllegalArgumentException("Folder not found: " + folderPath);
    }
    final File[] files = requireNonNull(folderFile.listFiles());
    List<T> elements = new ArrayList<>();
    for (File jsonFile : files) {
      if (jsonFile.isFile()) {
        elements.add(readJsonObject(jsonFile, targetClazz));
      }
    }
    return elements;
  }
}
