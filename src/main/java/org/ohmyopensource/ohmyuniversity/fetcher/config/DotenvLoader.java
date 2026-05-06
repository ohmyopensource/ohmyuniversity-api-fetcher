package org.ohmyopensource.ohmyuniversity.fetcher.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> envProperties = new HashMap<>();
        dotenv.entries().forEach(entry -> envProperties.put(entry.getKey(), entry.getValue()));

        if (!envProperties.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenvProperties", envProperties));
        }
    }
}