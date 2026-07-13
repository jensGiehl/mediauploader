package de.agiehl.mediauploader.web;

import de.agiehl.mediauploader.upload.UploadService;
import de.agiehl.mediauploader.upload.InvalidUploadException;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Controller
public class UploadController {

    private final UploadService uploadService;
    private final MessageSource messageSource;

    public UploadController(UploadService uploadService, MessageSource messageSource) {
        this.uploadService = uploadService;
        this.messageSource = messageSource;
    }

    @GetMapping("/")
    String index() {
        return "upload";
    }

    @PostMapping("/upload")
    @ResponseBody
    ResponseEntity<?> upload(@RequestParam("files") MultipartFile file, Locale locale) {
        try {
            List<String> stored = uploadService.store(List.of(file));
            return ResponseEntity.ok(Map.of(
                    "message", messageSource.getMessage("upload.completed.one", null, locale),
                    "files", stored));
        } catch (InvalidUploadException exception) {
            String message = messageSource.getMessage(exception.getMessageCode(), null, locale);
            return ResponseEntity.badRequest().body(Map.of("message", message));
        } catch (IOException exception) {
            String message = messageSource.getMessage("upload.error.storage", null, locale);
            return ResponseEntity.internalServerError().body(Map.of("message", message));
        }
    }
}
