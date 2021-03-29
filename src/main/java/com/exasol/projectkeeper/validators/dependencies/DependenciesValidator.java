package com.exasol.projectkeeper.validators.dependencies;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Consumer;

import com.exasol.errorreporting.ExaError;
import com.exasol.projectkeeper.ValidationFinding;
import com.exasol.projectkeeper.Validator;
import com.exasol.projectkeeper.pom.MavenArtifactModelReader;
import com.exasol.projectkeeper.pom.MavenFileModelReader;
import com.exasol.projectkeeper.validators.dependencies.renderer.DependencyPageRenderer;

/**
 * {@link Validator} for the dependencies.md file.
 */
public class DependenciesValidator implements Validator {
    private final File pomFile;
    private final ProjectDependencyReader projectDependencyReader;
    private final Path dependenciesFile;

    /**
     * Create a new instance of {@link DependenciesValidator}.
     * 
     * @param fileModelReader     pom file parser
     * @param artifactModelReader maven dependency reader
     * @param pomFile             pom file to validate
     * @param projectDirectory    project root directory
     */
    public DependenciesValidator(final MavenFileModelReader fileModelReader,
            final MavenArtifactModelReader artifactModelReader, final File pomFile, final Path projectDirectory) {
        this.projectDependencyReader = new ProjectDependencyReader(fileModelReader, artifactModelReader);
        this.pomFile = pomFile;
        this.dependenciesFile = projectDirectory.resolve("dependencies.md");
    }

    @Override
    public Validator validate(final Consumer<ValidationFinding> findingConsumer) {
        final List<ProjectDependency> dependencies = this.projectDependencyReader.readDependencies(this.pomFile);
        final String expectedDependenciesPage = new DependencyPageRenderer().render(dependencies);
        if (!this.dependenciesFile.toFile().exists()) {
            findingConsumer
                    .accept(new ValidationFinding(
                            ExaError.messageBuilder("E-PK-50")
                                    .message("This project does not have a dependencies.md file.").toString(),
                            getFix(expectedDependenciesPage)));
        } else {
            validateFileContent(findingConsumer, expectedDependenciesPage);
        }
        return this;
    }

    private void validateFileContent(final Consumer<ValidationFinding> findingConsumer,
            final String expectedDependenciesPage) {
        try {
            final String actualContent = Files.readString(this.dependenciesFile);
            if (!actualContent.equals(expectedDependenciesPage)) {
                findingConsumer.accept(new ValidationFinding(ExaError.messageBuilder("E-PK-53").message(
                        "The dependencies.md file has a outdated content.\nExpected content:\n{{expected content|uq}}",
                        expectedDependenciesPage).toString(), getFix(expectedDependenciesPage)));
            }
        } catch (final IOException exception) {
            throw new IllegalStateException(ExaError.messageBuilder("E-PK-52")
                    .message("Failed to read dependencies.md for validation.").toString(), exception);
        }
    }

    private ValidationFinding.Fix getFix(final String expectedDependenciesPage) {
        return log -> {
            try {
                Files.writeString(this.dependenciesFile, expectedDependenciesPage, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (final IOException exception) {
                throw new IllegalStateException(
                        ExaError.messageBuilder("E-PK-51").message("Failed to write dependencies.md file.").toString(),
                        exception);
            }
        };
    }
}
