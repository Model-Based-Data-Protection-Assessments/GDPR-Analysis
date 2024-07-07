package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;

import java.util.Collection;
import java.util.List;

public class ContextDependentAttributeSource {
    private final AbstractGDPRElement annotatedElement;
    private final Property propertyType;
    private final List<ContextDependentAttributeScenario> contextDependentAttributeScenarios;

    public ContextDependentAttributeSource(PropertyAnnotation propertyAnnotation) {
        this.annotatedElement = propertyAnnotation.getAnnotatedElement();
        this.propertyType = propertyAnnotation.getProperty();
        if (!propertyAnnotation.getContextannotation().isEmpty()) {
            this.contextDependentAttributeScenarios = propertyAnnotation.getContextannotation().stream()
                    .map(it -> new ContextDependentAttributeScenario(it, this))
                    .toList();
        } else {
            this.contextDependentAttributeScenarios = propertyAnnotation.getProperty().getPropertyvalue().stream()
                    .map(it -> new ContextDependentAttributeScenario(it, this))
                    .toList();
        }
    }

    public boolean applicable(Collection<DFDGDPRVertex> vertices) {
        return vertices.stream()
                .anyMatch(it -> it.getRelatedElements().contains(this.annotatedElement));
    }

    public boolean applicable(DFDGDPRVertex vertex) {
        return vertex.getRelatedElements().contains(this.annotatedElement);
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
}
