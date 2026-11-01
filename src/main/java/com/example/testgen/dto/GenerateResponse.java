package com.example.testgen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Résultat de la génération")
public record GenerateResponse(
        String packageName,
        String className,
        String testClassName,
        List<String> methodsDetected,
        String testCode,
        @Schema(description = "true si le code généré est syntaxiquement valide") boolean syntaxValid,
        List<String> syntaxErrors,
        int attempts,
        String model,
        long durationMs
) {}
