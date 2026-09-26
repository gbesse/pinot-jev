package org.apache.pinot.function.jev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pinot.spi.annotations.ScalarFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JevClientTest {
  private HttpServer server;
  private final AtomicInteger calls = new AtomicInteger();
  private final ObjectMapper json = new ObjectMapper();

  @BeforeEach
  void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/systemone", exchange -> {
      try {
        calls.incrementAndGet();
        assertEquals("Bearer test-key", exchange.getRequestHeaders().getFirst("Authorization"));
        JsonNode body = json.readTree(exchange.getRequestBody());
        assertEquals("jev-1.13.0", body.path("model").asText());
        assertEquals("The ending was good. Watch it!", body.path("state").path("value").asText());
        assertEquals(2, body.path("questions").size());
        byte[] response = ("{\"answers\":{\"q0\":{\"type\":\"noul\",\"noul\":0.91},"
            + "\"q1\":{\"type\":\"noul\",\"noul\":0.84}}}")
            .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
      } finally {
        exchange.close();
      }
    });
    server.start();
  }

  @AfterEach
  void stop() { server.stop(0); }

  @Test
  void fusesTwoQuestionsAndCachesVerdicts() {
    JevClient client = client(1);
    List<String> conditions = Arrays.asList("discusses the ending", "recommends the movie");
    assertArrayEquals(new double[]{0.91, 0.84},
        client.evaluate("The ending was good. Watch it!", conditions));
    assertArrayEquals(new double[]{0.91, 0.84},
        client.evaluate("The ending was good. Watch it!", conditions));
    assertEquals(1, calls.get());
    assertThrows(IllegalStateException.class,
        () -> client.evaluate("A different review", conditions));
  }

  @Test
  void validatesConditionsAndProtocol() {
    assertEquals(2, JevClient.conditions("[\"a\",\"b\"]").size());
    assertThrows(IllegalArgumentException.class, () -> JevClient.conditions("[]"));
    assertThrows(IllegalArgumentException.class, () -> JevClient.conditions("[1]"));
    assertThrows(IllegalArgumentException.class, () -> JevClient.conditions("not JSON"));
    assertThrows(IllegalArgumentException.class,
        () -> new JevClient("test-key", "http://example.com", "jev-1.13.0", 10000, 1));
    assertThrows(IllegalArgumentException.class,
        () -> client(2).evaluate("", Arrays.asList("a")));
  }

  @Test
  void coalescesConcurrentIdenticalRequests() throws Exception {
    JevClient client = client(2);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Future<double[]> a = pool.submit(() -> {
        start.await();
        return client.evaluate("The ending was good. Watch it!", Arrays.asList("a", "b"));
      });
      Future<double[]> b = pool.submit(() -> {
        start.await();
        return client.evaluate("The ending was good. Watch it!", Arrays.asList("a", "b"));
      });
      start.countDown();
      assertArrayEquals(a.get(), b.get());
      assertEquals(1, calls.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void pinotFunctionsAreDiscoverable() throws Exception {
    assertNotNull(JevFunctions.class.getMethod("jevAll", String.class, String.class)
        .getAnnotation(ScalarFunction.class));
    assertNotNull(JevFunctions.class.getMethod("jevAny", String.class, String.class)
        .getAnnotation(ScalarFunction.class));
    assertNotNull(JevFunctions.class.getMethod("jevProbability", String.class, String.class)
        .getAnnotation(ScalarFunction.class));
    assertEquals(0, JevFunctions.jevAll(null, "[]"));
  }

  private JevClient client(int maxRequests) {
    return new JevClient("test-key", "http://127.0.0.1:" + server.getAddress().getPort()
        + "/v1/systemone", "jev-1.13.0", 10000, maxRequests);
  }
}
