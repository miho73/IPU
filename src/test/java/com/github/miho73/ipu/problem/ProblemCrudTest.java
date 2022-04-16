package com.github.miho73.ipu.problem;

import com.github.miho73.ipu.services.SessionService;
import org.json.JSONObject;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProblemCrudTest {
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
        session.setAttribute("code", 0);
        session.setAttribute("name", "test-user");
        session.setAttribute("isLoggedIn", true);
        sessionService.addToSessionTable(session);
    }

    @DisplayName("Create of problem test")
    @Test
    public void createProblemTest() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/problem/register")
                        .session(session)
                        .param("name", "test problem")
                        .param("cate", "etce")
                        .param("diff", "diam")
                        .param("cont", "THIS IS PROBLEM")
                        .param("solu", "THIS IS SOLUTION")
                        .param("tags", "[]")
                        .param("active", "true")
                        .param("answer", "245/2")
                )
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject respones = new JSONObject(result.getResponse().getContentAsString());
        String code = Integer.toString(respones.getInt("result"));

        mockMvc.perform(
                get("/problem/api/get")
                        .session(session)
                        .param("code", code)
                )
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.result.cate").value("etce"),
                        jsonPath("$.result.diff").value("diam"),
                        jsonPath("$.result.prob_cont").value("THIS IS PROBLEM"),
                        jsonPath("$.result.prob_exp").value("THIS IS SOLUTION"),
                        jsonPath("$.result.prob_name").value("test problem"),
                        jsonPath("$.result.tags").value("[]"),
                        jsonPath("$.result.active").value(true),
                        jsonPath("$.result.has_objective").value(true),
                        jsonPath("$.result.judge_type").value(1),
                        jsonPath("$.result.answer").value("245/2")
                );
    }

    @Test
    @DisplayName("Problem add fraction format error")
    public void fractionFormatError() throws Exception {
        mockMvc.perform(
                post("/problem/register")
                        .session(session)
                        .param("name", "test problem")
                        .param("cate", "etce")
                        .param("diff", "diam")
                        .param("cont", "THIS IS PROBLEM")
                        .param("solu", "THIS IS SOLUTION")
                        .param("tags", "[]")
                        .param("active", "true")
                        .param("answer", "2453")
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/problem/register")
                                .session(session)
                                .param("name", "test problem")
                                .param("cate", "etce")
                                .param("diff", "diam")
                                .param("cont", "THIS IS PROBLEM")
                                .param("solu", "THIS IS SOLUTION")
                                .param("tags", "[]")
                                .param("active", "true")
                                .param("answer", "24//53")
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/problem/register")
                                .session(session)
                                .param("name", "test problem")
                                .param("cate", "etce")
                                .param("diff", "diam")
                                .param("cont", "THIS IS PROBLEM")
                                .param("solu", "THIS IS SOLUTION")
                                .param("tags", "[]")
                                .param("active", "true")
                                .param("answer", "24/53/")
                )
                .andExpect(status().isBadRequest());
    }
}
