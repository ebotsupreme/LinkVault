package com.linkvault.controller;

import com.linkvault.constants.apiPaths.AuthEndpoints;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.linkvault.util.LogUtils.info;

@Validated
@Slf4j
@RestController
@RequestMapping(AuthEndpoints.BASE_AUTH)
public class AuthController {

    @PostMapping(AuthEndpoints.LOGIN)
    public void login(@Valid @RequestBody  String userName, String password) {
        info(log, "Logging in user: {}", userName);
    }
}
