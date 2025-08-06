package com.linkvault.unit.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class JsonBuilder {
    private final Map<String, Object> fields = new HashMap<>();

    public JsonBuilder withUserId(Object userId) {
        fields.put("userId", userId);
        return this;
    }

    public JsonBuilder withUrl(Object url) {
        fields.put("url", url);
        return this;
    }

    public JsonBuilder withTitle(String title) {
        fields.put("title", title);
        return this;
    }

    public JsonBuilder withDescription(String description) {
        fields.put("description", description);
        return this;
    }

    public JsonBuilder withoutField(String fieldName) {
        fields.remove(fieldName);
        return this;
    }

    public String build() throws Exception {
        try {
            return new ObjectMapper().writeValueAsString(fields);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to build JSON string", e);
        }
    }
}
