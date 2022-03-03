package com.github.miho73.ipu.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CacheEtagTest {
    @Autowired MockMvc mockMvc;

    private String[] testCases = {"/lib/univ.css", "/lib/univ.js", "/lib/fonts/Cafe24/Cafe24Shiningstar.ttf", "/lib/fonts/Cafe24/Cafe24Oneprettynight.ttf"};

    @Test
    public void testEtagForUnivCss() throws Exception {
        for(String test : testCases) {
            MvcResult result = mockMvc.perform(get(test))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("ETag"))
                    .andReturn();

            String etag = result.getResponse().getHeader("ETag");

            mockMvc.perform(get(test)
                            .header("If-None-Match", etag)
                    )
                    .andExpect(status().isNotModified())
                    .andExpect(header().exists("ETag"))
                    .andReturn();
        }
    }
}
