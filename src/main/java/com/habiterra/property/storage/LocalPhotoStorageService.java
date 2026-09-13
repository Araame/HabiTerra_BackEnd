package com.habiterra.property.storage;

import com.habiterra.property.exception.PropertyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalPhotoStorageService implements PhotoStorageService {
    private final Path root;
    private final long maxBytes;

    public LocalPhotoStorageService(@Value("${property.photos.directory:./var/property-photos}") String directory,
            @Value("${property.photos.max-bytes:10485760}") long maxBytes) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > maxBytes)
            throw new PropertyException(400, "INVALID_PHOTO", "A photo within the configured size limit is required");
        try (var input = ImageIO.createImageInputStream(file.getInputStream())) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();
            var reader = readers.next();
            String extension;
            java.awt.image.BufferedImage image;
            try {
                reader.setInput(input);
                String format = reader.getFormatName();
                extension = format.equalsIgnoreCase("JPEG") ? "jpg" : format.equalsIgnoreCase("PNG") ? "png" : null;
                if (extension == null || reader.getWidth(0) <= 0 || reader.getHeight(0) <= 0
                        || (long) reader.getWidth(0) * reader.getHeight(0) > 25000000L) throw invalidImage();
                image = reader.read(0);
                if (image == null) throw invalidImage();
            } finally { reader.dispose(); }
            Files.createDirectories(root);
            Path target = root.resolve(UUID.randomUUID() + "." + extension);
            try {
                // Decode and re-encode: strip untrusted payloads and camera metadata.
                if (!ImageIO.write(image, extension, target.toFile())) throw new IOException("Encoder unavailable");
            } catch (IOException | RuntimeException error) {
                Files.deleteIfExists(target);
                throw error;
            }
            return target.getFileName().toString();
        } catch (PropertyException error) { throw error; }
        catch (IOException error) {
            throw new PropertyException(503, "PHOTO_STORAGE_FAILED", "Photo could not be stored");
        }
    }

    private PropertyException invalidImage() {
        return new PropertyException(400, "INVALID_PHOTO", "Only valid JPEG or PNG images up to 25 megapixels are accepted");
    }

    private Path resolve(String url) {
        if (url == null || !url.matches("[0-9a-f-]{36}\\.(jpg|png)")) throw invalidImage();
        Path path = root.resolve(url).normalize();
        if (!path.startsWith(root)) throw invalidImage();
        return path;
    }

    @Override
    public void delete(String url) {
        try { Files.deleteIfExists(resolve(url)); }
        catch (IOException error) {
            throw new PropertyException(503, "PHOTO_STORAGE_FAILED", "Photo could not be deleted");
        }
    }

    @Override
    public Resource load(String url) {
        Path path = resolve(url);
        if (!Files.isRegularFile(path))
            throw new PropertyException(404, "PHOTO_NOT_FOUND", "Photo file not found");
        return new PathResource(path);
    }
}

