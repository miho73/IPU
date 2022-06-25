package com.github.miho73.ipu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
public class DdayController {
    @GetMapping("/dday")
    public String ddayHandler(@RequestParam(value = "for", required = false, defaultValue = "") String dfor,
                              Model model, HttpServletResponse response) throws IOException {
        String forWhat = "";
        long posix = 0;
        switch (dfor) {
            case "sshs" -> {
                forWhat = "영재학교 2차 시험";
                posix = 1657407600;
            }
            default -> response.sendError(404);
        }
        model.addAllAttributes(Map.of(
            "forwhat", forWhat,
            "posix", posix
        ));
        return "dday/dday";
    }
}
