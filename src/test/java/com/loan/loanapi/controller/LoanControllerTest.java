package com.loan.loanapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.loanapi.dto.response.LoanApprovalResponseDto;
import com.loan.loanapi.dto.response.LoanRequestResponseDto;
import com.loan.loanapi.dto.response.LoanResponseDto;
import com.loan.loanapi.enums.LoanStatus;
import com.loan.loanapi.exception.LoanAlreadyProcessedException;
import com.loan.loanapi.exception.LoanNotFoundException;
import com.loan.loanapi.service.IdempotencyService;
import com.loan.loanapi.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @Test
    void requestLoan_returnsCreatedLoanInSnakeCase() throws Exception {
        LoanRequestResponseDto responseDto = LoanRequestResponseDto.builder()
                .userId("Bruce")
                .loans(List.of(LoanResponseDto.builder()
                        .mrp(BigDecimal.valueOf(100_000_000))
                        .dp(BigDecimal.valueOf(20_000_000))
                        .vehicleYear(2018)
                        .policeNumber("B 1234 BYE")
                        .machineNumber("SDR72V25000W201")
                        .status(LoanStatus.SUBMITTED)
                        .build()))
                .build();
        when(loanService.requestLoan(any())).thenReturn(responseDto);

        String requestJson = """
                {
                  "user_id": "Bruce",
                  "mrp": 100000000,
                  "dp": 20000000,
                  "vehicle_year": 2018,
                  "police_number": "B 1234 BYE",
                  "machine_number": "SDR72V25000W201"
                }
                """;

        mockMvc.perform(post("/api/v1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value("Bruce"))
                .andExpect(jsonPath("$.loans[0].police_number").value("B 1234 BYE"))
                .andExpect(jsonPath("$.loans[0].status").value("SUBMITTED"));
    }

    @Test
    void requestLoan_invalidPayload_returns400() throws Exception {
        String invalidJson = """
                {
                  "user_id": "",
                  "mrp": -1
                }
                """;

        mockMvc.perform(post("/api/v1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveLoan_success() throws Exception {
        when(loanService.approveLoan(anyString(), anyString())).thenReturn(
                LoanApprovalResponseDto.builder()
                        .userId("Bruce")
                        .policeNumber("B 1234 BYE")
                        .message("Loan updated successfully.")
                        .build());

        String requestJson = """
                {
                  "user_id": "Bruce",
                  "police_number": "B 1234 BYE"
                }
                """;

        mockMvc.perform(post("/api/v1/loans/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value("Bruce"))
                .andExpect(jsonPath("$.message").value("Loan updated successfully."));
    }

    @Test
    void approveLoan_notFound_returnsErrorContract() throws Exception {
        when(loanService.approveLoan(anyString(), anyString()))
                .thenThrow(new LoanNotFoundException("Loan not Found"));

        String requestJson = """
                {
                  "user_id": "Bruce",
                  "police_number": "UNKNOWN"
                }
                """;

        mockMvc.perform(post("/api/v1/loans/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("loan_not_found"))
                .andExpect(jsonPath("$.error_description").value("Loan not Found"));
    }

    @Test
    void approveLoan_alreadyProcessed_returnsConflict() throws Exception {
        when(loanService.approveLoan(anyString(), anyString()))
                .thenThrow(new LoanAlreadyProcessedException("Loan is already approved, cannot be approved again."));

        String requestJson = """
                {
                  "user_id": "Bruce",
                  "police_number": "B 1234 BYE"
                }
                """;

        mockMvc.perform(post("/api/v1/loans/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("loan_already_processed"));
    }
}
