package com.github.miho73.ipu.auth;

import com.github.miho73.ipu.services.SessionService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    SessionService sessionService;


    private MockHttpSession setupLogin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("privilege", "u");
        session.setAttribute("id", "test_user_id");
        session.setAttribute("code", 0);
        session.setAttribute("name", "test-user");
        session.setAttribute("isLoggedIn", true);
        sessionService.addToSessionTable(session);
        return session;
    }

    @DisplayName("Get login page")
    @Test
    public void getLoginPage() throws Exception {
        mockMvc.perform(
                get("/login")
        ).andExpect(status().isOk());
    }

    @DisplayName("Get login page with return address")
    @Test
    public void getLoginPageWithReturnAddress() throws Exception {
        mockMvc.perform(
                get("/login")
                        .param("ret", "/")
        ).andExpect(status().isOk());
    }

    // return address length: 100
    @DisplayName("Get login page with long return address")
    @Test
    public void getLoginPageWithLongReturnAddress() throws Exception {
        mockMvc.perform(
                get("/login")
                        .param("ret", "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("Try login when already logged in")
    @Test
    public void tryLoginWhenAlreadyLoggedIn() throws Exception {
        mockMvc.perform(
                post("/login")
                        .session(setupLogin())
                        .param("id", "test_user_id")
                        .param("password", "test_user_pwd")
                        .param("gToken", "")
                        .param("gVers", "v3")
                        .param("ret", "/returnAddress")
        ).andExpectAll(
                status().is3xxRedirection(),
                redirectedUrl("/returnAddress")
        );
    }

    @DisplayName("Try login without parameter")
    @Test
    public void tryLoginWithoutParameter() throws Exception {
        mockMvc.perform(
                post("/login")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("Test login sign out")
    @Test
    public void signOutTest() throws Exception {
        MockHttpSession session = setupLogin();
        // Check if logged
        mockMvc.perform(
                get("/login")
                        .session(session)
                        .param("ret", "/returnAddress")
        ).andExpectAll(
                status().is3xxRedirection(),
                redirectedUrl("/returnAddress")
        );
        // Sign out
        mockMvc.perform(
                get("/login/deauth")
                        .session(session)
        ).andExpectAll(
                status().is3xxRedirection(),
                redirectedUrl("/")
        );
        // Check if logged again
        mockMvc.perform(
                get("/login")
                        .session(session)
                        .param("ret", "/returnAddress")
        ).andExpectAll(
                status().isOk()
        );
    }
}
