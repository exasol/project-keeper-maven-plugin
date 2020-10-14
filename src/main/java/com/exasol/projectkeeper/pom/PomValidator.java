package com.exasol.projectkeeper.pom;

import java.util.Collection;
import java.util.function.Consumer;

import org.w3c.dom.Document;

import com.exasol.projectkeeper.ProjectKeeperModule;

/**
 * Interface for Pom file validation templates.
 * <p>
 * Register your instances of this interface to {@link PomFileTemplateRunner#ALL_VALIDATORS}.
 * </p>
 */
public interface PomValidator {

    /**
     * Run the template validation or fixing.
     * 
     * @param pom             pom document
     * @param enabledModules  list of enabled modules
     * @param findingConsumer Consumer that accepts the {@link PomValidationFinding}s. The findings must be fixed in the
     *                        order, they are reported.
     */
    public void validate(final Document pom, final Collection<ProjectKeeperModule> enabledModules,
            Consumer<PomValidationFinding> findingConsumer);

    /**
     * Get the module this template belongs to.
     *
     * @return module this template belongs to
     */
    public ProjectKeeperModule getModule();
}
