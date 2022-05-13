package FIS.iLUVit.controller;

import FIS.iLUVit.config.argumentResolver.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SignController {

    @GetMapping("/user")
    public String user(@Login Long id) {
        System.out.println("id = " + id);
        return "asd";
    }

    @GetMapping("/asd")
    public String asd() {
        return "asd";
    }

    @PostMapping("/login")
    public String login() {
        return "login";
    }
}
