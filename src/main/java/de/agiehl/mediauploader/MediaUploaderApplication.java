package de.agiehl.mediauploader;

import de.agiehl.mediauploader.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MediaUploaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaUploaderApplication.class, args);
    }
}
