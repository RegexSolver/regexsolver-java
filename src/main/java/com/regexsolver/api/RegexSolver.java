package com.regexsolver.api;

public final class RegexSolver {
    public static void initialize(String token) {
        RegexSolverApiWrapper.initialize(token);
    }

    public static void initialize(String token, String baseUrl) {
        RegexSolverApiWrapper.initialize(token, baseUrl);
    }
}
