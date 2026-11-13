package com.moneytrasnfer.moneytransferservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytrasnfer.moneytransferservice.model.TransferHistoryResponse;
import com.moneytrasnfer.moneytransferservice.model.TransferRequest;
import com.moneytrasnfer.moneytransferservice.service.TransferService;
import org.openapitools.jackson.nullable.JsonNullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class) // Replace with your actual controller class name
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();;

    @MockitoBean
    private TransferService transferService;

    @Test
    void makeTransfer_Succeeds() throws Exception {

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(101L);
        request.setTargetAccountNumber(102L);
        request.setAmount(BigDecimal.valueOf(100.00));

        mockMvc.perform(post("/api/transfers") // Adjust path to match your contract/mapping
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Adjust to isCreated() if your endpoint returns 201 Created
    }

    @Test
    void getTransferHistory_Succeeds_WhenTransfersExist() throws Exception {

        Long accountNumber = 101L;

        TransferHistoryResponse historyResponse = new TransferHistoryResponse();
        historyResponse.setTransferId(1L);
        historyResponse.setSourceAccountNumber(101L);
        historyResponse.setTargetAccountNumber(102L);
        historyResponse.setAmountSent(BigDecimal.valueOf(100.00));
        historyResponse.setAmountReceived(BigDecimal.valueOf(100.00));
        historyResponse.setExchangeRate(BigDecimal.ONE);
        historyResponse.setStatus("SUCCESS");
        historyResponse.setFailureReason(JsonNullable.undefined());
        historyResponse.setDateOfTransfer(OffsetDateTime.now());

        when(transferService.getTransferHistory(accountNumber))
                .thenReturn(List.of(historyResponse));

        mockMvc.perform(get("/api/accounts/{accountNumber}/transfers", accountNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].transferId").value(1L))
                .andExpect(jsonPath("$[0].sourceAccountNumber").value(101L))
                .andExpect(jsonPath("$[0].targetAccountNumber").value(102L))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].amountSent").value(100.00));
    }

    @Test
    void getTransferHistory_ReturnsEmptyList_WhenNoTransfersExist() throws Exception {
        // Arrange
        Long accountNumber = 999L;

        when(transferService.getTransferHistory(accountNumber))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}/transfers", accountNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}