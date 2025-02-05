package mdpa.gdpr.analysis.dfd;

import mdpa.gdpr.analysis.UncertaintyUtils;
import mdpa.gdpr.analysis.core.ContextAttributeState;
import mdpa.gdpr.analysis.core.ContextDependentAttributeScenario;
import mdpa.gdpr.analysis.core.ContextDependentAttributeSource;
import mdpa.gdpr.metamodel.GDPR.Data;
import mdpa.gdpr.metamodel.GDPR.NaturalPerson;
import mdpa.gdpr.metamodel.GDPR.PersonalData;
import mdpa.gdpr.metamodel.GDPR.Role;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.core.entity.ResourceProvidedRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class DFDGDPRTransposeFlowGraph extends DFDTransposeFlowGraph {
	private final Logger logger = Logger.getLogger(DFDGDPRTransposeFlowGraph.class);
	private final List<ContextDependentAttributeSource> relevantContextDependentAttributes;
	private final DataDictionary dd;
	
    private final Optional<ContextAttributeState> contextAttributeState;

    /**
     * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
     *
     * @param sink Sink vertex that induces the transpose flow graph
     */
    public DFDGDPRTransposeFlowGraph(AbstractVertex<?> sink, List<ContextDependentAttributeSource> contextDependentAttributes, DataDictionary dd) {
        super(sink);
        this.relevantContextDependentAttributes = contextDependentAttributes;
		this.contextAttributeState = Optional.empty();
		this.dd = dd;
    }

	/**
	 * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
	 *
	 * @param sink Sink vertex that induces the transpose flow graph
	 */
	public DFDGDPRTransposeFlowGraph(AbstractVertex<?> sink, List<ContextDependentAttributeSource> contextDependentAttributes, ContextAttributeState contextAttributeState, DataDictionary dd) {
		super(sink);
		this.relevantContextDependentAttributes = contextDependentAttributes;
		this.contextAttributeState = Optional.of(contextAttributeState);
		this.dd = dd;
	}

    public List<DFDGDPRTransposeFlowGraph> determineAlternateFlowGraphs() {
    	List<DFDGDPRTransposeFlowGraph> result = new ArrayList<>();
		List<ContextAttributeState> states = ContextAttributeState.createAllContextAttributeStates(this.relevantContextDependentAttributes);
		Map<List<ContextDependentAttributeScenario>, List<PropertyValue>> unmatchedStates = new HashMap<>();
    	for(ContextAttributeState state : states) {
			if (state.getSelectedScenarios().stream().noneMatch(it -> it.applicable(this))) {
				logger.warn("State not applicable to transpose flow graph, skipping");
				continue;
			}
			DFDGDPRTransposeFlowGraph currentTransposeFlowGraph = (DFDGDPRTransposeFlowGraph) this.copy(new HashMap<>(), state);
			
			for (ContextDependentAttributeScenario scenario : state.getSelectedScenarios()) {
				ContextDependentAttributeSource source = scenario.getContextDependentAttributeSource();
				Optional<DFDGDPRVertex> matchingVertex = currentTransposeFlowGraph.getVertices().stream()
						.filter(DFDGDPRVertex.class::isInstance)
						.map(DFDGDPRVertex.class::cast)
						.filter(source::applicable)
						.findFirst();
				if (matchingVertex.isEmpty()) {
					logger.warn("Could not find matching vertex for context dependent attribute");
					continue;
				}
				if (!scenario.applicable(matchingVertex.get())) {
					// Scenario must not be resolved by uncertainty
					logger.warn("Scenario not applicable to vertex!");
					continue;
				}

				if (source.getAnnotatedElement() instanceof NaturalPerson person) {
					// Insert Data Characteristic
					List<DFDGDPRVertex> targetedVertices = currentTransposeFlowGraph.getVertices().stream()
							.filter(DFDGDPRVertex.class::isInstance)
							.map(DFDGDPRVertex.class::cast)
							.filter(scenario::applicable)
							.filter(vertex -> {
								var previousVertices = vertex.getPreviousElements();
								boolean reappeared = previousVertices.stream()
										.filter(DFDGDPRVertex.class::isInstance)
										.map(DFDGDPRVertex.class::cast)
										.noneMatch(scenario::applicable);
								boolean contextChanged = previousVertices.stream()
										.filter(DFDGDPRVertex.class::isInstance)
										.map(DFDGDPRVertex.class::cast)
										.noneMatch(it -> it.getResponsibilityRole().equals(vertex.getResponsibilityRole()));
								return reappeared || contextChanged;
							})
							.toList();
					
					for (DFDGDPRVertex targetVertex : targetedVertices) {
						DFDGDPRVertex currentTargetVertex = currentTransposeFlowGraph.getVertices().stream()
								.filter(DFDGDPRVertex.class::isInstance)
								.map(DFDGDPRVertex.class::cast)
								.filter(it -> it.getReferencedElement().getId().equals(targetVertex.getReferencedElement().getId()))
								.filter(it -> it.getReferencedElement().getEntityName().equals(targetVertex.getReferencedElement().getEntityName()))
								.findAny().orElseThrow();
						DFDGDPRVertex impactedElement = currentTargetVertex.getPreviousElements().stream()
								.filter(DFDGDPRVertex.class::isInstance)
								.map(DFDGDPRVertex.class::cast)
								.filter(it -> {
									return it.getOutgoingData().stream()
											.filter(PersonalData.class::isInstance)
											.map(PersonalData.class::cast)
											.anyMatch(data -> data.getDataReferences().contains(person));
								})
								.findAny().orElse(currentTargetVertex);
						Behavior replacingBehavior = UncertaintyUtils.createBehavior(impactedElement, dd, source, scenario, person);
						Node replacingNode = EcoreUtil.copy(impactedElement.getReferencedElement());
						replacingNode.setBehavior(replacingBehavior);
						DFDGDPRVertex replacingVertex = this.copyVertex(impactedElement, replacingNode);
						List<ContextDependentAttributeScenario> scenarios = new ArrayList<>(impactedElement.getContextDependentAttributes());
						scenarios.add(scenario);
						replacingVertex.setContextDependentAttributes(scenarios);
						Map<DFDVertex, DFDVertex> mapping = new HashMap<>();
						mapping.put(impactedElement, replacingVertex);
						currentTransposeFlowGraph = (DFDGDPRTransposeFlowGraph) currentTransposeFlowGraph.copy(mapping, state);
					}
					
				} else if (source.getAnnotatedElement() instanceof Data data) {
					// Insert Data Characteristic
					List<DFDGDPRVertex> targetedVertices = currentTransposeFlowGraph.getVertices().stream()
							.filter(DFDGDPRVertex.class::isInstance)
							.map(DFDGDPRVertex.class::cast)
							.filter(scenario::applicable)
							.filter(vertex -> {
								var previousVertices = vertex.getPreviousElements();
								boolean reappeared = previousVertices.stream()
										.filter(DFDGDPRVertex.class::isInstance)
										.map(DFDGDPRVertex.class::cast)
										.noneMatch(scenario::applicable);
								boolean contextChanged = previousVertices.stream()
										.filter(DFDGDPRVertex.class::isInstance)
										.map(DFDGDPRVertex.class::cast)
										.noneMatch(it -> it.getResponsibilityRole().equals(vertex.getResponsibilityRole()));
								return reappeared || contextChanged;
							})
							.toList();
					
					for (DFDGDPRVertex targetVertex : targetedVertices) {
						DFDGDPRVertex currentTargetVertex = currentTransposeFlowGraph.getVertices().stream()
								.filter(DFDGDPRVertex.class::isInstance)
								.map(DFDGDPRVertex.class::cast)
								.filter(it -> it.getReferencedElement().getId().equals(targetVertex.getReferencedElement().getId()))
								.filter(it -> it.getReferencedElement().getEntityName().equals(targetVertex.getReferencedElement().getEntityName()))
								.findAny().orElseThrow();
						DFDGDPRVertex impactedElement = currentTargetVertex.getPreviousElements().stream()
								.filter(DFDGDPRVertex.class::isInstance)
								.map(DFDGDPRVertex.class::cast)
								.filter(it -> it.getOutgoingData().contains(data))
								.findAny().orElse(currentTargetVertex);
						Behavior replacingBehavior = UncertaintyUtils.createBehavior(impactedElement, dd, source, scenario, data);
						Node replacingNode = EcoreUtil.copy(impactedElement.getReferencedElement());
						replacingNode.setBehavior(replacingBehavior);
						DFDGDPRVertex replacingVertex = this.copyVertex(impactedElement, replacingNode);
						List<ContextDependentAttributeScenario> scenarios = new ArrayList<>(impactedElement.getContextDependentAttributes());
						scenarios.add(scenario);
						replacingVertex.setContextDependentAttributes(scenarios);
						Map<DFDVertex, DFDVertex> mapping = new HashMap<>();
						mapping.put(impactedElement, replacingVertex);
						currentTransposeFlowGraph = (DFDGDPRTransposeFlowGraph) currentTransposeFlowGraph.copy(mapping, state);
					}
				} else {
					// Insert Node Characteristics at all matching vertices
					List<String> matchingVertices = currentTransposeFlowGraph.getVertices().stream()
							.filter(DFDGDPRVertex.class::isInstance)
							.map(DFDGDPRVertex.class::cast)
							.filter(it -> source.applicable(it))
							.filter(it -> scenario.applicable(it))
							.map(it -> it.getReferencedElement().getId())
							.toList();
					for (String targetVertexID : matchingVertices) {
						DFDGDPRVertex targetVertex = currentTransposeFlowGraph.getVertices().stream()
								.filter(DFDGDPRVertex.class::isInstance)
								.map(DFDGDPRVertex.class::cast)
								.filter(it -> it.getReferencedElement().getId().equals(targetVertexID))
								.findFirst().orElseThrow();
						Node replacingNode = EcoreUtil.copy(targetVertex.getReferencedElement());

						LabelType labelType = dd.getLabelTypes().stream()
								.filter(it -> it.getEntityName().equals(source.getPropertyType().getEntityName()))
								.findAny().orElseThrow();
						List<Label> labels = new ArrayList<>();
						for(PropertyValue propertyValue : scenario.getPropertyValues()) {
							Label label = labelType.getLabel().stream()
									.filter(it -> it.getEntityName().equals(propertyValue.getEntityName()))
									.findAny().orElseThrow();
							labels.add(label);
						}
						replacingNode.getProperties().addAll(labels);

						DFDGDPRVertex replacingVertex = this.copyVertex(targetVertex, replacingNode);
						List<ContextDependentAttributeScenario> scenarios = new ArrayList<>(targetVertex.getContextDependentAttributes());
						scenarios.add(scenario);
						replacingVertex.setContextDependentAttributes(scenarios);
						Map<DFDVertex, DFDVertex> mapping = new HashMap<>();
						mapping.put(targetVertex, replacingVertex);
						currentTransposeFlowGraph = (DFDGDPRTransposeFlowGraph) currentTransposeFlowGraph.copy(mapping, state);
					}
				}
			}
			result.add(currentTransposeFlowGraph);
    	}
        return result;
    }
    
	@Override
  	public AbstractTransposeFlowGraph evaluate() {
		 if (!(this.sink instanceof DFDGDPRVertex dfdSink)) {
			 logger.error("Stored sink of DFD Transpose flow graph is not a DFDVertex");
			 throw new IllegalStateException("Stored sink of DFD Transpose flow graph is not a DFD Vertex");
		 }
		 if (this.contextAttributeState.isEmpty()) {
			 logger.error("Before evaluating the data flow, alternative flow graphs need to be created!");
			 throw new IllegalStateException();
		 }
		 DFDGDPRVertex newSink = dfdSink.copy(new IdentityHashMap<>());
		 newSink.unify(new HashSet<>());
		 newSink.evaluateDataFlow();
		 return new DFDGDPRTransposeFlowGraph(newSink, this.relevantContextDependentAttributes, this.contextAttributeState.get(), this.dd);
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
		DFDGDPRVertex copiedSink;
		if (mapping.containsKey(this.sink)) {
			copiedSink = (DFDGDPRVertex) mapping.get(this.sink);
		} else {
			copiedSink = ((DFDGDPRVertex) sink).copy(mapping);
		}
		copiedSink.unify(new HashSet<>());
        return this.contextAttributeState.map(attributeState -> new DFDGDPRTransposeFlowGraph(copiedSink, this.relevantContextDependentAttributes, attributeState, this.dd)).orElseGet(() -> new DFDGDPRTransposeFlowGraph(copiedSink, this.relevantContextDependentAttributes, this.dd));
	}

	public AbstractTransposeFlowGraph copy(Map<DFDVertex, DFDVertex> mapping, ContextAttributeState contextAttributeState) {
		DFDGDPRVertex copiedSink;
		if (mapping.containsKey(this.sink)) {
			copiedSink = (DFDGDPRVertex) mapping.get(this.sink);
		} else {
			copiedSink = ((DFDGDPRVertex) sink).copy(mapping);
		}
		copiedSink.unify(new HashSet<>());
		return new DFDGDPRTransposeFlowGraph(copiedSink, this.relevantContextDependentAttributes, contextAttributeState, this.dd);
	}
	
	public ContextAttributeState getContextAttributeState() {
		return contextAttributeState.get();
	}
}
