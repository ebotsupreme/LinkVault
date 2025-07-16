package com.linkvault.util;

import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
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
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }
}
