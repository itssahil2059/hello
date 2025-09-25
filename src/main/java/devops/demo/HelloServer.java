package devops.demo;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HelloServer {
    private static final String NAME  = "Sahil Bhusal";
    private static final String MAJOR = "Computer Science";

    public static void main(String[] args) throws Exception {
        int port = 8080; // change to 8081 if needed
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new RootHandler());
        server.createContext("/health", ex -> {
            byte[] ok = "OK".getBytes();
            ex.sendResponseHeaders(200, ok.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(ok); }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:" + port);
    }

    static class RootHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            String html = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <title>Hello, DevOps!</title>
                  <style>
                    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif;
                           display:flex; align-items:center; justify-content:center; min-height:100vh; background:#faf7ff; }
                    .card { background:#fff; padding:40px; border-radius:16px;
                            box-shadow:0 10px 30px rgba(0,0,0,.08); text-align:center; }
                    h1 { margin:0 0 8px; }
                    p { margin:8px 0; color:#555; }
                    small { color:#777; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>Hello, DevOps!</h1>
                    <p>Welcome to our first DevOps demo application.</p>
                    <p>This page is generated to verify the build and deployment pipeline.</p>
                    <small>© DevOps Demo | %s — %s</small>
                  </div>
                </body>
                </html>
            """.formatted(MAJOR, NAME);

            byte[] bytes = html.getBytes();
            ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        }
    }
}
