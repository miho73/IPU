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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SubmitSolutionTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    SessionService sessionService;

    MockHttpSession session;

    @Before
    public void setupLogin() {
        session = new MockHttpSession();
        session.setAttribute("privilege", "up");
        session.setAttribute("id", "test_user_id");
        session.setAttribute("code", 2);
        session.setAttribute("name", "test-user");
        session.setAttribute("isLoggedIn", true);
        sessionService.addToSessionTable(session);
    }

    @Test
    @DisplayName("Submit solution with correct answer")
    public void CorrectTest() throws Exception {
        mockMvc.perform(
                post("/problem/api/solution/post")
                        .session(session)
                        .param("code", "1")
                        .param("time", "123")
                        .param("answer", "1/2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @DisplayName("Submit solution with wrong answer")
    public void WrongTest() throws Exception {
        mockMvc.perform(
                post("/problem/api/solution/post")
                        .session(session)
                        .param("code", "1")
                        .param("time", "123")
                        .param("answer", "1/3")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    @DisplayName("Submit solution with self judge AC")
    public void SelfACTest() throws Exception {
        mockMvc.perform(
                        post("/problem/api/solution/post")
                                .session(session)
                                .param("code", "2")
                                .param("time", "123")
                                .param("answer", "true")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @DisplayName("Submit solution with self judge WA")
    public void SelfWATest() throws Exception {
        mockMvc.perform(
                        post("/problem/api/solution/post")
                                .session(session)
                                .param("code", "2")
                                .param("time", "123")
                                .param("answer", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(false));
    }
}
