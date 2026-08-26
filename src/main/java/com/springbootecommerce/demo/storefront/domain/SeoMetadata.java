package com.springbootecommerce.demo.storefront.domain;

public record SeoMetadata(String title, String description, String canonicalPath, String robots) {

  public SeoMetadata {
    validateCanonicalPath(canonicalPath);
  }

  static void validateCanonicalPath(String canonicalPath) {
    if (canonicalPath == null
        || canonicalPath.isBlank()
        || !canonicalPath.startsWith("/")
        || canonicalPath.startsWith("//")) {
      throw new IllegalArgumentException("Canonical path must be root-relative.");
    }
  }
}
