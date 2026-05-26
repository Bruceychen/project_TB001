package com.bruceychen.tb001.controller;

import com.bruceychen.tb001.dto.OrderCreateRequest;
import com.bruceychen.tb001.dto.OrderPatchRequest;
import com.bruceychen.tb001.dto.OrderResponse;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import com.bruceychen.tb001.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean OrderService orderService;

    private static OrderResponse sampleResponse(Long orderId) {
        return new OrderResponse(
                orderId, 1L, "alice",
                1L, 1L, "Food",
                new BigDecimal("100.00"),
                new BigDecimal("0.0500"),
                3,
                new BigDecimal("315.00")
        );
    }

    @Test
    void getByUser_returns200WithJsonArray() throws Exception {
        when(orderService.findByUserId(1L)).thenReturn(List.of(sampleResponse(1L), sampleResponse(2L)));

        mockMvc.perform(get("/api/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].totalCost").value(315.00));
    }

    @Test
    void getByUser_userHasNoOrders_returnsEmptyArray() throws Exception {
        when(orderService.findByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getByUser_missingUser_returns404() throws Exception {
        when(orderService.findByUserId(99L)).thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/api/order/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void create_validBody_returns201WithLocationHeader() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest(1L, 1L, 3);
        when(orderService.create(any(OrderCreateRequest.class))).thenReturn(sampleResponse(7L));

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/order/1"))
                .andExpect(jsonPath("$.orderId").value(7))
                .andExpect(jsonPath("$.totalCost").value(315.00));
    }

    @Test
    void create_invalidBody_missingUserId_returns400() throws Exception {
        String badJson = """
                { "productId": 1, "orderAmount": 3 }
                """;

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("userId")));
    }

    @Test
    void create_invalidBody_zeroAmount_returns400() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest(1L, 1L, 0);

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("orderAmount")));
    }

    @Test
    void patch_validBody_returns200WithUpdated() throws Exception {
        OrderPatchRequest req = new OrderPatchRequest(null, 10);
        OrderResponse updated = new OrderResponse(
                5L, 1L, "alice", 1L, 1L, "Food",
                new BigDecimal("100.00"), new BigDecimal("0.0500"),
                10, new BigDecimal("1050.00")
        );
        when(orderService.patch(eq(5L), any(OrderPatchRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/order/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderAmount").value(10))
                .andExpect(jsonPath("$.totalCost").value(1050.00));
    }

    @Test
    void patch_missingOrder_returns404() throws Exception {
        OrderPatchRequest req = new OrderPatchRequest(null, 5);
        when(orderService.patch(eq(99L), any(OrderPatchRequest.class)))
                .thenThrow(new ResourceNotFoundException("Order", 99L));

        mockMvc.perform(patch("/api/order/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patch_invalidAmount_returns400() throws Exception {
        OrderPatchRequest req = new OrderPatchRequest(null, 0);

        mockMvc.perform(patch("/api/order/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_existingOrder_returns204() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/api/order/1"))
                .andExpect(status().isNoContent());

        verify(orderService).delete(eq(1L));
    }

    @Test
    void delete_missingOrder_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Order", 99L))
                .when(orderService).delete(99L);

        mockMvc.perform(delete("/api/order/99"))
                .andExpect(status().isNotFound());
    }
}
