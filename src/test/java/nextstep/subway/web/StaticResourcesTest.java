package nextstep.subway.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.TimeUnit;

import static nextstep.subway.config.WebMvcConfig.PREFIX_STATIC_RESOURCES;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StaticResourcesTest {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourcesTest.class);

    @Autowired
    private WebTestClient client;

    @DisplayName("CSS : max-age = 1 year")
    @Test
    void get_static_resources_css() {
        String uri = PREFIX_STATIC_RESOURCES + "/static/css/main.css";
        EntityExchangeResult<String> response = client
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .cacheControl(CacheControl.maxAge(60 * 60 * 24 * 365, TimeUnit.SECONDS))
                .expectBody(String.class)
                .returnResult();

        logger.debug("body : {}", response.getResponseBody());

        String etag = response.getResponseHeaders()
                .getETag();

        client.get()
                .uri(uri)
                .header("If-None-Match", etag)
                .exchange()
                .expectStatus()
                .isNotModified();
    }

    @DisplayName("js : No cache")
    @Test
    void get_static_resources_js() {
        String uri = PREFIX_STATIC_RESOURCES + "/static/js/main.js";
        EntityExchangeResult<String> response = client
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .cacheControl(CacheControl.noCache().cachePrivate())
                .expectBody(String.class)
                .returnResult();

        logger.debug("body : {}", response.getResponseBody());

        String etag = response.getResponseHeaders()
                .getETag();

        client.get()
                .uri(uri)
                .header("If-None-Match", etag)
                .exchange()
                .expectStatus()
                .isNotModified();
    }
}