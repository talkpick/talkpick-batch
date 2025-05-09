package com.likelion.backendplus4.talkpick.batch.temp.presentation;

import com.likelion.backendplus4.talkpick.batch.temp.application.TempService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/temp")
public class TempController {

    private final TempService tempService;


    @RequestMapping("/fail-case1")
    public String failCase1() {
        return tempService.failCase1();
    }

    @RequestMapping("/fail-case2")
    public String failCase2() {
        return tempService.failCase2(null);
    }
}
