import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Delivo Core Engine - Development & Prototype Server
 * Zero-dependency standalone server providing static files compilation (SSI),
 * OpenAI API Proxying with rate limiting, JWT-session emulation, and CSRF guards.
 */
public class Server {
    private static final int PORT = 8080;
    private static final String FRONTEND_DIR = "c:/Users/hp/Downloads/Food Delivery/frontend";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Session and Security Stores
    private static final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> ipRateLimits = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> featureFlags = new ConcurrentHashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();

    static {
        // Initialize Default Feature Flags
        featureFlags.put("surge", true);
        featureFlags.put("ai", true);
        featureFlags.put("search", true);
        featureFlags.put("biometric", true);
        featureFlags.put("geofence", true);
        featureFlags.put("jwt", true);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Core Static and API Routes
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/chat", new OpenAiProxyHandler());
        server.createContext("/api/auth/", new AuthHandler());
        server.createContext("/api/admin/", new AdminHandler());
        server.createContext("/api/flags", new FlagsHandler());
        server.createContext("/api/orders/", new OrdersHandler());

        server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        System.out.println("==================================================");
        System.out.println(" Delivo Core Engine Local Dev Server Started");
        System.out.println(" Port: " + PORT);
        System.out.println(" Static Folder: " + FRONTEND_DIR);
        System.out.println(" URL: http://127.0.0.1:" + PORT + "/");
        System.out.println("==================================================");
        server.start();
    }

    // ==========================================================================
    // HANDLER: STATIC FILES & SERVER-SIDE INCLUDES (SSI)
    // ==========================================================================
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String pathStr = exchange.getRequestURI().getPath();
                if (pathStr.equals("/")) {
                    pathStr = "/index.html";
                }

                // Security Check: Block directory traversal
                if (pathStr.contains("..")) {
                    sendError(exchange, 400, "Bad Request: Directory traversal blocked.");
                    return;
                }

                Path filePath = Paths.get(FRONTEND_DIR, pathStr);
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    // Fallback to index.html for SPA router (Ticket #14 offline / 404 fallback support)
                    filePath = Paths.get(FRONTEND_DIR, "index.html");
                    if (!Files.exists(filePath)) {
                        sendError(exchange, 404, "File Not Found: index.html missing.");
                        return;
                    }
                }

                byte[] fileBytes;
                String mimeType = getMimeType(filePath.toString());

                // Perform Server-Side Includes (SSI) compilation for HTML files
                if (mimeType.equals("text/html")) {
                    String rawContent = Files.readString(filePath, StandardCharsets.UTF_8);
                    String compiledContent = processServerSideIncludes(rawContent);
                    fileBytes = compiledContent.getBytes(StandardCharsets.UTF_8);
                } else {
                    fileBytes = Files.readAllBytes(filePath);
                }

                // Add Security Headers (Ticket #7 & #8 compliance)
                exchange.getResponseHeaders().set("Content-Security-Policy",
                        "default-src 'self' https://unpkg.com https://cdnjs.cloudflare.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://unpkg.com https://cdnjs.cloudflare.com; " +
                        "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                        "img-src 'self' data: https://*.tile.openstreetmap.org https://unpkg.com https://images.unsplash.com https://*.google.com; " +
                        "connect-src 'self' https://api.openai.com https://nominatim.openstreetmap.org; " +
                        "script-src 'self' 'unsafe-inline' https://unpkg.com https://cdn.jsdelivr.net;");
                exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
                exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
                exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer-when-downgrade");

                // Set MIME Type
                exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=utf-8");

                // CORS headers for static resource caching
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

                exchange.sendResponseHeaders(200, fileBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }

                logRequest(exchange, 200);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }

        private String processServerSideIncludes(String html) throws IOException {
            // Match tag patterns like: <include-component src="components/navbar.html"></include-component>
            Pattern pattern = Pattern.compile("<include-component\\s+src=\"([^\"]+)\"\\s*></include-component>");
            Matcher matcher = pattern.matcher(html);
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                String componentPath = matcher.group(1);
                Path path = Paths.get(FRONTEND_DIR, componentPath);
                if (Files.exists(path)) {
                    String componentContent = Files.readString(path, StandardCharsets.UTF_8);
                    // Recursively compile nested components
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(processServerSideIncludes(componentContent)));
                } else {
                    matcher.appendReplacement(sb, "<!-- Component missing: " + componentPath + " -->");
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        private String getMimeType(String path) {
            String lower = path.toLowerCase();
            if (lower.endsWith(".html")) return "text/html";
            if (lower.endsWith(".css")) return "text/css";
            if (lower.endsWith(".js")) return "application/javascript";
            if (lower.endsWith(".json")) return "application/json";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".webp")) return "image/webp";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }

    // ==========================================================================
    // HANDLER: OPENAI API SECURE PROXY (TICKET #5 & #8 COMPLIANCE)
    // ==========================================================================
    private static class OpenAiProxyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            // 1. IP-Based Rate Limiting Guard
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            if (isRateLimited(clientIp)) {
                sendError(exchange, 429, "Too Many Requests: Rate limit exceeded (Max 10 calls/min).");
                return;
            }

            // 2. Validate CSRF Token
            if (featureFlags.get("jwt") && !validateCsrfToken(exchange)) {
                sendError(exchange, 403, "Forbidden: Invalid CSRF Token.");
                return;
            }

            // 3. Read OpenAI API Key from Env Variable (NO hardcoding!)
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                sendError(exchange, 503, "Service Unavailable: OpenAI API Key missing on backend server configuration.");
                return;
            }

            try {
                // 4. Sanitize User Prompt Inputs (XSS prevention)
                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                String sanitizedBody = sanitizeInput(requestBody);

                // 5. Send API Request to OpenAI v1/chat/completions
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(sanitizedBody, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                // Set headers and forward response
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = response.body().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

                logRequest(exchange, response.statusCode());
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Gateway Connection Error: " + e.getMessage());
            }
        }

        private boolean isRateLimited(String ip) {
            long now = System.currentTimeMillis();
            List<Long> timestamps = ipRateLimits.computeIfAbsent(ip, k -> new ArrayList<>());
            
            // Retain only requests made in the last 60 seconds
            timestamps.removeIf(t -> now - t > 60000);
            
            if (timestamps.size() >= 10) {
                return true;
            }
            
            timestamps.add(now);
            return false;
        }
    }

    // ==========================================================================
    // HANDLER: AUTHENTICATION & SESSIONS (TICKET #36 COMPLIANCE)
    // ==========================================================================
    private static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (path.endsWith("/login") && exchange.getRequestMethod().equals("POST")) {
                handleLogin(exchange);
            } else if (path.endsWith("/logout") && exchange.getRequestMethod().equals("POST")) {
                handleLogout(exchange);
            } else {
                sendError(exchange, 404, "Endpoint not found");
            }
        }

        private void handleLogin(HttpExchange exchange) throws IOException {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String email = "";
                String password = "";

                Pattern emailPattern = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");
                Matcher emailMatcher = emailPattern.matcher(body);
                if (emailMatcher.find()) {
                    email = emailMatcher.group(1);
                }

                Pattern passPattern = Pattern.compile("\"password\"\\s*:\\s*\"([^\"]+)\"");
                Matcher passMatcher = passPattern.matcher(body);
                if (passMatcher.find()) {
                    password = passMatcher.group(1);
                }

                String role = "";
                boolean valid = false;

                if ("admin@delivo.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
                    role = "ADMIN";
                    valid = true;
                } else if ("customer@delivo.com".equalsIgnoreCase(email) && "customer123".equals(password)) {
                    role = "CUSTOMER";
                    valid = true;
                } else if ("driver@delivo.com".equalsIgnoreCase(email) && "driver123".equals(password)) {
                    role = "DRIVER";
                    valid = true;
                } else if ("kitchen@delivo.com".equalsIgnoreCase(email) && "kitchen123".equals(password)) {
                    role = "KITCHEN";
                    valid = true;
                }

                if (!valid) {
                    sendError(exchange, 401, "Invalid email or password");
                    return;
                }

                // Generate Auth Session and CSRF Token
                String sessionId = UUID.randomUUID().toString();
                String csrfToken = UUID.randomUUID().toString();
                activeSessions.put(sessionId, role + ":" + email);

                // Set Cookies
                exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + sessionId + "; Path=/; HttpOnly; SameSite=Strict");
                exchange.getResponseHeaders().add("Set-Cookie", "XSRF-TOKEN=" + csrfToken + "; Path=/; SameSite=Strict");

                String jsonResponse = String.format("{\"success\":true,\"email\":\"%s\",\"role\":\"%s\",\"csrfToken\":\"%s\"}", email, role, csrfToken);
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                logRequest(exchange, 200);
            } catch (Exception e) {
                sendError(exchange, 500, "Auth Error: " + e.getMessage());
            }
        }

        private void handleLogout(HttpExchange exchange) throws IOException {
            // Revoke session cookie
            exchange.getResponseHeaders().add("Set-Cookie", "session_id=; Path=/; HttpOnly; Max-Age=0");
            exchange.getResponseHeaders().add("Set-Cookie", "XSRF-TOKEN=; Path=/; Max-Age=0");

            String jsonResponse = "{\"success\":true}";
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            logRequest(exchange, 200);
        }
    }

    // ==========================================================================
    // HANDLER: TELEMETRY METRICS & LOGS (TICKET #6 & #10 COMPLIANCE)
    // ==========================================================================
    private static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Guard role access permissions
            String role = getSessionRole(exchange);
            if (featureFlags.get("jwt") && !role.equals("ADMIN")) {
                sendError(exchange, 403, "Access Denied: Admin role authorization required.");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/metrics")) {
                sendMetrics(exchange);
            } else {
                sendError(exchange, 404, "Not Found");
            }
        }

        private void sendMetrics(HttpExchange exchange) throws IOException {
            Random rand = new Random();
            int throughput = 100 + rand.nextInt(20);
            double sagaRate = 99.9 + (rand.nextDouble() * 0.09);
            
            String json = String.format(
                "{\"throughput\":\"%dK events/s\",\"regions\":\"3 Regions\",\"sagaSuccess\":\"%.2f%%\",\"rateLimitBlocks\":\"%d Blocks/m\"}",
                throughput, sagaRate, rand.nextInt(6)
            );
            
            byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            logRequest(exchange, 200);
        }
    }

    // ==========================================================================
    // HANDLER: FEATURE FLAGS PERSISTENCE
    // ==========================================================================
    private static class FlagsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                StringBuilder sb = new StringBuilder("{");
                featureFlags.forEach((k, v) -> sb.append(String.format("\"%s\":%b,", k, v)));
                if (sb.length() > 1) sb.setLength(sb.length() - 1);
                sb.append("}");

                byte[] responseBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else if (exchange.getRequestMethod().equals("POST")) {
                // Parse simple json toggle requests
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String flagName = body.split("\"flag\":\"")[1].split("\"")[0];
                boolean currentVal = featureFlags.getOrDefault(flagName, true);
                featureFlags.put(flagName, !currentVal);

                String reply = String.format("{\"success\":true,\"flag\":\"%s\",\"value\":%b}", flagName, !currentVal);
                byte[] responseBytes = reply.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        }
    }

    // ==========================================================================
    // GENERAL SECURITY & LOGGING HELPER FUNCTIONS
    // ==========================================================================
    private static String getSessionRole(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies == null) return "GUEST";
        
        for (String cookie : cookies.split(";")) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && parts[0].equals("session_id")) {
                String sessionData = activeSessions.get(parts[1]);
                if (sessionData != null) {
                    return sessionData.split(":")[0];
                }
            }
        }
        return "GUEST";
    }

    private static boolean validateCsrfToken(HttpExchange exchange) {
        String csrfHeader = exchange.getRequestHeaders().getFirst("X-XSRF-TOKEN");
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (csrfHeader == null || cookies == null) return false;

        String expectedToken = null;
        for (String cookie : cookies.split(";")) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && parts[0].equals("XSRF-TOKEN")) {
                expectedToken = parts[1];
            }
        }
        return csrfHeader.equals(expectedToken);
    }

    private static String sanitizeInput(String raw) {
        if (raw == null) return "";
        // Sanitize scripts injection and tags from query parameters
        return raw.replaceAll("(?i)<script.*?>.*?</script.*?>", "")
                  .replaceAll("(?i)javascript:", "")
                  .replaceAll("['\"`#$&<>^\\\\]", "");
    }

    private static void sendError(HttpExchange exchange, int code, String msg) throws IOException {
        String json = String.format("{\"error\":true,\"code\":%d,\"message\":\"%s\"}", code, msg);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        logRequest(exchange, code);
    }

    private static void logRequest(HttpExchange exchange, int status) {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.printf("[%s] %s %s - %d (Client: %s)\n",
                date,
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                status,
                exchange.getRemoteAddress().getAddress().getHostAddress()
        );
    }

    private static final Map<String, Order> ordersDb = new ConcurrentHashMap<>();

    public static class Order {
        public String orderId;
        public String paymentId;
        public String transactionId;
        public String restaurantName;
        public double amount;
        public String eta;
        public String address;
        public String paymentMethod;
        public String currentStatus;
        public String items;
        public long createdAt;

        public Order(String orderId, String paymentId, String transactionId, String restaurantName,
                     double amount, String eta, String address, String paymentMethod, String currentStatus, String items) {
            this.orderId = orderId;
            this.paymentId = paymentId;
            this.transactionId = transactionId;
            this.restaurantName = restaurantName;
            this.amount = amount;
            this.eta = eta;
            this.address = address;
            this.paymentMethod = paymentMethod;
            this.currentStatus = currentStatus;
            this.items = items;
            this.createdAt = System.currentTimeMillis();
        }

        public void updateStatus() {
            long age = System.currentTimeMillis() - this.createdAt;
            if (age >= 40000) {
                this.currentStatus = "DELIVERED";
            } else if (age >= 35000) {
                this.currentStatus = "OUT_FOR_DELIVERY";
            } else if (age >= 30000) {
                this.currentStatus = "PICKED_UP";
            } else if (age >= 25000) {
                this.currentStatus = "DRIVER_ASSIGNED";
            } else if (age >= 20000) {
                this.currentStatus = "READY_FOR_PICKUP";
            } else if (age >= 15000) {
                this.currentStatus = "PREPARING";
            } else if (age >= 10000) {
                this.currentStatus = "RESTAURANT_ACCEPTED";
            } else if (age >= 5000) {
                this.currentStatus = "ORDER_CONFIRMED";
            } else {
                this.currentStatus = "PAYMENT_SUCCESS";
            }
        }
        
        public String toJson() {
            return String.format(
                Locale.US,
                "{\"orderId\":\"%s\",\"paymentId\":\"%s\",\"transactionId\":\"%s\",\"restaurantName\":\"%s\"," +
                "\"amount\":%.2f,\"eta\":\"%s\",\"address\":\"%s\",\"paymentMethod\":\"%s\",\"currentStatus\":\"%s\",\"items\":\"%s\"}",
                orderId, paymentId, transactionId, restaurantName, amount, eta, address.replace("\"", "\\\""), paymentMethod, currentStatus, items.replace("\"", "\\\"")
            );
        }
    }

    private static class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (method.equals("OPTIONS")) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Set CORS Headers for Dev Ease
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

            try {
                if (path.equals("/api/orders/pay") && method.equals("POST")) {
                    handlePay(exchange);
                } else if (path.startsWith("/api/orders/")) {
                    String subPath = path.substring("/api/orders/".length());
                    if (subPath.endsWith("/status")) {
                        String orderId = subPath.substring(0, subPath.length() - "/status".length());
                        handleGetStatus(exchange, orderId);
                    } else {
                        handleGetOrder(exchange, subPath);
                    }
                } else {
                    sendError(exchange, 404, "Endpoint not found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Orders Error: " + e.getMessage());
            }
        }

        private void handlePay(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String restaurantName = "Bella Napoli Pizza";
            double amount = 0.00;
            String address = "IT Tech Park, Bandra East, Mumbai";
            String paymentMethod = "CARD";
            String items = "";

            Pattern rPattern = Pattern.compile("\"restaurantName\"\\s*:\\s*\"([^\"]+)\"");
            Matcher rMatcher = rPattern.matcher(body);
            if (rMatcher.find()) restaurantName = rMatcher.group(1);

            Pattern amtPattern = Pattern.compile("\"amount\"\\s*:\\s*([0-9.]+)");
            Matcher amtMatcher = amtPattern.matcher(body);
            if (amtMatcher.find()) amount = Double.parseDouble(amtMatcher.group(1));

            Pattern addrPattern = Pattern.compile("\"address\"\\s*:\\s*\"([^\"]+)\"");
            Matcher addrMatcher = addrPattern.matcher(body);
            if (addrMatcher.find()) address = addrMatcher.group(1);

            Pattern pmPattern = Pattern.compile("\"paymentMethod\"\\s*:\\s*\"([^\"]+)\"");
            Matcher pmMatcher = pmPattern.matcher(body);
            if (pmMatcher.find()) paymentMethod = pmMatcher.group(1);

            Pattern itemsPattern = Pattern.compile("\"items\"\\s*:\\s*\"([^\"]+)\"");
            Matcher itemsMatcher = itemsPattern.matcher(body);
            if (itemsMatcher.find()) items = itemsMatcher.group(1);

            String orderId = "ord_" + (100000 + secureRandom.nextInt(900000));
            String paymentId = "pay_" + (100000 + secureRandom.nextInt(900000));
            String transactionId = "tx_" + (100000 + secureRandom.nextInt(900000));

            Order newOrder = new Order(orderId, paymentId, transactionId, restaurantName, amount, "25 min", address, paymentMethod, "PAYMENT_SUCCESS", items);
            ordersDb.put(orderId, newOrder);

            String jsonResponse = newOrder.toJson();
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            logRequest(exchange, 200);
        }

        private void handleGetOrder(HttpExchange exchange, String orderId) throws IOException {
            Order order = ordersDb.get(orderId);
            if (order == null) {
                sendError(exchange, 404, "Order not found");
                return;
            }
            order.updateStatus();

            String jsonResponse = order.toJson();
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            logRequest(exchange, 200);
        }

        private void handleGetStatus(HttpExchange exchange, String orderId) throws IOException {
            Order order = ordersDb.get(orderId);
            if (order == null) {
                sendError(exchange, 404, "Order not found");
                return;
            }
            order.updateStatus();

            String jsonResponse = String.format("{\"status\":\"%s\"}", order.currentStatus);
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            logRequest(exchange, 200);
        }
    }
}
