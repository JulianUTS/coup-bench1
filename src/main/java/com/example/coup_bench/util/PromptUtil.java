package com.example.coup_bench.util;

public class PromptUtil {
    public static String cleanResponse(String response) {
        return response
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("`", "")
                .replace("\"null\"", "null");
    }

    public static String getPersonalityPrompt( String personality ) {
        String aggressiveRules = """
        ### PERSONALITY — AGGRESSIVE
        - You prefer high‑impact actions.
        - You prioritize COUP, ASSASSINATE, STEAL, and TAX.
        - You rarely choose INCOME unless forced.
        - You frequently challenge opponents.
        - You block aggressively whenever possible.
        """;

        String defensiveRules = """
        ### PERSONALITY — DEFENSIVE
        - You avoid unnecessary risks.
        - You rarely challenge unless confident.
        - You block only when safe.
        - You prefer TAX, INCOME, and EXCHANGE.
        - You avoid STEAL unless advantageous.
        """;

        String chaoticRules = """
        ### PERSONALITY — CHAOTIC
        - You choose actions unpredictably.
        - You challenge frequently.
        - You block aggressively even when risky.
        - You may choose EXCHANGE or STEAL unexpectedly.
        """;

        String analyticalRules = """
        ### PERSONALITY — ANALYTICAL
        - You make decisions based on logic and probability.
        - You bluff only when the expected value is positive.
        - You challenge selectively, only when evidence is strong.
        - You prefer TAX, EXCHANGE, and safe coin‑efficient plays.
        - You avoid chaotic or impulsive actions.
        """;

        String opportunisticRules = """
        ### PERSONALITY — OPPORTUNISTIC
        - You adapt your strategy based on opponents' behavior.
        - You bluff when opponents appear passive or unlikely to challenge.
        - You challenge aggressive or suspicious opponents more often.
        - You prefer STEAL, TAX, and EXCHANGE depending on the situation.
        - You take calculated risks when they offer high reward.
        """;

        String defaultRules = """
        ### PERSONALITY — DEFAULT
        - You play with balanced, neutral strategy.
        - You bluff occasionally when it is strategically reasonable.
        - You challenge only when moderately confident.
        - You use TAX, INCOME, STEAL, and EXCHANGE without strong bias.
        - You avoid extreme risk-taking or extreme caution.
        """;

        return  (switch (personality) {
            case "aggressive" -> aggressiveRules;
            case "defensive" -> defensiveRules;
            case "chaotic" -> chaoticRules;
            case "analytical" -> analyticalRules;
            case "opportunistic" -> opportunisticRules;
            case "default" -> defaultRules;
            default -> "";
        });
    }
}
