package com.delivo.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StaticFileController {

    private static final String FRONTEND_DIR = "c:/Users/hp/Downloads/Food Delivery/frontend";

    @GetMapping(value = {"/", "/index.html", "/landing.html", "/login.html", "/customer-dashboard.html", "/admin-dashboard.html"})
    public ResponseEntity<String> getIndexHtml() throws IOException {
        Path indexPath = Paths.get(FRONTEND_DIR, "index.html");
        String content = Files.readString(indexPath, StandardCharsets.UTF_8);
        String compiled = processServerSideIncludes(content);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(compiled);
    }

    @GetMapping(value = {"/css/**", "/js/**", "/images/**", "/components/**", "/*.css", "/*.js", "/*.json", "/*.png", "/*.jpg", "/*.svg", "/*.ico", "/sw.js", "/manifest.json"})
    public ResponseEntity<Resource> getStaticResource(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        Path filePath = Paths.get(FRONTEND_DIR, path);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private String processServerSideIncludes(String html) throws IOException {
        Pattern pattern = Pattern.compile("<include-component\\s+src=\"([^\"]+)\"\\s*></include-component>");
        Matcher matcher = pattern.matcher(html);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String componentPath = matcher.group(1);
            Path path = Paths.get(FRONTEND_DIR, componentPath);
            if (Files.exists(path)) {
                String componentContent = Files.readString(path, StandardCharsets.UTF_8);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(processServerSideIncludes(componentContent)));
            } else {
                matcher.appendReplacement(sb, "<!-- Component missing: " + componentPath + " -->");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
