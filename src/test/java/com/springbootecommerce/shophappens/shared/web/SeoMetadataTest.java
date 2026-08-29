package com.springbootecommerce.shophappens.shared.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SeoMetadataTest {

    private final CanonicalUrlFactory factory = new CanonicalUrlFactory("https://shop.example");

    @Test
    void rejectsCanonicalPathsOutsideThisSite() {
        assertThatThrownBy(() -> metadata("https://other.example/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> metadata("//other.example/"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingOrNonRootRelativeCanonicalPaths() {
        assertThatThrownBy(() -> metadata(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> metadata("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> metadata(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> metadata("products")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildsCanonicalUrlFromConfiguredOriginAndPath() {
        assertThat(factory.forPath("/catalog")).isEqualTo("https://shop.example/catalog");
        assertThat(factory.forPath("/")).isEqualTo("https://shop.example/");
    }

    @Test
    void trimsTrailingSlashFromConfiguredOrigin() {
        assertThat(new CanonicalUrlFactory("https://shop.example/").forPath("/catalog"))
                .isEqualTo("https://shop.example/catalog");
    }

    @Test
    void ignoresForgedRequestHostBecauseOriginIsConfigured() {
        var seo = new SeoMetadata("Title", "Description", "/catalog", "index,follow");
        assertThat(factory.forPath(seo.canonicalPath())).isEqualTo("https://shop.example/catalog");
    }

    @Test
    void rejectsInvalidConfiguredOrigins() {
        assertThatThrownBy(() -> new CanonicalUrlFactory("shop.example"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CanonicalUrlFactory("https://shop.example/catalog"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CanonicalUrlFactory("https://user:pw@shop.example"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CanonicalUrlFactory("https://shop.example?q=1"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CanonicalUrlFactory("https://shop.example#frag"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CanonicalUrlFactory("ftp://shop.example"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CanonicalUrlFactory("https://"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private SeoMetadata metadata(String canonicalPath) {
        return new SeoMetadata("Title", "Description", canonicalPath, "index,follow");
    }
}
