package com.spdji.ilmp.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {

    public record IndexSummary(String id, String name, String currency, int constituents) {}

    static final List<IndexSummary> INDICES = List.of(
            new IndexSummary("SPDJI-DEMO-50", "ILMP Demo 50", "USD", 50),
            new IndexSummary("SPDJI-DEMO-ESG", "ILMP Demo ESG Leaders", "USD", 120),
            new IndexSummary("SPDJI-DEMO-DIV", "ILMP Demo Dividend Aristocrats", "USD", 66));

    private final VersionInfo versionInfo;

    public ApiController(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        return versionInfo.asMap();
    }

    @GetMapping("/indices")
    public List<IndexSummary> indices() {
        return INDICES;
    }

    @GetMapping("/indices/{id}")
    public IndexSummary index(@PathVariable String id) {
        return INDICES.stream()
                .filter(i -> i.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "index not found"));
    }
}
