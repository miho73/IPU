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

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @DisplayName("Get issue view page (Success)")
    @Test
    public void getIssueViewPageTest() throws Exception {
        mockMvc.perform(
                get("/issue/1")
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

    @DisplayName("Change issue name (Success)")
    @Test
    public void changeIssueNameGreen() throws Exception {
        String newName = "Title for test changed at "+(new Date().toString());

        mockMvc.perform(
                patch("/issue/api/name/update")
                        .param("issue-code", "1")
                        .param("new-name", newName)
                        .session(session)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/issue/api/get/name")
                        .param("issue-code", "1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value(newName));
    }
}
