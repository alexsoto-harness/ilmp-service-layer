package com.spdji.ilmp.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Build identity for FR-008. On ECS the digest comes from the task metadata
 * endpoint, so /api/version reports what was actually pulled.
 */
@Component
public class VersionInfo {

    private static final Pattern IMAGE = Pattern.compile("\"Image\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern IMAGE_ID = Pattern.compile("\"ImageID\"\\s*:\\s*\"([^\"]*)\"");

    private final Map<String, String> env;
    private volatile String[] imageAndDigest;

    public VersionInfo() {
        this(System.getenv());
    }

    VersionInfo(Map<String, String> env) {
        this.env = env;
    }

    public Map<String, String> asMap() {
        String[] image = runningImage();
        Map<String, String> out = new LinkedHashMap<>();
        out.put("component", "service-layer");
        out.put("image", image[0]);
        out.put("image_digest", image[1]);
        out.put("image_tag", env.getOrDefault("IMAGE_TAG", "dev"));
        out.put("commit_sha", env.getOrDefault("GIT_SHA", "unknown"));
        out.put("build_number", env.getOrDefault("BUILD_NUMBER", "0"));
        out.put("environment", env.getOrDefault("ENVIRONMENT", "local"));
        return out;
    }

    String[] runningImage() {
        if (imageAndDigest == null) {
            imageAndDigest = resolve();
        }
        return imageAndDigest;
    }

    private String[] resolve() {
        String uri = env.get("ECS_CONTAINER_METADATA_URI_V4");
        if (uri != null && !uri.isBlank()) {
            try {
                String body = HttpClient.newHttpClient()
                        .send(HttpRequest.newBuilder(URI.create(uri)).timeout(Duration.ofSeconds(2)).build(),
                                HttpResponse.BodyHandlers.ofString())
                        .body();
                return parseMetadata(body);
            } catch (Exception e) {
                // fall through to env-provided values
            }
        }
        return new String[] {env.getOrDefault("IMAGE_REF", "local"), env.getOrDefault("IMAGE_DIGEST", "unknown")};
    }

    static String[] parseMetadata(String json) {
        String image = find(IMAGE, json);
        String digest = find(IMAGE_ID, json);
        int at = image.indexOf("@sha256:");
        if (at >= 0) {
            digest = image.substring(at + 1);
        }
        return new String[] {image, digest.isEmpty() ? "unknown" : digest};
    }

    private static String find(Pattern p, String json) {
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
}
