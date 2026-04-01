package com.project.concert.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin(
        origins = "https://concertticketingsystem.netlify.app",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)

public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }
}