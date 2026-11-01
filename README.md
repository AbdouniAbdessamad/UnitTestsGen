# TestGen AI — Générateur de tests unitaires JUnit (Mini-projet n°23)

API REST qui reçoit une **classe Java** et renvoie une **classe de tests JUnit 5** générée par un LLM local.

## Stack
- Spring Boot 3.4 + **Spring AI** (`ChatClient`) + **Ollama** (modèle local gratuit)
- **JavaParser** : analyse statique du code reçu + validation de la syntaxe du code généré
- **Swagger / OpenAPI** (springdoc)
- Front : page HTML/JS simple (servie par Spring Boot)

## Fonctionnement
1. Le client envoie le code (JSON ou upload `.java`).
2. `ClassAnalyzer` (JavaParser) extrait package, classe, méthodes publiques, dépendances (champs).
3. `TestGeneratorService` construit un prompt (règles JUnit 5, Mockito si dépendances) et appelle Ollama.
4. `CodeExtractor` retire le Markdown de la réponse.
5. Le code généré est **re-parsé** : s'il est invalide, une 2e tentative est faite en renvoyant les erreurs au modèle (boucle d'auto-correction).
6. L'API renvoie le code + métadonnées (syntaxe valide, tentatives, durée, modèle).

## Lancer le projet
```bash
# 1. Installer Ollama (https://ollama.com) puis :
ollama pull qwen2.5-coder:7b      # ou llama3.1, codellama, deepseek-coder...
ollama serve

# 2. Lancer l'API (Java 17+, Maven)
mvn spring-boot:run
```
- Interface web : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html

## Exemple d'appel
```bash
curl -X POST http://localhost:8080/api/v1/tests/generate \
  -H "Content-Type: application/json" \
  -d '{"sourceCode":"public class Calc { public int add(int a,int b){return a+b;} }","useMockito":false}'

# ou upload d'un fichier
curl -X POST http://localhost:8080/api/v1/tests/generate/upload \
  -F "file=@samples/BankAccount.java"
```

## Changer de modèle
Dans `application.yml` : `spring.ai.ollama.chat.options.model`. Pour OpenAI/Anthropic, remplacer le starter dans le `pom.xml` (le code `ChatClient` reste identique).

## Structure
```
src/main/java/com/example/testgen/
├── controller/TestGeneratorController.java
├── service/{TestGeneratorService, ClassAnalyzer, CodeExtractor}.java
├── dto/{GenerateRequest, GenerateResponse}.java
└── exception/{InvalidSourceException, GlobalExceptionHandler}.java
```

## Pistes d'amélioration (pour le rapport)
- Compiler et **exécuter** les tests générés (javax.tools + JUnit Launcher) et renvoyer taux de réussite / couverture JaCoCo.
- RAG (pgvector) sur des exemples de bons tests pour imiter le style de l'équipe.
- Évaluation comparative de modèles (qwen2.5-coder vs llama3.1 vs codellama) : syntaxe valide, compilation, couverture.
- Génération multi-classes (projet entier).
