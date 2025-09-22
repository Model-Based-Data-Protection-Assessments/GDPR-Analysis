package mdpa.gdpr.analysis.core;

import java.util.List;
import mdpa.gdpr.analysis.UncertaintyUtils;
import mdpa.gdpr.analysis.dfd.DFDGDPRTransposeFlowGraph;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;
import org.apache.log4j.Logger;

/**
 * Models a Context Dependent Attribute Scenario that applies the given list of property values.
 * <p/>
 * As a Context Dependent Attribute Scenario can occur in two scenarios we differentiate:
 */
public class ContextDependentAttributeScenario {
    private final Logger logger = Logger.getLogger(ContextDependentAttributeScenario.class);

    private final String name;

    private final List<PropertyValue> propertyValues;
    private final List<ContextDefinition> context;
    private final List<ContextDependentAttributeSource> sources;
    private final ContextDependentAttributeSource contextDependentAttributeSource;
    private final boolean resolvedUncertainty;

    /**
     * Creates a new context dependent attribute scenario that matches a specific context. Therefore, it does not resolve an
     * uncertain CDA
     * @param contextAnnotation {@link ContextAnnotation} the Scenario requires
     * @param contextDependentAttributeSource Corresponding {@link ContextDependentAttributeSource}
     */
    public ContextDependentAttributeScenario(ContextAnnotation contextAnnotation, ContextDependentAttributeSource contextDependentAttributeSource) {
        this.name = contextAnnotation.getEntityName();
        this.propertyValues = contextAnnotation.getPropertyvalue();
        this.context = contextAnnotation.getContextdefinition();
        this.sources = List.of();
        this.contextDependentAttributeSource = contextDependentAttributeSource;
        this.resolvedUncertainty = false;
    }

    /**
     * Creates a new {@link ContextDependentAttributeScenario} that is resolving an uncertainty. Therefore, it requires a
     * property value that is applied, the corresponding {@link ContextDependentAttributeSource} and a list of other
     * {@link ContextDependentAttributeSource} that contradict the uncertain CDA
     * @param propertyValue Property value that is applied, when this scenario is applied
     * @param contextDependentAttributeSource Corresponding {@link ContextDependentAttributeSource}
     * @param sources Other {@link ContextDependentAttributeSource} that must not be true
     */
    public ContextDependentAttributeScenario(PropertyValue propertyValue, ContextDependentAttributeSource contextDependentAttributeSource,
            List<ContextDependentAttributeSource> sources) {
        this.name = propertyValue.getEntityName() + "@" + contextDependentAttributeSource.getName();
        this.propertyValues = List.of(propertyValue);
        this.context = List.of();
        this.sources = sources;
        this.contextDependentAttributeSource = contextDependentAttributeSource;
        this.resolvedUncertainty = true;
    }

    /**
     * Returns whether the {@link ContextDependentAttributeScenario} is applicable to the given vertex
     * @param vertex {@link DFDGDPRVertex} that is checked
     * @return Returns true, if the scenario should be applied to the vertex. Otherwise, the method returns false
     */
    public boolean applicable(DFDGDPRVertex vertex) {
        logger.trace("Determining whether " + this.name + " can be applied to " + vertex);
        if (this.resolvedUncertainty) {
            if (!this.contextDependentAttributeSource.applicable(vertex)) {
                return false;
            }
            logger.trace("Context Dependent Attribute Scenario is resolved with uncertainties!");
            return this.sources.stream()
                    .noneMatch(it -> {
                        logger.trace("Should not match: " + it.getContextDependentAttributeScenarios()
                                .get(0)
                                .getName());
                        var scenario = it.getContextDependentAttributeScenarios()
                                .get(0);
                        logger.trace("Result: " + scenario.applicable(vertex));
                        return scenario.applicable(vertex);
                    });
        }
        return this.context.stream()
                .anyMatch(it -> UncertaintyUtils.matchesContextDefinition(vertex, it));
    }

    /**
     * Determines whether the {@link ContextDependentAttributeScenario} is applicable to any of the nodes in the given
     * transpose flow graph
     * @param transposeFlowGraph {@link DFDGDPRTransposeFlowGraph} that is checked
     * @return Returns true, if the {@link ContextDependentAttributeScenario} can be applied to any of the vertices in the
     * TFG. Otherwise, the method returns false
     */
    public boolean applicable(DFDGDPRTransposeFlowGraph transposeFlowGraph) {
        if (this.resolvedUncertainty) {
            return this.sources.stream()
                    .noneMatch(it -> {
                        var scenario = it.getContextDependentAttributeScenarios()
                                .get(0);
                        return scenario.applicable(transposeFlowGraph);
                    });
        }
        return transposeFlowGraph.getVertices()
                .stream()
                .filter(DFDGDPRVertex.class::isInstance)
                .map(DFDGDPRVertex.class::cast)
                .anyMatch(this::applicable);
    }

    /**
     * Returns the property values that are applied, when this scenario is fulfilled
     * @return Returns a list of {@link PropertyValue} that are applied in the case the scenario is true
     */
    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }

    /**
     * Retrieves the {@link ContextDependentAttributeSource} that this scenario is a part of
     * @return Returns the parent {@link ContextDependentAttributeSource}
     */
    public ContextDependentAttributeSource getContextDependentAttributeSource() {
        return contextDependentAttributeSource;
    }

    /**
     * Returns the name of the {@link ContextDependentAttributeScenario}
     * @return The name of the {@link ContextDependentAttributeScenario}
     */
    public String getName() {
        return name;
    }
}
