package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.UncertaintyUtils;
import mdpa.gdpr.analysis.dfd.DFDGDPRTransposeFlowGraph;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.GDPRContextElement;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;
import org.apache.log4j.Logger;

import java.util.List;

public class ContextDependentAttributeScenario {
    private final Logger logger = Logger.getLogger(ContextDependentAttributeScenario.class);

	private final String name;
    private final TransformationManager transformationManager;

    private final List<PropertyValue> propertyValues;
    private final List<ContextDefinition> context;
    private final List<ContextDependentAttributeSource> sources;
    private final ContextDependentAttributeSource contextDependentAttributeSource;
    private final boolean resolvedUncertainty;

    public ContextDependentAttributeScenario(ContextAnnotation contextAnnotation, ContextDependentAttributeSource contextDependentAttributeSource) {
    	this.name = contextAnnotation.getEntityName();
    	this.transformationManager = new TransformationManager();
        this.propertyValues = contextAnnotation.getPropertyvalue();
        this.context = contextAnnotation.getContextdefinition();
        this.sources = List.of();
        this.contextDependentAttributeSource = contextDependentAttributeSource;
        this.resolvedUncertainty = false;
    }

    public ContextDependentAttributeScenario(PropertyValue propertyValue, ContextDependentAttributeSource contextDependentAttributeSource, List<ContextDependentAttributeSource> sources) {
        this.name = propertyValue.getEntityName() + "@" + contextDependentAttributeSource.getName();
    	this.transformationManager = new TransformationManager();
        this.propertyValues = List.of(propertyValue);
        this.context = List.of();
        this.sources = sources;
        this.contextDependentAttributeSource = contextDependentAttributeSource;
        this.resolvedUncertainty = true;
    }

    public boolean applicable(DFDGDPRVertex vertex) {
        logger.info("Determining whether " + this.name + " can be applied to " + vertex);
    	if (this.resolvedUncertainty) {
            if (!this.contextDependentAttributeSource.applicable(vertex)) {
                return false;
            }
            logger.info("Context Depdendent Attribute Scenario is resolved with uncertainties!");
    		return this.sources.stream().noneMatch(it -> {
                logger.info("Should not match: " + it.getContextDependentAttributeScenarios().get(0).getName());
    			var scenario = it.getContextDependentAttributeScenarios().get(0);
                logger.info("Result: " + scenario.applicable(vertex));
    			return scenario.applicable(vertex);
    		});
    	}
        return this.context.stream()
                .anyMatch(it -> UncertaintyUtils.matchesContextDefinition(vertex, it));
    }

    public boolean applicable(DFDGDPRTransposeFlowGraph transposeFlowGraph) {
    	if (this.resolvedUncertainty) {
    		return this.sources.stream().noneMatch(it -> {
    			var scenario = it.getContextDependentAttributeScenarios().get(0);
    			return scenario.applicable(transposeFlowGraph);
    		});
    	}
    	return transposeFlowGraph.getVertices().stream()
    			.filter(DFDGDPRVertex.class::isInstance)
    			.map(DFDGDPRVertex.class::cast)
    			.anyMatch(it -> this.applicable(it));
    }

    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }

    public boolean resolvedByUncertainty() {
        return this.resolvedUncertainty;
    }

    public ContextDependentAttributeSource getContextDependentAttributeSource() {
        return contextDependentAttributeSource;
    }
    
    public String getName() {
		return name;
	}
}
