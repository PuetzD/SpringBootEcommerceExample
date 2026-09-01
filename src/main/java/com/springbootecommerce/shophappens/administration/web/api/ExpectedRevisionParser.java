package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;

public final class ExpectedRevisionParser {
    private ExpectedRevisionParser() {
    }

    public static long parse(String ifMatch) {
        if (ifMatch == null || !ifMatch.matches("\"(0|[1-9]\\d*)\"")) {
            throw new InvalidCatalogOperationException("If-Match must be a quoted revision");
        }
        return Long.parseLong(ifMatch.substring(1, ifMatch.length() - 1));
    }
}
