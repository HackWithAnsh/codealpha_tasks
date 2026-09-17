package hotel.io;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Small generic helper around plain-text CSV file I/O so the service layer
 * doesn't need to repeat try/catch boilerplate for every entity type.
 */
public class FileStore {

    public static <T> List<T> load(String path, Function<String, T> parser) {
        List<T> items = new ArrayList<>();
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return items;
        }
        try (BufferedReader reader = Files.newBufferedReader(p)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                items.add(parser.apply(line));
            }
        } catch (IOException e) {
            System.out.println("[Warning] Could not read " + path + ": " + e.getMessage());
        }
        return items;
    }

    public static <T> void saveAll(String path, List<T> items, Function<T, String> toCsv) {
        Path p = Paths.get(path);
        try {
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(p)) {
                for (T item : items) {
                    writer.write(toCsv.apply(item));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("[Warning] Could not write " + path + ": " + e.getMessage());
        }
    }
}
