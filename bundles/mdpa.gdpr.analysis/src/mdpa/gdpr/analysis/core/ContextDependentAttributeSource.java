package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.UncertaintyUtils;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.GDPRContextElement;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;

import java.util.Collection;
import java.util.List;

public class ContextDependentAttributeSource {
	private final String name;
    private final AbstractGDPRElement annotatedElement;
    private final Property propertyType;
    private final List<ContextDependentAttributeScenario> contextDependentAttributeScenarios;

    private final List<ContextDefinition> context;
    private final List<ContextDependentAttributeSource> sources;
    private final boolean resolvedUncertainty;

    public ContextDependentAttributeSource(PropertyAnnotation propertyAnnotation, ContextAnnotation contextAnnotation) {
    	this.name = contextAnnotation.getEntityName() + "@" + propertyAnnotation.getEntityName();
        this.annotatedElement = propertyAnnotation.getAnnotatedElement();
        this.propertyType = propertyAnnotation.getProperty();
        this.contextDependentAttributeScenarios = List.of(new ContextDependentAttributeScenario(contextAnnotation, this));
        this.context = contextAnnotation.getContextdefinition();
        this.sources = List.of();
        this.resolvedUncertainty = false;
    }
    
    public ContextDependentAttributeSource(PropertyAnnotation propertyAnnotation, List<PropertyValue> values, List<ContextDependentAttributeSource> sources) {
    	this.name = "Unknown@" +  propertyAnnotation.getEntityName();
        this.annotatedElement = propertyAnnotation.getAnnotatedElement();
        this.propertyType = propertyAnnotation.getProperty();
        this.contextDependentAttributeScenarios = values.stream()
        		.map(it -> new ContextDependentAttributeScenario(it, this, sources))
        		.toList();
        this.context = List.of();
        this.sources = sources;
        this.resolvedUncertainty = true;
    }

    public boolean applicable(Collection<DFDGDPRVertex> vertices) {
    	if (!vertices.stream().map(it -> it.getRelatedElements()).flatMap(List::stream).toList().contains(this.annotatedElement)) {
    		return false;
    	}
    	return vertices.stream().anyMatch(it -> this.applicable(it));
    }

    public boolean applicable(DFDGDPRVertex vertex) {
    	if (!vertex.getRelatedElements().contains(this.annotatedElement)) {
    		return false;
    	}
    	if (this.resolvedUncertainty) {
    		return this.sources.stream().noneMatch(it -> it.applicable(vertex));
    	}
        return this.context.stream()
                .anyMatch(it -> UncertaintyUtils.matchesContextDefinition(vertex, it));
    }

    public Property getPropertyType() {
        return propertyType;
    }

    public List<ContextDependentAttributeScenario> getContextDependentAttributeScenarios() {
        return contextDependentAttributeScenarios;
    }

    public AbstractGDPRElement getAnnotatedElement() {
        return annotatedElement;
    }
    
    public String getName() {
		return name;
	}
    
    @Override
    public String toString() {
    	return this.getName();
    }
}
