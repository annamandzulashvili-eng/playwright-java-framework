package qa.core.testng;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAlterSuiteListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

import qa.core.browser.BrowserManager;
import qa.core.config.FrameworkConfig;

/**
 * Suite-level wiring, registered in every suite XML:
 * <ul>
 *   <li>validates configuration before any test starts (fail fast, one clear error)</li>
 *   <li>applies {@code threads} from config over the XML thread-count when set</li>
 *   <li>writes Allure environment.properties and copies categories.json</li>
 *   <li>closes all browsers once the suite is done</li>
 * </ul>
 */
public class FrameworkSuiteListener implements IAlterSuiteListener, ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger(FrameworkSuiteListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        int threads = FrameworkConfig.get().threads();
        if (threads > 0) {
            suites.forEach(s -> s.setThreadCount(threads));
            log.info("Thread count overridden from config: {}", threads);
        }
    }

    @Override
    public void onStart(ISuite suite) {
        FrameworkConfig cfg = FrameworkConfig.get();
        Path results = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        try {
            Files.createDirectories(results);
            Properties env = new Properties();
            cfg.describe().forEach(env::setProperty);
            env.setProperty("Suite", suite.getName());
            try (Writer w = Files.newBufferedWriter(results.resolve("environment.properties"), StandardCharsets.UTF_8)) {
                env.store(w, null);
            }
            try (InputStream categories = getClass().getClassLoader().getResourceAsStream("allure/categories.json")) {
                if (categories != null) {
                    Files.copy(categories, results.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            log.warn("Cannot write Allure environment info: {}", e.getMessage());
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        BrowserManager.closeAll();
    }
}
