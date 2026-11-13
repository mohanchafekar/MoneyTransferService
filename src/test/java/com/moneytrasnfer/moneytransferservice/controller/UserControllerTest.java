package com.moneytrasnfer.moneytransferservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.exception.GlobalExceptionHandler;
import com.moneytrasnfer.moneytransferservice.model.UserRequest;
import com.moneytrasnfer.moneytransferservice.model.UserResponse;
import com.moneytrasnfer.moneytransferservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_ReturnsCreated() throws Exception {

        UserRequest request = new UserRequest();
        request.setUniqueId("UID-1001");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setContactNumber("+31611111111");
        request.setAddress("Amsterdam");

        UserResponse response = new UserResponse();
        response.setUserId(1L);
        response.setUniqueId("UID-1001");
        response.setFirstName("John");
        response.setLastName("Doe");

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.uniqueId").value("UID-1001"));
    }

    @Test
    void createUser_ReturnsUnprocessableContent_WhenUserAlreadyExists() throws Exception {

        UserRequest request = new UserRequest();
        request.setUniqueId("UID-1001");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setContactNumber("+31611111111");
        request.setAddress("Amsterdam");

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new FunctionalException(FunctionalErrorCode.USER_ALREADY_EXISTS));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("User with this unique ID already exists"))
                .andExpect(jsonPath("$.['trace-id']").exists());
    }
}
