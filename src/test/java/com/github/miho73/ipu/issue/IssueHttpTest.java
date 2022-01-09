package com.github.miho73.ipu.issue;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IssueHttpTest {
    @Autowired MockMvc mockMvc;
    @Autowired SessionService sessionService;

    MockHttpSession session;

    @Before
    public void setUp() {
        session = new MockHttpSession();
        session.setAttribute("privilege", "u");
        session.setAttribute("id", "test_user_id");
        session.setAttribute("code", 0);
        session.setAttribute("name", "test-user");
        session.setAttribute("isLoggedIn", true);
        sessionService.addToSessionTable(session);
    }

    @DisplayName("Get issues page test (Success)")
    @Test
    public void getIssuePageTest() throws Exception {
        mockMvc.perform(
                    get("/issue")
                )
                .andExpect(status().isOk());
    }

    @DisplayName("Create new issue (Success)")
    @Test
    public void createNewIssueGreen() throws Exception {
        mockMvc.perform(
                        post("/issue/api/create-new")
                                .param("name", "Test Issue")
                                .param("type", "0")
                                .param("pCode", "1")
                                .param("content", "Test Issue.")
                                .session(session)
                )
                .andExpect(status().isCreated());
    }
    @DisplayName("Create new issue (Fail)")
    @Test
    public void createNewIssueRed() throws Exception {


        mockMvc.perform(
                        post("/issue/api/create-new")
                                .param("name", "Test Issue")
                                .param("type", "0")
                                .param("pCode", "9999")
                                .param("content", "Test Issue.")
                                .session(session)
                )
                .andExpect(status().isBadRequest());
    }
}
