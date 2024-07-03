package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.GDPRContextElement;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

import java.util.List;

public class ContextDependentAttributeScenario {
    private final TransformationManager transformationManager;

    private final List<PropertyValue> propertyValues;
    private final List<ContextDefinition> context;
    private final boolean resolvedUncertainty;

    public ContextDependentAttributeScenario(ContextAnnotation contextAnnotation) {
        this.transformationManager = new TransformationManager();
        this.propertyValues = contextAnnotation.getPropertyvalue();
        this.context = contextAnnotation.getContextdefinition();
        this.resolvedUncertainty = true;
    }

    public ContextDependentAttributeScenario(PropertyValue propertyValue) {
        this.transformationManager = new TransformationManager();
        this.propertyValues = List.of(propertyValue);
        this.context = List.of();
        this.resolvedUncertainty = false;
    }

    public boolean applicable(DFDGDPRVertex vertex) {
        return this.context.stream()
                .anyMatch(it -> it.getGdprElements().stream()
                        .map(GDPRContextElement::getGdprElement)
                        .allMatch(el -> vertex.getRelatedElements().contains(el)));
    }

    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }

    public boolean resolvedByUncertainty() {
        return this.resolvedUncertainty;
    }
}
