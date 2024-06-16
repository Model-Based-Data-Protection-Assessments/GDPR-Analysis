package mdpa.gdpr.analysis.dfd;

import mdpa.gdpr.analysis.core.ContextDependentAttribute;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DFDGDPRTransposeFlowGraph extends DFDTransposeFlowGraph {
	private final Logger logger = Logger.getLogger(DFDGDPRTransposeFlowGraph.class);
	
    private final List<ContextDependentAttribute> contextDependentAttributes;

    /**
     * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
     *
     * @param sink Sink vertex that induces the transpose flow graph
     */
    public DFDGDPRTransposeFlowGraph(AbstractVertex<?> sink, List<ContextDependentAttribute> contextDependentAttributes) {
        super(sink);
        this.contextDependentAttributes = contextDependentAttributes;
    }

    public List<DFDGDPRTransposeFlowGraph> determineAlternateFlowGraphs() {
    	List<DFDGDPRTransposeFlowGraph> result = new ArrayList<>();
    	for(ContextDependentAttribute contextDependentAttribute : this.contextDependentAttributes) {
    		Optional<DFDGDPRVertex> matchingVertex = this.getVertices().stream()
    				.filter(DFDGDPRVertex.class::isInstance)
    				.map(DFDGDPRVertex.class::cast)
    				.filter(it -> contextDependentAttribute.matches(it))
    				.findAny();
    		if (matchingVertex.isEmpty()) {
    			logger.warn("Could not find matching vertex for context dependent attribute");
    			continue;
    		}
    		if (contextDependentAttribute.matchesContext(matchingVertex.get())) {
    			// Context dependent attribute resolvable
    			// TODO: Pin (and term values)
    			DFDGDPRVertex targetVertex = matchingVertex.get();
    			Node replacingNode = EcoreUtil.copy(targetVertex.getReferencedElement());
    			Assignment assignment = datadictionaryFactory.eINSTANCE.createAssignment();
    			assignment.setTerm(datadictionaryFactory.eINSTANCE.createTRUE());
    			assignment.setOutputPin(null);
    			replacingNode.getBehaviour().getAssignment().add(assignment);
    			DFDGDPRVertex replacingVertex = this.copyVertex(targetVertex, replacingNode);
    			Map<DFDVertex, DFDVertex> mapping = new IdentityHashMap<>();
    			mapping.put(targetVertex, replacingVertex);
    			result.add((DFDGDPRTransposeFlowGraph) this.copy(mapping));
    		} else {
    			// Context dependent attribute not resolvable
    		}
    	}
        return result;
    }
    
    public List<ContextDependentAttribute> getContextDependentAttributes() {
		return contextDependentAttributes;
	}
    
    /**
     * Copies the dfd vertex with the given replacing node
     * @param vertex DFD vertex of which the pin to vertex and pin to flow map should be copied
     * @param replacingNode Referenced node by the new dfd vertex
     * @return Returns a new dfd vertex with the given node and maps of the given vertex
     */
    private DFDGDPRVertex copyVertex(DFDGDPRVertex vertex, Node replacingElement) {
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        vertex.getPinDFDVertexMap().keySet().forEach(key -> copiedPinDFDVertexMap.put(key, vertex.getPinDFDVertexMap().get(key).copy(new IdentityHashMap<>())));
        return new DFDGDPRVertex(vertex.getReferencedElement(), copiedPinDFDVertexMap, new HashMap<>(vertex.getPinFlowMap()), new ArrayList<>(vertex.getRelatedElements()));
    }
}
