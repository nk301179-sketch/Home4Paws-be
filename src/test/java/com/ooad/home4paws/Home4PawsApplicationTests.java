package com.ooad.home4paws;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // This will ensure test properties are used if defined
class Home4PawsApplicationTests {

    @Test
    @WithMockUser
    void contextLoads() {
    }

}
