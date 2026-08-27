package com.springbootecommerce.shophappens.storefront.domain;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CanonicalUrlFactory {

    private final URI publicOrigin;

    public CanonicalUrlFactory(@Value("${storefront.public-origin}") String publicOrigin) {
        this.publicOrigin = parseAndValidateOrigin(publicOrigin);
    }

    public String forPath(String canonicalPath) {
        validateCanonicalPath(canonicalPath);
        var base = publicOrigin.toString();
        return (base.endsWith("/") ? base.substring(0, base.length() - 1) : base) + canonicalPath;
    }

    private static void validateCanonicalPath(String canonicalPath) {
        if (canonicalPath == null
                || canonicalPath.isBlank()
                || !canonicalPath.startsWith("/")
                || canonicalPath.startsWith("//")) {
            throw new IllegalArgumentException("Canonical path must be root-relative.");
        }
    }

    private static URI parseAndValidateOrigin(String value) {
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        boolean http = "http".equalsIgnoreCase(scheme);
        boolean https = "https".equalsIgnoreCase(scheme);
        String path = uri.getPath();
        if ((!http && !https)
                || host == null
                || host.isBlank()
                || uri.getRawUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null
                || (path != null && !path.isBlank() && !"/".equals(path))) {
            throw new IllegalArgumentException(
                    "storefront.public-origin must be an HTTP(S) origin with a host "
                            + "and no user info, path, query, or fragment");
        }
        return uri;
    }
}
