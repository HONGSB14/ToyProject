package LDSS.controller;

import LDSS.service.UserService;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class MainController {

    @Autowired
    private UserService userService;

    @PostMapping("/idInput")
    public JSONArray userInfo(@RequestParam ("myId")String myName){
       return userService.userInfo(myName);
    }

}
