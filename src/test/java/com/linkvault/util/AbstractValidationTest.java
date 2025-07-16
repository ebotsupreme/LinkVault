package com.linkvault.util;

import com.linkvault.constants.apiPaths.LinkEndpoints;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Setter
public class AbstractValidationTest {
    protected MockMvc mockMvc;

    protected ResultActions performJsonRequest(
        MockHttpServletRequestBuilder builder, String json
    ) throws Exception {
        return mockMvc.perform(builder
            .contentType(MediaType.APPLICATION_JSON)
            .content(json));
    }

    protected void assertValidationFailure(
        ResultActions result, String fieldName
    ) throws Exception {
        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString(fieldName))))
            .andExpect(jsonPath("$.message").value(
                "One or more fields are invalid")
            )
            .andExpect(jsonPath("$.status").value(400));
    }

    protected MockHttpServletRequestBuilder buildSimpleRequest(
        String method, String path
    ) {
        return switch (method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(path);
            default -> throw new IllegalArgumentException("Invalid method");
        };
    }
}
