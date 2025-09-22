package mdpa.gdpr.analysis.core;

import java.util.Collection;
import java.util.List;
import mdpa.gdpr.analysis.UncertaintyUtils;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

/**
 * This class models an application of a context dependent attribute on an element in the GDPR model.
 * The different values it can take are saved in one or multiple child {@link ContextDependentAttributeScenario}.
 */
public class ContextDependentAttributeSource {
    private final String name;
    private final AbstractGDPRElement annotatedElement;
    private final Property propertyType;
    private final List<ContextDependentAttributeScenario> contextDependentAttributeScenarios;

    private final List<ContextDefinition> context;
    private final List<ContextDependentAttributeSource> sources;
    private final boolean resolvedUncertainty;

    /**
     * Creates a new {@link ContextDependentAttributeSource} with the given property annotation containing the information about the annotated element and value
     * and the context annotation describing the context of the {@link ContextDependentAttributeSource}
     * @param propertyAnnotation {@link PropertyAnnotation} describing where the CDA is applied
     * @param contextAnnotation {@link ContextAnnotation} describing which scenarios the source has
     */
    public ContextDependentAttributeSource(PropertyAnnotation propertyAnnotation, ContextAnnotation contextAnnotation) {
        this.name = contextAnnotation.getEntityName() + "@" + propertyAnnotation.getEntityName();
        this.annotatedElement = propertyAnnotation.getAnnotatedElement();
        this.propertyType = propertyAnnotation.getProperty();
        this.contextDependentAttributeScenarios = List.of(new ContextDependentAttributeScenario(contextAnnotation, this));
        this.context = contextAnnotation.getContextdefinition();
        this.sources = List.of();
        this.resolvedUncertainty = false;
    }

    /**
     * Creates a new {@link ContextDependentAttributeSource} that needs to be resolved with uncertain CDAs.
     * Resolves an uncertainty regarding the value of an {@link ContextDependentAttributeSource} by creating a scenario for each
     * passed {@link PropertyValue}.
     * Additionally, the given list of other {@link ContextDependentAttributeSource} denotes where this source cannot apply
     * @param propertyAnnotation {@link PropertyAnnotation} containing information about the annotated element and value
     * @param values Different {@link PropertyValue} that are resolved by the uncertainty
     * @param sources List of {@link ContextDependentAttributeSource} that cannot be applied at the same time
     */
    public ContextDependentAttributeSource(PropertyAnnotation propertyAnnotation, List<PropertyValue> values,
            List<ContextDependentAttributeSource> sources) {
        this.name = "Unknown@" + propertyAnnotation.getEntityName();
        this.annotatedElement = propertyAnnotation.getAnnotatedElement();
        this.propertyType = propertyAnnotation.getProperty();
        this.contextDependentAttributeScenarios = values.stream()
                .map(it -> new ContextDependentAttributeScenario(it, this, sources))
                .toList();
        this.context = List.of();
        this.sources = sources;
        this.resolvedUncertainty = true;
    }

    /**
     * Determines whether this {@link ContextDependentAttributeSource} is applicable to the given list of vertices
     * @param vertices Given list of vertices
     * @return Returns true, if this {@link ContextDependentAttributeSource} is applicable at least one of the vertices
     *          Otherwise, the method returns false.
     */
    public boolean applicable(Collection<DFDGDPRVertex> vertices) {
        if (!vertices.stream()
                .map(DFDGDPRVertex::getRelatedElements)
                .flatMap(List::stream)
                .toList()
                .contains(this.annotatedElement)) {
            return false;
        }
        return vertices.stream()
                .anyMatch(this::applicable);
    }

    /**
     * Determines whether the {@link ContextDependentAttributeSource} is applicable at the given vertex.
     * <p/>
     * This is the case, it the vertex has the annotated element in its context.
     * If this {@link ContextDependentAttributeSource} is resolving an uncertainty, the other saved sources must not match.
     * If this source is not resolving an uncertainty, it must match at least one context definition
     * @param vertex Given {@link DFDGDPRVertex} that is checked
     * @return Returns true, if the source is applicable to the vertex.
     *          Otherwise, the method returns false
     */
    public boolean applicable(DFDGDPRVertex vertex) {
        if (!vertex.getRelatedElements()
                .contains(this.annotatedElement)) {
            return false;
        }
        if (this.resolvedUncertainty) {
            return this.sources.stream()
                    .noneMatch(it -> it.applicable(vertex));
        }
        return this.context.stream()
                .anyMatch(it -> UncertaintyUtils.matchesContextDefinition(vertex, it));
    }

    /**
     * Returns the property type that will be applied if the source is applicable
     * @return Returns the applied {@link Property}
     */
    public Property getPropertyType() {
        return propertyType;
    }

    /**
     * Returns the different possible {@link ContextDependentAttributeScenario} of this source
     * @return List of possible {@link ContextDependentAttributeScenario}
     */
    public List<ContextDependentAttributeScenario> getContextDependentAttributeScenarios() {
        return contextDependentAttributeScenarios;
    }

    /**
     * Returns the {@link AbstractGDPRElement} the source is annotated to
     * @return Annotated {@link AbstractGDPRElement}
     */
    public AbstractGDPRElement getAnnotatedElement() {
        return annotatedElement;
    }

    /**
     * Returns the name of the {@link ContextDependentAttributeSource}
     * @return Returns the name of the source
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
