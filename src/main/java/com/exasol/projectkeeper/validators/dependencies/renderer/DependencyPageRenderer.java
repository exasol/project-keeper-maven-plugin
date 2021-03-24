package com.exasol.projectkeeper.validators.dependencies.renderer;

import java.util.List;
import java.util.stream.Collectors;

import com.exasol.projectkeeper.validators.dependencies.License;
import com.exasol.projectkeeper.validators.dependencies.ProjectDependency;

import net.steppschuh.markdowngenerator.table.Table;

public class DependencyPageRenderer {
    public String render(final List<ProjectDependency> dependencies) {
        final MarkdownReferenceBuilder markdownReferenceBuilder = new MarkdownReferenceBuilder();
        final StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("<!-- @formatter:off -->\n");
        reportBuilder.append("# Dependencies\n");
        reportBuilder.append(
                buildDependencySectionForScope(dependencies, ProjectDependency.Type.COMPILE, markdownReferenceBuilder));
        reportBuilder.append(
                buildDependencySectionForScope(dependencies, ProjectDependency.Type.TEST, markdownReferenceBuilder));
        reportBuilder.append(
                buildDependencySectionForScope(dependencies, ProjectDependency.Type.RUNTIME, markdownReferenceBuilder));
        reportBuilder.append(
                buildDependencySectionForScope(dependencies, ProjectDependency.Type.PLUGIN, markdownReferenceBuilder));
        reportBuilder.append("\n");
        reportBuilder.append(markdownReferenceBuilder.getReferences());
        return reportBuilder.toString();
    }

    private String buildDependencySectionForScope(final List<ProjectDependency> dependencies,
            final ProjectDependency.Type type, final MarkdownReferenceBuilder markdownReferenceBuilder) {
        final List<ProjectDependency> dependenciesOfThisScope = dependencies.stream()
                .filter(dependency -> dependency.getType().equals(type)).collect(Collectors.toList());
        if (dependenciesOfThisScope.isEmpty()) {
            return "";
        } else {
            final String heading = "## " + capitalizeFirstLetter(type.name()) + " Dependencies";
            return "\n" + heading + "\n\n" + buildTable(dependenciesOfThisScope, markdownReferenceBuilder) + "\n";
        }
    }

    private String buildTable(final List<ProjectDependency> dependencies,
            final MarkdownReferenceBuilder markdownReferenceBuilder) {
        final Table.Builder tableBuilder = new Table.Builder().withAlignments(Table.ALIGN_LEFT, Table.ALIGN_LEFT);
        tableBuilder.addRow("Dependency", "License");
        dependencies.forEach(dependency -> {
            final String name = renderDependencyName(dependency, markdownReferenceBuilder);
            final String licenseText = dependency.getLicenses().stream()
                    .map(license -> renderLicense(license, markdownReferenceBuilder)).collect(Collectors.joining("; "));
            tableBuilder.addRow(name, licenseText);
        });
        return tableBuilder.build().toString();
    }

    private String renderDependencyName(final ProjectDependency dependency,
            final MarkdownReferenceBuilder markdownReferenceBuilder) {
        return renderLink(dependency.getName(), dependency.getWebsiteUrl(), markdownReferenceBuilder);
    }

    private String renderLicense(final License license, final MarkdownReferenceBuilder markdownReferenceBuilder) {
        return renderLink(license.getName(), license.getUrl(), markdownReferenceBuilder);
    }

    private String renderLink(final String name, final String url,
            final MarkdownReferenceBuilder markdownReferenceBuilder) {
        if (url == null || url.isBlank()) {
            return name;
        } else {
            return "[" + name + "][" + markdownReferenceBuilder.getReferenceForUrl(name, url) + "]";
        }
    }

    private String capitalizeFirstLetter(final String string) {
        final String lowerCase = string.toLowerCase();
        final String firstLetter = lowerCase.substring(0, 1);
        return firstLetter.toUpperCase() + lowerCase.substring(1);
    }
}
