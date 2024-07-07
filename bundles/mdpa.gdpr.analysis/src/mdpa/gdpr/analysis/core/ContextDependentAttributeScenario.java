package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DFDGDPRTransposeFlowGraph;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.GDPRContextElement;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

import java.util.List;

public class ContextDependentAttributeScenario {
    private final TransformationManager transformationManager;

    private final List<PropertyValue> propertyValues;
    private final List<ContextDefinition> context;
    private final ContextDependentAttributeSource contextDependentAttributeSource;
    private final boolean resolvedUncertainty;

    public ContextDependentAttributeScenario(ContextAnnotation contextAnnotation, ContextDependentAttributeSource contextDependentAttributeSource) {
        this.transformationManager = new TransformationManager();
        this.propertyValues = contextAnnotation.getPropertyvalue();
        this.context = contextAnnotation.getContextdefinition();
        this.contextDependentAttributeSource = contextDependentAttributeSource;
        this.resolvedUncertainty = false;
    }

    public ContextDependentAttributeScenario(PropertyValue propertyValue, ContextDependentAttributeSource contextDependentAttributeSource) {
        this.transformationManager = new TransformationManager();
        this.propertyValues = List.of(propertyValue);
        this.context = List.of();
        this.contextDependentAttributeSource = contextDependentAttributeSource;
        this.resolvedUncertainty = true;
    }

    public boolean applicable(DFDGDPRVertex vertex) {
        return this.context.stream()
                .anyMatch(it -> it.getGdprElements().stream()
                        .map(GDPRContextElement::getGdprElement)
                        .allMatch(el -> vertex.getRelatedElements().contains(el)));
    }

    public boolean applicable(DFDGDPRTransposeFlowGraph transposeFlowGraph) {
        List<AbstractGDPRElement> relatedElements = transposeFlowGraph.getVertices().stream()
                .filter(DFDGDPRVertex.class::isInstance)
                .map(DFDGDPRVertex.class::cast)
                .map(DFDGDPRVertex::getRelatedElements)
                .flatMap(List::stream)
                .toList();
        return this.context.stream()
                .anyMatch(it -> it.getGdprElements().stream()
                        .map(GDPRContextElement::getGdprElement)
                        .allMatch(relatedElements::contains));
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
}
