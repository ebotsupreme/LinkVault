package com.linkvault.integration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.AuthEndpoints;
import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.unit.util.TestConstants;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IntegrationTestFactory {

    public static String createJsonForUser(String username, String password) {
        return  String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, password);
    }

    public static String createJsonForLink(String url, String title, String description) {
        return  String.format("""
            {
                "url": "%s",
                "title": "%s",
                "description": "%s"
            }
            """, url, title, description);
    }

    public static void performJsonRegisterUserRequest(
        MockMvc mockMvc,
        String json
    ) throws Exception {
        mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    public static MvcResult performJsonUserLoginRequest(
        MockMvc mockMvc,
        String json
    ) throws Exception {
        return mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();
    }

    public static String getUserTokenFromJsonResponse(
        MvcResult result,
        ObjectMapper mapper
    ) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(responseBody);
        return jsonNode.get("token").asText();
    }

    public static ResultActions performJsonCreateLinkRequest(
        MockMvc mockMvc,
        String token,
        String json
    ) throws Exception {
        return mockMvc.perform(post(LinkEndpoints.BASE_LINKS)
                    .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isCreated());
    }

    public static MvcResult assertLinkCreateValidationSuccess(
        ResultActions result,
        String url,
        String title
    ) throws Exception {
        return result
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.url").value(url))
            .andExpect(jsonPath("$.title").value(title))
            .andReturn();
    }

    public static void performUnauthorizedJsonRequest(
        MockMvc mockMvc, HttpMethod method, String linkEndpoint, String token
    ) throws Exception {
        mockMvc.perform(request(method, linkEndpoint)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isUnauthorized());
    }

    public static void performUnauthorizedJsonRequestWithoutHeader(
        MockMvc mockMvc, HttpMethod method, String linkEndpoint
    ) throws Exception {
        mockMvc.perform(request(method, linkEndpoint))
            .andExpect(status().isUnauthorized());
    }

    public static JsonNode getLinkResponseJsonNodeFromBody(
        MvcResult linkResponse,
        ObjectMapper mapper
    ) throws Exception {
        String linkResponseBody = linkResponse.getResponse().getContentAsString();
        return mapper.readTree(linkResponseBody);
    }
}
