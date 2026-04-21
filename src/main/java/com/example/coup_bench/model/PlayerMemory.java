package com.example.coup_bench.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMemory {
    public final List<String> history = new ArrayList<>();
    public final Map<String, Integer> bluffCount = new HashMap<>();
    public final Map<String, Integer> trust = new HashMap<>();
}

