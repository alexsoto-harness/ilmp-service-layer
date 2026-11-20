package com.spdji.ilmp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthIsUp() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void versionReportsComponent() throws Exception {
        mvc.perform(get("/api/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.component").value("service-layer"))
                .andExpect(jsonPath("$.image_digest").exists());
    }

    @Test
    void indicesListsThree() throws Exception {
        mvc.perform(get("/api/indices")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void indexById() throws Exception {
        mvc.perform(get("/api/indices/SPDJI-DEMO-50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.constituents").value(50));
    }

    @Test
    void unknownIndexIs404() throws Exception {
        mvc.perform(get("/api/indices/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void versionUsesEnvWhenNotOnEcs() {
        VersionInfo info = new VersionInfo(Map.of("IMAGE_DIGEST", "sha256:feed", "IMAGE_TAG", "RC-service-layer-7-abc1234"));
        assertThat(info.asMap()).containsEntry("image_digest", "sha256:feed")
                .containsEntry("image_tag", "RC-service-layer-7-abc1234");
    }

    @Test
    void digestParsedFromImageReference() {
        String[] parsed = VersionInfo.parseMetadata("{\"Image\":\"repo/service-layer@sha256:abc\",\"ImageID\":\"sha256:other\"}",
                "{\"Family\":\"ilmp-service-layer-dev\",\"Revision\":\"4\"}");
        assertThat(parsed[1]).isEqualTo("sha256:abc");
        assertThat(parsed[2]).isEqualTo("ilmp-service-layer-dev:4");
    }

    @Test
    void digestFallsBackToImageId() {
        String[] parsed = VersionInfo.parseMetadata("{\"Image\":\"repo/service-layer:tag\",\"ImageID\":\"sha256:def\"}", "{}");
        assertThat(parsed[1]).isEqualTo("sha256:def");
    }
}
