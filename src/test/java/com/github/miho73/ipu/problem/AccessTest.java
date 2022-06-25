package com.github.miho73.ipu.problem;

import com.github.miho73.ipu.services.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AccessTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    SessionService sessionService;

    MockHttpSession session;

    @Before
    public void setupLogin() {
        session = new MockHttpSession();
        session.setAttribute("privilege", "u");
        session.setAttribute("id", "test_user_id");
        session.setAttribute("code", 0);
        session.setAttribute("name", "test-user");
        session.setAttribute("isLoggedIn", true);
        sessionService.addToSessionTable(session);
    }

    @DisplayName("Get problem without login (Fail)")
    @Test
    public void getProblemWithoutLogin() throws Exception {
        mockMvc.perform(
                    get("/problem/1")
                )
                .andExpect(status().isFound());
    }

    @DisplayName("Get problem with login (Success)")
    @Test
    public void getProblemWithLogin() throws Exception {
        mockMvc.perform(
                    get("/problem/1")
                                .session(session)
                )
                .andExpect(status().isOk());
    }

    @DisplayName("Get problem via api without login (Fail)")
    @Test
    public void getProblemViaApiWithoutLogin() throws Exception {
        mockMvc.perform(
                        get("/problem/api/get")
                                .param("code", "1")
                )
                .andExpect(status().isForbidden());
    }

    @DisplayName("Get problem via api with invalid data (Fail)")
    @Test
    public void getProblemViaApiWithInvalidData() throws Exception {
        mockMvc.perform(
                        get("/problem/api/get")
                                .param("code", "text")
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"));
    }

    @DisplayName("Get problem via api with login (Success)")
    @Test
    public void getProblemViaApiWithLogin() throws Exception {
        mockMvc.perform(
                        get("/problem/api/get")
                                .param("code", "1")
                                .session(session)
                )
                .andExpect(status().isOk());
    }

    @DisplayName("Get problem resource without login (Fail)")
    @Test
    public void getProblemResourceWithoutLogin() throws Exception {
        mockMvc.perform(
                        get("/problem/lib/VLgV_m8Q-eSl8S3CYCiwxw==")
                )
                .andExpect(status().isForbidden());
    }

    @DisplayName("Get problem resource with login (Success)")
    @Test
    public void getProblemResourceWithLogin() throws Exception {
        mockMvc.perform(
                        get("/problem/lib/VLgV_m8Q-eSl8S3CYCiwxw==")
                                .session(session)
                )
                .andExpect(status().isOk());
    }
}
