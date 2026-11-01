package com.example.testgen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Demande de génération de tests unitaires")
public record GenerateRequest(
        @Schema(description = "Code source complet de la classe Java à tester")
        @NotBlank(message = "Le code source est obligatoire")
        @Size(max = 50_000, message = "Le code source est trop volumineux (max 50 000 caractères)")
        String sourceCode,

        @Schema(description = "Utiliser Mockito pour simuler les dépendances", defaultValue = "true")
        Boolean useMockito,

        @Schema(description = "Consignes supplémentaires (optionnel)", example = "Teste aussi les cas limites et les exceptions")
        @Size(max = 1000)
        String additionalInstructions
) {
    public boolean mockitoEnabled() {
        return useMockito == null || useMockito;
    }
}
