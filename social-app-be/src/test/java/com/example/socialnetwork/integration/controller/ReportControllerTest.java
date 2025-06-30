package com.example.socialnetwork.integration.controller;

import com.example.socialnetwork.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql("/report-test-data.sql")
class ReportControllerTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "reporter@test.com")
    void getWeeklyReport_Success_ReturnsExcelFile() throws Exception {
        mockMvc.perform(get("/api/reports/weekly-summary"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".xlsx")))
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }

    @Test
    void getWeeklyReport_Unauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/reports/weekly-summary"))
                .andExpect(status().is(400));
    }
}