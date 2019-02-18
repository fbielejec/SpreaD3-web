package com.spread.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.spread.utils.TokenUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "log.file.path=/logs",
        "storage.location=/spread-storage"
    })
public class TokenControllerTests {

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @Before
    public void setupMockMvc() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext) //
            .build();
    }

    @Test
    public void testForgedToken() throws Exception {
        String forgedSecret = "forged";
        String sessionId = UUID.randomUUID().toString();
        String token = TokenUtils.createJWT(forgedSecret, sessionId);

        mockMvc.perform(MockMvcRequestBuilders.get("/continuous/model").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

}
