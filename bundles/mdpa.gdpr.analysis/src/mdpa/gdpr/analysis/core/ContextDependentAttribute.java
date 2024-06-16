package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

import java.util.List;
import java.util.Optional;

/**
 * Context Definition applicable: Must contain all elements
 * Context Annotation applicable: One Context Definition applicable; Property has referenced property value
 * Property Annotates GDPR Elements with Context Annotations
 */
public class ContextDependentAttribute {
    private final TransformationManager transformationManager;
    
    private final AbstractGDPRElement referencedElement;
    
    private final Property propertyType;
    private final List<PropertyValue> propertyValues;
    private final List<ContextDefinition> context;

    public ContextDependentAttribute(PropertyAnnotation propertyAnnotation, ContextAnnotation contextAnnotation) {
        this.transformationManager = new TransformationManager();
        this.referencedElement = propertyAnnotation.getAnnotatedElement();
        this.propertyType = propertyAnnotation.getProperty();
        this.propertyValues = contextAnnotation.getPropertyvalue();
        this.context = contextAnnotation.getContextdefinition();
    }

    public boolean matches(DFDGDPRVertex vertex) {
    	Optional<AbstractGDPRElement> referencedGDPRElement = this.transformationManager.getElement(vertex.getReferencedElement());
        if (referencedGDPRElement.isEmpty() || !referencedGDPRElement.get().equals(this.referencedElement)) {
            return false;
        }
        return true;
    }
    
    public boolean matchesContext(DFDGDPRVertex vertex) {
    	return this.matches(vertex) && this.context.stream().anyMatch(context -> this.matchesContext(vertex, context));
    }

    private boolean matchesContext(DFDGDPRVertex vertex, ContextDefinition contextDefinition) {
        if(!contextDefinition.getGdprElements().stream().allMatch(it -> vertex.getRelatedElements().contains(it.getGdprElement()))) {
        	return false;
        }
        // Are nested context dependent properties possible/nececarry?
        return true;
    }
    
    public AbstractGDPRElement getReferencedElement() {
		return this.referencedElement;
	}
    
    public Property getPropertyType() {
		return this.propertyType;
	}
    
    public List<PropertyValue> getPropertyValues() {
		return this.propertyValues;
	}
}
