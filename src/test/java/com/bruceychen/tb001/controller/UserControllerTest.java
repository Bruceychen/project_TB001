package com.bruceychen.tb001.controller;

import com.bruceychen.tb001.dto.UserCreateRequest;
import com.bruceychen.tb001.dto.UserResponse;
import com.bruceychen.tb001.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    @Test
    void list_returns200WithJsonArray() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                new UserResponse(1L, "alice"),
                new UserResponse(2L, "bob")
        ));

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].username").value("bob"));
    }

    @Test
    void getById_returns200WithSingleObject() throws Exception {
        when(userService.findById(1L)).thenReturn(new UserResponse(1L, "alice"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getById_missingUser_returns404() throws Exception {
        when(userService.findById(99L)).thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/api/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_validBody_returns201WithLocationHeader() throws Exception {
        UserCreateRequest req = new UserCreateRequest("david");
        when(userService.create(any(UserCreateRequest.class)))
                .thenReturn(new UserResponse(7L, "david"));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/user/7"))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.username").value("david"));
    }

    @Test
    void create_blankUsername_returns400() throws Exception {
        UserCreateRequest req = new UserCreateRequest("");

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("username")));
    }

    @Test
    void delete_existingUser_returns204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/user/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(eq(1L));
    }

    @Test
    void delete_missingUser_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("User", 99L))
                .when(userService).delete(99L);

        mockMvc.perform(delete("/api/user/99"))
                .andExpect(status().isNotFound());
    }
}
