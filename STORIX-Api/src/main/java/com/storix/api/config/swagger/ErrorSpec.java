package com.storix.api.config.swagger;

import java.util.List;

public record ErrorSpec(String path, String security, List<Entry> own, List<Entry> common) {

    public record Entry(int status, String code, String message, String reason, boolean fieldErrors, String scope) {

        public boolean isAuth() {
            return "auth".equals(scope);
        }

        public boolean isGlobal() {
            return "global".equals(scope);
        }
    }

    public List<Entry> all() {
        return java.util.stream.Stream.concat(own.stream(), common.stream()).toList();
    }
}
