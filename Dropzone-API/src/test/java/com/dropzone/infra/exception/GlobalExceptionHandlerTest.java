package com.dropzone.infra.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.InnerConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class InnerConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    @RestController
    public static class TestController {
        @GetMapping("/test/bad-request")
        public void throwBadRequest() {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        @GetMapping("/test/general-error")
        public void throwGeneralError() throws Exception {
            throw new Exception("Unexpected system failure");
        }
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and return 400 Bad Request")
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Invalid input parameters")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500 Internal Server Error")
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/general-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("Unexpected system failure")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}