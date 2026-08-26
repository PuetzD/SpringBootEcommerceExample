package com.springbootecommerce.demo.shared.web;

public record SeoMetadata(
    String title,
    String description,
    String canonicalPath,
    String robots,
    String openGraphTitle,
    String openGraphDescription,
    String openGraphType) {

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
