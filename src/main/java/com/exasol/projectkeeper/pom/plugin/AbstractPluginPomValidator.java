package com.exasol.projectkeeper.pom.plugin;

import static com.exasol.xpath.XPathErrorHanlingWrapper.runXPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import com.exasol.projectkeeper.ProjectKeeperModule;
import com.exasol.projectkeeper.pom.PomFileTemplateRunner;
import com.exasol.projectkeeper.pom.PomValidationFinding;
import com.exasol.projectkeeper.pom.PomValidator;
import com.exasol.xpath.XPathSplitter;

/**
 * Abstract basis for maven plugin configuration validation.
 * <p>
 * Create a new plugin validation by adding a template file to {@code src/main/resources/maven_templates/}. Next create
 * an instance of this class and pass the template's name to the super constructor. If you want to enforce more than
 * only the existence of the plugin definition, override
 * {@link #validatePluginConfiguration(Node, Collection, Consumer)}. Finally add your class to
 * {@link PomFileTemplateRunner#ALL_VALIDATORS}.
 * </p>
 */
public abstract class AbstractPluginPomValidator extends AbstractPomValidator implements PomValidator {
    public static final String GROUP_ID_XPATH = "groupId";
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final String PLUGINS_XPATH = "/project/build/plugins";
    private final String pluginArtifactId;
    private final String pluginGroupId;
    private final Node template;
    private final String pluginXPath;

    public AbstractPluginPomValidator(final String templateResourceName) {
        this.template = readPluginTemplate(templateResourceName);
        this.pluginArtifactId = runXPath(this.template, "artifactId/text()").getNodeValue();
        this.pluginGroupId = runXPath(this.template, GROUP_ID_XPATH + "/text()").getNodeValue();
        this.pluginXPath = PLUGINS_XPATH + "/plugin[artifactId[text()='" + this.pluginArtifactId + "']]";
    }

    @Override
    public void validate(final Document pom, final Collection<ProjectKeeperModule> enabledModules,
            final Consumer<PomValidationFinding> findingConsumer) {
        if (validatePluginExists(pom, findingConsumer)) {
            final Node plugin = runXPath(pom, this.pluginXPath);
            verifyPluginPropertyHasExactValue(plugin, GROUP_ID_XPATH, findingConsumer);
            validatePluginConfiguration(plugin, enabledModules, findingConsumer);
        }
    }

    private boolean validatePluginExists(final Document pom, final Consumer<PomValidationFinding> findingConsumer) {
        if (runXPath(pom, this.pluginXPath) == null) {
            findingConsumer.accept(new PomValidationFinding(
                    "Missing maven plugin " + this.pluginGroupId + ":" + this.pluginArtifactId + ".",
                    getFixForMissingPlugin(pom)));
            return false;
        } else {
            return true;
        }
    }

    private PomValidationFinding.Fix getFixForMissingPlugin(final Document pom) {
        return () -> {
            createObjectPathIfNotExists(runXPath(pom, "/project"), List.of("build", "plugins"));
            final Node plugin = pom.importNode(getPluginTemplate(), true);
            runXPath(pom, PLUGINS_XPATH).appendChild(plugin);
        };
    }

    /**
     * Get the XML template for this plugin.
     * 
     * @return XML template for this plugin
     */
    protected final Node getPluginTemplate() {
        return this.template;
    }

    private Node readPluginTemplate(final String templateResourceName) {
        try (final InputStream templateInputStream = getClass().getClassLoader()
                .getResourceAsStream(templateResourceName)) {
            if (templateInputStream == null) {
                throw new IllegalStateException("F-PK-11 Failed to open " + this.pluginArtifactId + "'s template.");
            }
            DOCUMENT_BUILDER_FACTORY.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            DOCUMENT_BUILDER_FACTORY.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            return documentBuilder.parse(templateInputStream).getFirstChild();
        } catch (final IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalStateException("F-PK-10 Failed to parse " + this.pluginArtifactId + "'s template.");
        }
    }

    /**
     * Hook for validating the plugin specific parts.
     * <p>
     * Hint: use {@link #verifyPluginHasProperty(Node, String, Consumer)} or
     * {@link #verifyPluginPropertyHasExactValue(Node, String, Consumer)} in this method's implementation.
     * </p>
     *
     * @param plugin          the plugin to validate
     * @param enabledModules  list of enabled modules
     * @param findingConsumer to report the validation findings to
     */
    protected void validatePluginConfiguration(final Node plugin, final Collection<ProjectKeeperModule> enabledModules,
            final Consumer<PomValidationFinding> findingConsumer) {
    }

    /**
     * Helper function for validating that an element exists. In FIX mode, this method creates the missing element with
     * the content from the template.
     * <p>
     * This method does not validate, that the element has the correct content. For that purpose use
     * {@link #verifyPluginPropertyHasExactValue(Node, String, Consumer)}.
     * </p>
     * 
     * @param plugin          the plugin to validate
     * @param xPath           path of the property to validate / fix. Only use simple XPaths here (only / and []).
     * @param findingConsumer to report the validation findings to
     * @return {@code true} if validation had no findings.
     */
    protected boolean verifyPluginHasProperty(final Node plugin, final String xPath,
            final Consumer<PomValidationFinding> findingConsumer) {
        if (runXPath(plugin, xPath) != null) {
            return true;
        } else {
            findingConsumer.accept(new PomValidationFinding("The " + this.pluginArtifactId
                    + "'s configuration does not contain the required property " + xPath + ".",
                    getCopyFixForMissingProperty(plugin, xPath)));
            return false;
        }
    }

    private PomValidationFinding.Fix getCopyFixForMissingProperty(final Node plugin,
            final String missingPropertiesXPath) {
        return () -> {
            final List<String> pathSegments = XPathSplitter.split(missingPropertiesXPath);
            findAndCopyFirstMissingPropertyFromTemplate(plugin, pathSegments);
        };
    }

    private void findAndCopyFirstMissingPropertyFromTemplate(final Node plugin, final List<String> pathSegments) {
        for (int pathLength = 1; pathLength <= pathSegments.size(); pathLength++) {
            final String currentXpath = String.join("/", pathSegments.subList(0, pathLength));
            if (runXPath(plugin, currentXpath) == null) {
                final Node parent = runXpathFromSegments(plugin, pathSegments, pathLength - 1);
                copyPropertyFromTemplate(plugin, currentXpath, parent);
                break;
            }
        }
    }

    private void copyPropertyFromTemplate(final Node plugin, final String xPath, final Node parent) {
        final Node node = runXPath(getPluginTemplate(), xPath);
        final Node importedNode = plugin.getOwnerDocument().importNode(node, true);
        parent.appendChild(importedNode);
    }

    private Node runXpathFromSegments(final Node plugin, final List<String> pathSegmments, final int pathLength) {
        if (pathLength == 0) {
            return plugin;
        } else {
            return runXPath(plugin, String.join("/", pathSegmments.subList(0, pathLength)));
        }
    }

    /**
     * Helper function for validating that an element exists and has same content as in the template. In FIX mode, this
     * method creates the missing element and replaces wrong content with the content from the template.
     *
     * @param plugin          the plugin to validate
     * @param propertyXpath   path of the property to validate / fix. Only use simple XPaths here (only / and [])
     * @param findingConsumer consumer to report findings to
     */
    protected void verifyPluginPropertyHasExactValue(final Node plugin, final String propertyXpath,
            final Consumer<PomValidationFinding> findingConsumer) {
        if (verifyPluginHasProperty(plugin, propertyXpath, findingConsumer)) {
            final Node property = runXPath(plugin, propertyXpath);
            final Node templateProperty = runXPath(getPluginTemplate(), propertyXpath);
            validatePropertiesAreEqual(plugin, propertyXpath, findingConsumer, property, templateProperty);
        }
    }

    private void validatePropertiesAreEqual(final Node plugin, final String propertyXpath,
            final Consumer<PomValidationFinding> findingConsumer, final Node property, final Node templateProperty) {
        if (!isXmlEqual(property, templateProperty)) {
            findingConsumer.accept(new PomValidationFinding("The " + this.pluginArtifactId
                    + "'s configuration-property " + propertyXpath + " has an illegal value.", () -> {
                        final Node importedProperty = plugin.getOwnerDocument().importNode(templateProperty, true);
                        property.getParentNode().replaceChild(importedProperty, property);
                    }));
        }
    }

    private boolean isXmlEqual(final Node property1, final Node property2) {
        final Diff comparison = DiffBuilder.compare(property1).withTest(property2).ignoreComments().ignoreWhitespace()
                .checkForSimilar().withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)).build();
        return !comparison.hasDifferences();
    }
}
