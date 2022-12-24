package LDSS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    //메인 페이징
    @GetMapping("/")
    public String main() throws Exception{
        return "main";
    }
}
