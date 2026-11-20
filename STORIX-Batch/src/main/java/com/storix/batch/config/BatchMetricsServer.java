package com.storix.batch.config;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchMetricsServer implements SmartLifecycle {

    public static final int PORT = 9101;
    public static final String PATH = "/actuator/prometheus";
    private static final String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";

    private final PrometheusMeterRegistry registry;
    private HttpServer server;

    @Override
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext(PATH, this::scrape);
            server.start();
            log.info(">>> [BatchMetricsServer] 지표 서버 시작 port={}", PORT);
        } catch (IOException e) {
            // 지표 실패와 배치 실행 분리
            server = null;
            log.error(">>> [BatchMetricsServer] 지표 서버 시작 실패 port={}", PORT, e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Override
    public boolean isRunning() {
        return server != null;
    }

    private void scrape(HttpExchange exchange) throws IOException {
        byte[] body = registry.scrape().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
