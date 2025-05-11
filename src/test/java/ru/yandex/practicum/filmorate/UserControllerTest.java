package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        String userJson = "{\"email\":\"invalid-email\", \"login\":\"login\", \"birthday\":\"2000-01-01\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() throws Exception {
        String userJson = "{\"email\":\"test@mail.ru\", \"login\":\"validLogin\", \"name\":\"\", \"birthday\":\"2000-01-01\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("validLogin"));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmptyOnCreate() throws Exception {
        String userJson = "{\"email\":\"test@mail.ru\", \"login\":\"validLogin\", \"name\":\"\", \"birthday\":\"2000-01-01\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("validLogin"));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmptyOnUpdate() throws Exception {
        String createJson = "{\"email\":\"test@mail.ru\", \"login\":\"validLogin\", \"birthday\":\"2000-01-01\"}";
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int id = extractIdFromResponse(response);
        String updateJson = "{\"id\":" + id + ", \"email\":\"updated@mail.ru\"," +
                " \"login\":\"newLogin\", \"name\":\"\", \"birthday\":\"2000-01-01\"}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newLogin"))
                .andExpect(jsonPath("$.email").value("updated@mail.ru"));
    }

    @Test
    void shouldKeepNameWhenUpdatingWithNonEmptyName() throws Exception {
        String createJson = "{\"email\":\"test@mail.ru\", \"login\":\"validLogin\", \"birthday\":\"2000-01-01\"}";
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int id = extractIdFromResponse(response);

        String updateJson = "{\"id\":" + id + ", \"email\":\"updated@mail.ru\"," +
                " \"login\":\"newLogin\", \"name\":\"Custom Name\", \"birthday\":\"2000-01-01\"}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Custom Name"))
                .andExpect(jsonPath("$.login").value("newLogin"));
    }

    private int extractIdFromResponse(String response) {
        String idField = "\"id\":";
        int start = response.indexOf(idField) + idField.length();
        int end = response.indexOf(",", start);
        return Integer.parseInt(response.substring(start, end));
    }
}

