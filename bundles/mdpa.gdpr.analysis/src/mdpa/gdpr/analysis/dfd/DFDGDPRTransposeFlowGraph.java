package mdpa.gdpr.analysis.dfd;

import mdpa.gdpr.analysis.core.ContextDependentAttributeScenario;
import mdpa.gdpr.analysis.core.ContextDependentAttributeSource;
import mdpa.gdpr.metamodel.GDPR.Data;
import mdpa.gdpr.metamodel.GDPR.Role;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behaviour;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DFDGDPRTransposeFlowGraph extends DFDTransposeFlowGraph {
	private final Logger logger = Logger.getLogger(DFDGDPRTransposeFlowGraph.class);
	private final List<ContextDependentAttributeSource> relevantContextDependentAttributes;
	
    private final List<ContextDependentAttributeScenario> contextDependentAttributes;

    /**
     * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
     *
     * @param sink Sink vertex that induces the transpose flow graph
     */
    public DFDGDPRTransposeFlowGraph(AbstractVertex<?> sink, List<ContextDependentAttributeSource> contextDependentAttributes) {
        super(sink);
        this.relevantContextDependentAttributes = contextDependentAttributes;
		this.contextDependentAttributes = new ArrayList<>();
    }

	/**
	 * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
	 *
	 * @param sink Sink vertex that induces the transpose flow graph
	 */
	public DFDGDPRTransposeFlowGraph(AbstractVertex<?> sink, List<ContextDependentAttributeSource> contextDependentAttributes, List<ContextDependentAttributeScenario> contextDependentAttributeScenarios) {
		super(sink);
		this.relevantContextDependentAttributes = contextDependentAttributes;
		this.contextDependentAttributes = contextDependentAttributeScenarios;
	}

    public List<DFDGDPRTransposeFlowGraph> determineAlternateFlowGraphs() {
    	List<DFDGDPRTransposeFlowGraph> result = new ArrayList<>();
    	for(ContextDependentAttributeSource source : this.relevantContextDependentAttributes) {
			for (ContextDependentAttributeScenario scenario : source.getContextDependentAttributeScenarios()) {
				Optional<DFDGDPRVertex> matchingVertex = this.getVertices().stream()
						.filter(DFDGDPRVertex.class::isInstance)
						.map(DFDGDPRVertex.class::cast)
						.filter(source::applicable)
						.findAny();
				if (matchingVertex.isEmpty()) {
					logger.warn("Could not find matching vertex for context dependent attribute");
					continue;
				}

				if (source.getAnnotatedElement() instanceof Role || source.getAnnotatedElement() instanceof Data) {
					// Insert Data Characteristic
					DFDGDPRVertex targetVertex = matchingVertex.get();
					Node replacingNode = EcoreUtil.copy(targetVertex.getReferencedElement());
					Optional<Assignment> assignment = Optional.empty();
					if (!targetVertex.getReferencedElement().getBehaviour().getOutPin().isEmpty()) {
						assignment = Optional.of(datadictionaryFactory.eINSTANCE.createAssignment());
						assignment.get().setTerm(datadictionaryFactory.eINSTANCE.createTRUE());
						assignment.get().getInputPins().addAll(targetVertex.getReferencedElement().getBehaviour().getInPin());
						assignment.get().setOutputPin(targetVertex.getReferencedElement().getBehaviour().getOutPin().get(0));
					}

					LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
					type.setEntityName(source.getPropertyType().getEntityName());
					type.setId(source.getPropertyType().getId());
					for(PropertyValue propertyValue : scenario.getPropertyValues()) {
						Label value = datadictionaryFactory.eINSTANCE.createLabel();
						value.setEntityName(propertyValue.getEntityName());
						value.setId(propertyValue.getId());
						type.getLabel().add(value);
						assignment.ifPresent(assignment1 -> assignment1.getOutputLabels().add(value));
					}

					assignment.ifPresent(assignment1 -> replacingNode.getBehaviour().getAssignment().add(assignment1));
					DFDGDPRVertex replacingVertex = this.copyVertex(targetVertex, replacingNode);
					replacingVertex.setContextDependentAttributes(List.of(scenario));
					Map<DFDVertex, DFDVertex> mapping = new IdentityHashMap<>();
					mapping.put(targetVertex, replacingVertex);
					// TODO: Copy add scenario to tfg
					result.add((DFDGDPRTransposeFlowGraph) this.copy(mapping, List.of(scenario)));
				} else {
					// Insert Node Characteristic
					DFDGDPRVertex targetVertex = matchingVertex.get();
					Node replacingNode = EcoreUtil.copy(targetVertex.getReferencedElement());

					LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
					type.setEntityName(source.getPropertyType().getEntityName());
					type.setId(source.getPropertyType().getId());
					for(PropertyValue propertyValue : scenario.getPropertyValues()) {
						Label value = datadictionaryFactory.eINSTANCE.createLabel();
						value.setEntityName(propertyValue.getEntityName());
						value.setId(propertyValue.getId());
						type.getLabel().add(value);
						replacingNode.getProperties().add(value);
					}

					DFDGDPRVertex replacingVertex = this.copyVertex(targetVertex, replacingNode);
					replacingVertex.setContextDependentAttributes(List.of(scenario));
					Map<DFDVertex, DFDVertex> mapping = new IdentityHashMap<>();
					mapping.put(targetVertex, replacingVertex);
					// TODO: Copy add scenario to tfg
					result.add((DFDGDPRTransposeFlowGraph) this.copy(mapping, List.of(scenario)));

				}
			}
    	}
        return result;
    }
	@Override
  	public AbstractTransposeFlowGraph evaluate() {
		 if (!(this.sink instanceof DFDGDPRVertex dfdSink)) {
			 logger.error("Stored sink of DFD Transpose flow graph is not a DFDVertex");
			 throw new IllegalStateException("Stored sink of DFD Transpose flow graph is not a DFD Vertex");
		 }
		 DFDGDPRVertex newSink = dfdSink.copy(new IdentityHashMap<>());
		 newSink.unify(new HashSet<>());
		 newSink.evaluateDataFlow();
		 return new DFDGDPRTransposeFlowGraph(newSink, this.relevantContextDependentAttributes, this.contextDependentAttributes);
	}
    
    public List<ContextDependentAttributeSource> getContextDependentAttributeSources() {
		return this.relevantContextDependentAttributes;
	}
    
    /**
     * Copies the dfd vertex with the given replacing node
     * @param vertex DFD vertex of which the pin to vertex and pin to flow map should be copied
     * @param replacingElement Referenced node by the new dfd vertex
     * @return Returns a new dfd vertex with the given node and maps of the given vertex
     */
    private DFDGDPRVertex copyVertex(DFDGDPRVertex vertex, Node replacingElement) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        vertex.getPinDFDVertexMap().keySet().forEach(key -> copiedPinDFDVertexMap.put(key, vertex.getPinDFDVertexMap().get(key).copy(new HashMap<>())));
        return new DFDGDPRVertex(replacingElement, copiedPinDFDVertexMap, new HashMap<>(vertex.getPinFlowMap()), new ArrayList<>(vertex.getRelatedElements()));
    }

	@Override
	public AbstractTransposeFlowGraph copy(Map<DFDVertex, DFDVertex> mapping) {
		DFDGDPRVertex copiedSink = (DFDGDPRVertex) mapping.getOrDefault((DFDVertex) sink, ((DFDGDPRVertex) sink).copy(mapping));
		copiedSink.unify(new HashSet<>());
		return new DFDGDPRTransposeFlowGraph(copiedSink, this.relevantContextDependentAttributes, this.contextDependentAttributes);
	}

	public AbstractTransposeFlowGraph copy(Map<DFDVertex, DFDVertex> mapping, List<ContextDependentAttributeScenario> scenarios) {
		DFDGDPRVertex copiedSink = (DFDGDPRVertex) mapping.getOrDefault((DFDVertex) sink, ((DFDGDPRVertex) sink).copy(mapping));
		copiedSink.unify(new HashSet<>());
		return new DFDGDPRTransposeFlowGraph(copiedSink, this.relevantContextDependentAttributes, scenarios);
	}
}
