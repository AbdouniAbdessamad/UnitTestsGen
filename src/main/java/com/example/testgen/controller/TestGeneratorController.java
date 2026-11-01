package com.example.testgen.controller;

import com.example.testgen.dto.GenerateRequest;
import com.example.testgen.dto.GenerateResponse;
import com.example.testgen.exception.InvalidSourceException;
import com.example.testgen.service.TestGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/tests")
@Tag(name = "Génération de tests", description = "Envoie une classe Java, reçois des tests JUnit 5")
public class TestGeneratorController {

    private final TestGeneratorService service;

    public TestGeneratorController(TestGeneratorService service) {
        this.service = service;
    }

    @Operation(summary = "Générer des tests JUnit à partir du code source (JSON)")
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
        return service.generate(request);
    }

    @Operation(summary = "Générer des tests JUnit à partir d'un fichier .java (upload)")
    @PostMapping(value = "/generate/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GenerateResponse generateFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "useMockito", defaultValue = "true") boolean useMockito) throws IOException {
        String name = file.getOriginalFilename();
        if (name == null || !name.endsWith(".java")) {
            throw new InvalidSourceException("Le fichier doit avoir l'extension .java");
        }
        String source = new String(file.getBytes(), StandardCharsets.UTF_8);
        return service.generate(new GenerateRequest(source, useMockito, null));
    }
}
