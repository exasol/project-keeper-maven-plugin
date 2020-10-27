package com.exasol.projectkeeper.validators.pom;

import static com.exasol.projectkeeper.HasNoMoreFindingsAfterApplyingFixesMatcher.hasNoMoreFindingsAfterApplyingFixes;
import static com.exasol.projectkeeper.HasValidationFindingWithMessageMatcher.hasNoValidationFindings;
import static com.exasol.projectkeeper.HasValidationFindingWithMessageMatcher.hasValidationFindingWithMessage;
import static com.exasol.projectkeeper.validators.pom.PomTesting.POM_WITH_NO_PLUGINS;
import static com.exasol.projectkeeper.validators.pom.PomTesting.writeResourceToTempFile;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.exasol.projectkeeper.ProjectKeeperModule;

class PomFileValidatorTest {

    @Test
    void testMissingPlugin(@TempDir final File tempDir) throws IOException {
        final PomFileIO pomFile = new PomFileIO(writeResourceToTempFile(tempDir, POM_WITH_NO_PLUGINS));
        final PomFileValidator runner = new PomFileValidator(Arrays.asList(ProjectKeeperModule.values()), pomFile);
        assertThat(runner, hasValidationFindingWithMessage(
                "E-PK-15: Missing maven plugin org.codehaus.mojo:versions-maven-plugin."));
    }

    @Test
    void testFixMissingPlugin(@TempDir final File tempDir) throws IOException {
        final PomFileIO pomFile = new PomFileIO(writeResourceToTempFile(tempDir, POM_WITH_NO_PLUGINS));
        final PomFileValidator runner = new PomFileValidator(Arrays.asList(ProjectKeeperModule.values()), pomFile);
        assertThat(runner, hasNoMoreFindingsAfterApplyingFixes());
    }

    @Test
    void testNoErrorsOnNoModules(@TempDir final File tempDir) throws IOException {
        final PomFileIO pomFile = new PomFileIO(writeResourceToTempFile(tempDir, POM_WITH_NO_PLUGINS));
        final PomFileValidator runner = new PomFileValidator(Collections.emptyList(), pomFile);
        assertThat(runner, hasNoValidationFindings());
    }
}