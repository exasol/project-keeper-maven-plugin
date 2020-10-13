package com.exasol.projectkeeper;

import java.util.Collection;

import org.w3c.dom.Node;

/**
 * Template for ossindex-maven-plugin.
 */
public class OssindexMavenPluginPomTemplate extends AbstractPluginPomTemplate {

    /**
     * Create a new instance of {@link OssindexMavenPluginPomTemplate}.
     */
    public OssindexMavenPluginPomTemplate() {
        super("maven_templates/ossindex-maven-plugin.xml");
    }

    @Override
    protected void validatePluginConfiguration(final Node plugin, final RunMode runMode,
            final Collection<String> enabledModules) throws PomTemplateValidationException {
        verifyOrFixPluginPropertyHasExactValue(plugin, runMode, "executions");
    }

    @Override
    public String getModule() {
        return AbstractProjectKeeperMojo.MODULE_DEFAULT;
    }
}
