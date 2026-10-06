package qa.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.qameta.allure.Allure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thin wrapper over the Allure attachment API. */
public final class Attachments {

    private static final Logger log = LoggerFactory.getLogger(Attachments.class);

    private Attachments() {
    }

    public static void png(String name, byte[] bytes) {
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
    }

    public static void text(String name, String content) {
        Allure.addAttachment(name, "text/plain",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), "txt");
    }

    public static void json(String name, String content) {
        Allure.addAttachment(name, "application/json",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), "json");
    }

    public static void file(String name, String mimeType, Path path, String extension) {
        try (InputStream in = Files.newInputStream(path)) {
            Allure.addAttachment(name, mimeType, in, extension);
        } catch (IOException e) {
            log.warn("Cannot attach {}: {}", path, e.getMessage());
        }
    }
}
