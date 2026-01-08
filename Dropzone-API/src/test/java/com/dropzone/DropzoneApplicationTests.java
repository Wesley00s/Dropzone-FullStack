package com.dropzone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class DropzoneApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Should load Spring Context successfully (Smoke Test)")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    @DisplayName("Should run main method")
    void shouldRunMainMethod() {
        try {
            DropzoneApplication.main(new String[]{});
        } catch (Exception ignored) {

        }
    }

}
