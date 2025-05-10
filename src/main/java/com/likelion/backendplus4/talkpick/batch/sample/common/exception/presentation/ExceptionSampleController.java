package com.likelion.backendplus4.talkpick.batch.sample.common.exception.presentation;

import com.likelion.backendplus4.talkpick.batch.sample.common.exception.application.ExceptionSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/temp")
public class ExceptionSampleController {

    private final ExceptionSampleService sampleService;


    @GetMapping("/fail-case1")
    public String failCase1() {
        return sampleService.failCase1();
    }

    @GetMapping("/fail-case2")
    public String failCase2() {
        return sampleService.failCase2(null);
    }
}
