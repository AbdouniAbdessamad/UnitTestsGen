package com.example.testgen.service;

import com.example.testgen.dto.GenerateRequest;
import com.example.testgen.dto.GenerateResponse;
import com.example.testgen.service.ClassAnalyzer.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(TestGeneratorService.class);
    private static final int MAX_ATTEMPTS = 2;

    private static final String SYSTEM_PROMPT = """
            Tu es un expert en tests unitaires Java. Tu écris des classes de tests JUnit 5 (Jupiter)
            propres, lisibles et compilables.
            Règles strictes :
            - Réponds UNIQUEMENT avec le code Java de la classe de test, sans explication ni Markdown.
            - Conserve le même package que la classe testée.
            - Inclus tous les imports nécessaires.
            - Utilise @Test, @DisplayName, @BeforeEach et Assertions (assertEquals, assertThrows, assertTrue...).
            - Couvre : cas nominaux, cas limites (null, vide, 0, valeurs négatives) et cas d'erreur (exceptions).
            - Un test = un comportement. Noms de méthodes explicites.
            - N'invente jamais de méthodes qui n'existent pas dans la classe testée.
            """;

    private final ChatClient chatClient;
    private final ClassAnalyzer analyzer;
    private final String model;

    public TestGeneratorService(ChatClient.Builder builder,
                                ClassAnalyzer analyzer,
                                @Value("${spring.ai.ollama.chat.options.model}") String model) {
        this.chatClient = builder.build();
        this.analyzer = analyzer;
        this.model = model;
    }

    public GenerateResponse generate(GenerateRequest request) {
        long start = System.currentTimeMillis();
        ClassInfo info = analyzer.analyze(request.sourceCode());
        String testClassName = info.className() + "Test";

        String code = "";
        List<String> errors = List.of("non généré");
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS && !errors.isEmpty()) {
            attempts++;
            String userPrompt = buildUserPrompt(request, info, testClassName,
                    attempts > 1 ? code : null, attempts > 1 ? errors : null);

            String raw = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            code = CodeExtractor.extractJava(raw);
            errors = analyzer.syntaxErrors(code);
            log.info("Tentative {} : {} erreur(s) de syntaxe", attempts, errors.size());
        }

        return new GenerateResponse(info.packageName(), info.className(), testClassName,
                info.methods(), code, errors.isEmpty(), errors, attempts, model,
                System.currentTimeMillis() - start);
    }

    private String buildUserPrompt(GenerateRequest req, ClassInfo info, String testClassName,
                                   String previousCode, List<String> previousErrors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Génère la classe de test JUnit 5 nommée ").append(testClassName)
          .append(" pour la classe ").append(info.className()).append(".\n\n");

        sb.append("Méthodes à tester :\n");
        info.methods().forEach(m -> sb.append("- ").append(m).append('\n'));

        if (req.mockitoEnabled() && !info.dependencies().isEmpty()) {
            sb.append("\nDépendances à simuler avec Mockito (@ExtendWith(MockitoExtension.class), @Mock, @InjectMocks) :\n");
            info.dependencies().forEach(d -> sb.append("- ").append(d).append('\n'));
        } else if (!req.mockitoEnabled()) {
            sb.append("\nN'utilise pas Mockito.\n");
        }

        if (req.additionalInstructions() != null && !req.additionalInstructions().isBlank()) {
            sb.append("\nConsignes supplémentaires : ").append(req.additionalInstructions()).append('\n');
        }

        sb.append("\nCode source de la classe :\n").append(req.sourceCode()).append('\n');

        if (previousCode != null && previousErrors != null) {
            sb.append("\nTa précédente réponse contenait des erreurs de syntaxe :\n")
              .append(String.join("\n", previousErrors))
              .append("\n\nCode précédent :\n").append(previousCode)
              .append("\n\nCorrige-le et renvoie uniquement le code Java complet.");
        }
        return sb.toString();
    }
}
