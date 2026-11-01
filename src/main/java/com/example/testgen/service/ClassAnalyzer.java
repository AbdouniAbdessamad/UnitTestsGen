package com.example.testgen.service;

import com.example.testgen.exception.InvalidSourceException;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Component;

import java.util.List;

/** Analyse statique du code Java reçu (JavaParser). */
@Component
public class ClassAnalyzer {

    public record ClassInfo(String packageName, String className,
                            List<String> methods, List<String> dependencies) {}

    public ClassInfo analyze(String source) {
        ParseResult<CompilationUnit> result = new JavaParser().parse(source);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            throw new InvalidSourceException("Le code fourni n'est pas du Java valide : "
                    + result.getProblems().stream().findFirst().map(p -> p.getMessage()).orElse("erreur inconnue"));
        }
        CompilationUnit cu = result.getResult().get();
        ClassOrInterfaceDeclaration cls = cu.findFirst(ClassOrInterfaceDeclaration.class,
                        c -> !c.isInterface())
                .orElseThrow(() -> new InvalidSourceException("Aucune classe Java trouvée dans le code fourni."));

        List<String> methods = cls.getMethods().stream()
                .filter(m -> !m.isPrivate())
                .map(m -> m.getDeclarationAsString(true, true, true))
                .toList();

        List<String> deps = cls.getFields().stream()
                .filter(f -> !f.isStatic())
                .flatMap(f -> f.getVariables().stream()
                        .map(v -> v.getType().asString() + " " + v.getNameAsString()))
                .toList();

        String pkg = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
        return new ClassInfo(pkg, cls.getNameAsString(), methods, deps);
    }

    /** Retourne la liste des erreurs de syntaxe (liste vide = code valide). */
    public List<String> syntaxErrors(String code) {
        if (code == null || code.isBlank()) {
            return List.of("Le modèle a renvoyé une réponse vide.");
        }
        ParseResult<CompilationUnit> result = new JavaParser().parse(code);
        if (result.isSuccessful()) {
            return List.of();
        }
        return result.getProblems().stream().map(p -> p.getMessage()).limit(5).toList();
    }
}
