package de.agiehl.mediauploader.upload;

import de.agiehl.mediauploader.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class UploadService {

    private static final DateTimeFormatter DATE_PREFIX = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final Path uploadDirectory;
    private final Clock clock;

    public UploadService(AppProperties properties, Clock clock) {
        this.uploadDirectory = properties.upload().directory().toAbsolutePath().normalize();
        this.clock = clock;
    }

    public List<String> store(List<MultipartFile> files) throws IOException {
        Files.createDirectories(uploadDirectory);
        List<String> storedNames = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            validateMediaType(file);
            String originalName = safeFilename(file.getOriginalFilename());
            String datedName = LocalDate.now(clock).format(DATE_PREFIX) + "_" + originalName;
            Path destination = copyWithoutOverwriting(file, datedName);
            storedNames.add(destination.getFileName().toString());
        }
        if (storedNames.isEmpty()) {
            throw new InvalidUploadException("upload.error.empty");
        }
        return storedNames;
    }

    private void validateMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new InvalidUploadException("upload.error.mediaOnly");
        }
    }

    private String safeFilename(String originalName) {
        String name = originalName == null ? "upload" : Path.of(originalName).getFileName().toString();
        name = name.replaceAll("[^\\p{L}\\p{N}._ -]", "_").replaceAll("\\s+", "_");
        return name.isBlank() || name.equals(".") || name.equals("..") ? "upload" : name;
    }

    private Path copyWithoutOverwriting(MultipartFile file, String filename) throws IOException {
        int dot = filename.lastIndexOf('.');
        String base = dot > 0 ? filename.substring(0, dot) : filename;
        String extension = dot > 0 ? filename.substring(dot) : "";

        for (int number = 1; ; number++) {
            String candidateName = number == 1 ? filename : base + "_" + number + extension;
            Path candidate = uploadDirectory.resolve(candidateName).normalize();
            ensureInsideUploadDirectory(candidate);
            try (var input = file.getInputStream()) {
                Files.copy(input, candidate);
                return candidate;
            } catch (FileAlreadyExistsException exception) {
                // Try the next numeric suffix. Files.copy performs the existence check atomically.
            }
        }
    }

    private void ensureInsideUploadDirectory(Path candidate) {
        if (!candidate.startsWith(uploadDirectory)) {
            throw new InvalidUploadException("upload.error.invalidName");
        }
    }
}
