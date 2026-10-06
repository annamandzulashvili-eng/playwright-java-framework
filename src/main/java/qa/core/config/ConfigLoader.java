package qa.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds {@link FrameworkConfig} from layered sources.
 * <p>
 * Precedence (highest first), identical for every key:
 * <ol>
 *   <li>JVM system property, e.g. {@code -Dbrowser=firefox}</li>
 *   <li>Environment variable, key upper-cased with dots as underscores, e.g. {@code BROWSER}</li>
 *   <li>{@code config/env-<env>.properties} on the classpath</li>
 *   <li>{@code config/default.properties} on the classpath</li>
 * </ol>
 * The environment name itself comes from {@code -Denv}, {@code ENV}, or the default file.
 */
public final class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String DEFAULTS = "config/default.properties";

    private ConfigLoader() {
    }

    public static FrameworkConfig load() {
        Properties files = new Properties();
        read(DEFAULTS, files);

        String env = firstNonBlank(System.getProperty("env"), System.getenv("ENV"), files.getProperty("env"));
        if (env == null) {
            throw new ConfigException(List.of("'env' is not set in " + DEFAULTS));
        }
        read("config/env-" + env + ".properties", files);

        Reader r = new Reader(files);
        FrameworkConfig cfg = new FrameworkConfig(
                env,
                r.url("base.url"),
                r.url("api.url"),
                r.enumValue("browser", BrowserName.class),
                r.bool("headless"),
                r.integer("slowmo.ms", 0, 5_000),
                r.enumValue("device", DeviceProfile.class),
                r.string("testid.attribute"),
                r.integer("timeout.default.ms", 1_000, 120_000),
                r.integer("timeout.navigation.ms", 1_000, 180_000),
                r.integer("timeout.assertion.ms", 1_000, 60_000),
                r.enumValue("trace", ArtifactMode.class),
                r.enumValue("video", ArtifactMode.class),
                r.bool("screenshot.on.failure"),
                r.bool("block.third.party"),
                r.integer("api.timeout.ms", 1_000, 120_000),
                r.integer("threads", 0, 32),
                r.integer("retry.max", 0, 3),
                r.integer("overload.retries", 0, 5),
                Path.of(r.string("artifacts.dir")));

        r.throwIfInvalid();
        log.info("Loaded config: env={}, browser={}, device={}, headless={}, baseUrl={}",
                cfg.env(), cfg.browser(), cfg.device(), cfg.headless(), cfg.baseUrl());
        return cfg;
    }

    private static void read(String resource, Properties target) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new ConfigException(List.of("Config file not found on classpath: " + resource));
            }
            target.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ConfigException("Cannot read " + resource, e);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    /** Reads typed values and collects every validation problem instead of failing on the first. */
    private static final class Reader {
        private final Properties files;
        private final List<String> problems = new ArrayList<>();

        Reader(Properties files) {
            this.files = files;
        }

        String raw(String key) {
            String envVar = key.toUpperCase(Locale.ROOT).replace('.', '_');
            return firstNonBlank(System.getProperty(key), System.getenv(envVar), files.getProperty(key));
        }

        String string(String key) {
            String v = raw(key);
            if (v == null) {
                problems.add("'" + key + "' is required");
                return "";
            }
            return v;
        }

        String url(String key) {
            String v = string(key);
            if (v.isEmpty()) {
                return v;
            }
            try {
                URI uri = URI.create(v);
                if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                    problems.add("'" + key + "' must be an http(s) URL, got: " + v);
                }
            } catch (IllegalArgumentException e) {
                problems.add("'" + key + "' is not a valid URL: " + v);
            }
            return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
        }

        boolean bool(String key) {
            String v = string(key);
            if (!v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("false")) {
                if (!v.isEmpty()) {
                    problems.add("'" + key + "' must be true or false, got: " + v);
                }
                return false;
            }
            return Boolean.parseBoolean(v);
        }

        int integer(String key, int min, int max) {
            String v = string(key);
            if (v.isEmpty()) {
                return min;
            }
            try {
                int n = Integer.parseInt(v);
                if (n < min || n > max) {
                    problems.add("'" + key + "' must be in [" + min + ", " + max + "], got: " + n);
                }
                return n;
            } catch (NumberFormatException e) {
                problems.add("'" + key + "' must be an integer, got: " + v);
                return min;
            }
        }

        <E extends Enum<E>> E enumValue(String key, Class<E> type) {
            String v = string(key);
            E[] all = type.getEnumConstants();
            if (v.isEmpty()) {
                return all[0];
            }
            String normalized = v.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            for (E e : all) {
                if (e.name().equals(normalized)) {
                    return e;
                }
            }
            problems.add("'" + key + "' must be one of " + Arrays.toString(all).toLowerCase(Locale.ROOT) + ", got: " + v);
            return all[0];
        }

        void throwIfInvalid() {
            if (!problems.isEmpty()) {
                throw new ConfigException(problems);
            }
        }
    }
}
