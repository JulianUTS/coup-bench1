package com.example.coup_bench.AiServices;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final MultiModelRouter router;

    public AiController(MultiModelRouter router) {
        this.router = router;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam String provider,
            @RequestParam String prompt
    ) {
        return router.ask(provider, prompt);
    }
}
