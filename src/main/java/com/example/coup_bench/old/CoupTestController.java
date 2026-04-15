package com.example.coup_bench.old;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coup")
public class CoupTestController {

    private final CoupTestService coupTestService;

    public CoupTestController(CoupTestService coupTestService) {
        this.coupTestService = coupTestService;
    }

    @GetMapping("/test")
    public CoupTestSnapshot playTestGame() {
        return coupTestService.playTestGame();
    }
}
