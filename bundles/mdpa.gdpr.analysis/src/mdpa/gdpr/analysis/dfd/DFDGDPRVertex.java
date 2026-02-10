package mdpa.gdpr.analysis.dfd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mdpa.gdpr.analysis.core.ContextDependentAttributeScenario;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.GDPR.Data;
import mdpa.gdpr.metamodel.GDPR.LegalBasis;
import mdpa.gdpr.metamodel.GDPR.Processing;
import mdpa.gdpr.metamodel.GDPR.Purpose;
import mdpa.gdpr.metamodel.GDPR.Role;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class DFDGDPRVertex extends DFDVertex {
    private final List<AbstractGDPRElement> relatedElements;
    private List<ContextDependentAttributeScenario> contextDependentAttributes;

    /**
     * Creates a new vertex with the given referenced node and pin mappings
     * @param node Node that is referenced by the vertex
     * @param pinDFDVertexMap Map containing relationships between the pins of the vertex and previous vertices
     * @param pinFlowMap Map containing relationships between the pins of the vertex and the flows connecting the node to
     * other vertices
     */
    public DFDGDPRVertex(Node node, Map<Pin, DFDVertex> pinDFDVertexMap, Map<Pin, Flow> pinFlowMap, List<AbstractGDPRElement> relatedElements) {
        super(node, pinDFDVertexMap, pinFlowMap);
        this.relatedElements = relatedElements;
        this.contextDependentAttributes = new ArrayList<>();
    }

    /**
     * Creates a clone of the vertex without considering data characteristics nor vertex characteristics
     */
    public DFDGDPRVertex copy(Map<DFDVertex, DFDVertex> mapping) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = this.copyPinDFDVertexMap(mapping);
        Map<Pin, Flow> copiedPinFlowMap = this.copyPinFlowMap(copiedPinDFDVertexMap);
        DFDGDPRVertex copy = new DFDGDPRVertex(this.referencedElement, copiedPinDFDVertexMap, copiedPinFlowMap,
                new ArrayList<>(this.relatedElements));
        if (!this.contextDependentAttributes.isEmpty()) {
            copy.setContextDependentAttributes(this.contextDependentAttributes);
        }
        return copy;
    }

    /**
     * Returns the Map from a pin on the vertex to the given predecessor {@link DFDVertex}, while adhering to the given mapping
     * @param mapping Mapping that should be applied to the mapping process
     * @return Returns a new copied pin to vertex map that adheres to the given mapping
     */
    private Map<Pin, DFDVertex> copyPinDFDVertexMap(Map<DFDVertex, DFDVertex> mapping) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        this.pinDFDVertexMap.keySet()
                .forEach(key -> copiedPinDFDVertexMap.put(key, mapping.getOrDefault(this.pinDFDVertexMap.get(key), this.pinDFDVertexMap.get(key)
                        .copy(mapping))));
        return copiedPinDFDVertexMap;
    }

    /**
     * Returns the Map from pin to outgoing flow that references the new correct DFD Vertex that is given by the mapping
     * @param pinDFDVertexMap Given mapping from pin to dfd vertex that each entry should respect
     * @return Returns a new map from pin to flow that is correct in the context of the given pin to vertex map
     */
    private Map<Pin, Flow> copyPinFlowMap(Map<Pin, DFDVertex> pinDFDVertexMap) {
        Map<Pin, Flow> copiedPinFlowMap = new HashMap<>();
        this.pinFlowMap.keySet()
                .forEach(key -> {
                    Pin correspondingPin = pinDFDVertexMap.get(key).getReferencedElement().getBehavior().getOutPin().stream()
                            .filter(it -> it.getEntityName().equals(key.getEntityName()))
                            .findAny()
                            .orElseThrow();
                    Flow flow = EcoreUtil.copy(this.pinFlowMap.get(key));
                    flow.setSourcePin(correspondingPin);
                    flow.setSourceNode(pinDFDVertexMap.get(key).getReferencedElement());
                    copiedPinFlowMap.put(key, flow);
                });
        return copiedPinFlowMap;
    }

    public void setContextDependentAttributes(List<ContextDependentAttributeScenario> contextDependentAttributes) {
        this.contextDependentAttributes = contextDependentAttributes;
    }

    public List<ContextDependentAttributeScenario> getContextDependentAttributes() {
        return this.contextDependentAttributes;
    }

    public List<AbstractGDPRElement> getRelatedElements() {
        return this.relatedElements;
    }

    public List<Data> getIncomingData() {
        return this.relatedElements.stream()
                .filter(Data.class::isInstance)
                .map(Data.class::cast)
                .toList();
    }

    public List<Data> getOutgoingData() {
        return this.relatedElements.stream()
                .filter(Data.class::isInstance)
                .map(Data.class::cast)
                .toList();
    }

    public List<Purpose> getPurpose() {
        return this.relatedElements.stream()
                .filter(Purpose.class::isInstance)
                .map(Purpose.class::cast)
                .toList();
    }

    public List<LegalBasis> getLegalBasis() {
        return this.relatedElements.stream()
                .filter(LegalBasis.class::isInstance)
                .map(LegalBasis.class::cast)
                .toList();
    }

    public Role getResponsibilityRole() {
        return this.relatedElements.stream()
                .filter(Processing.class::isInstance)
                .map(Processing.class::cast)
                .map(Processing::getResponsible)
                .findAny()
                .orElseThrow();
    }
}
