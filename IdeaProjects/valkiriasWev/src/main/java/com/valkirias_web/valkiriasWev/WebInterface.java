package com.valkirias_web.valkiriasWev;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
public class WebInterface {
    @GetMapping("/")
    public String hello(Model m) {
        m.addAttribute("title", "MyMessage");
        return "Index";
    }
    @RequestMapping("/t")
    public String index() {
        return "test";
    }
}
