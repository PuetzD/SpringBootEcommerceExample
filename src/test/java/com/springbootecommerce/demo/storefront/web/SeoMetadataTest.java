package com.springbootecommerce.demo.storefront.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.springbootecommerce.demo.storefront.domain.CanonicalUrlFactory;
import com.springbootecommerce.demo.storefront.domain.SeoMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SeoMetadataTest {

  private final CanonicalUrlFactory factory = new CanonicalUrlFactory();

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
  void createsAbsoluteCanonicalUrlFromRequestOriginAndPath() {
    var request = new MockHttpServletRequest("GET", "/products");
    request.setScheme("https");
    request.setServerName("shop.example");
    request.setServerPort(443);

    assertThat(factory.forRequest(request, "/")).isEqualTo("https://shop.example/");
  }

  @Test
  void includesContextPathAndNonDefaultPortInCanonicalUrl() {
    var request = new MockHttpServletRequest("GET", "/products");
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setContextPath("/shop");

    assertThat(factory.forRequest(request, "/products"))
        .isEqualTo("http://localhost:8080/shop/products");
  }

  private SeoMetadata metadata(String canonicalPath) {
    return new SeoMetadata("Title", "Description", canonicalPath, "index,follow");
  }
}
