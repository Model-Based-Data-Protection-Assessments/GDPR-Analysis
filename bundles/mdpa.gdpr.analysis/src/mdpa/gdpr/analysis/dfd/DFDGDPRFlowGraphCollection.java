package mdpa.gdpr.analysis.dfd;

import mdpa.gdpr.analysis.core.ContextDependentAttribute;
import mdpa.gdpr.analysis.core.resource.GDPRResourceProvider;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DFDGDPRFlowGraphCollection extends FlowGraphCollection {
    private final Logger logger = Logger.getLogger(DFDGDPRFlowGraphCollection.class);

    public DFDGDPRFlowGraphCollection(ResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public DFDGDPRFlowGraphCollection(List<DFDGDPRTransposeFlowGraph> transposeFlowGraphs, ResourceProvider resourceProvider) {
        super(transposeFlowGraphs, resourceProvider);
    }

    @Override
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        if (!(this.resourceProvider instanceof GDPRResourceProvider gdprResourceProvider)) {
            this.logger.error("Resource provider is not a GDPR resource provider!");
            throw new IllegalArgumentException();
        }
        DataFlowDiagramAndDataDictionary dfd = gdprResourceProvider.getTransformationManager().transform(gdprResourceProvider.getModel(), gdprResourceProvider.getContextDependentProperties());
        DFDTransposeFlowGraphFinder finder = new DFDTransposeFlowGraphFinder(dfd.dataDictionary(), dfd.dataFlowDiagram());
        List<DFDGDPRTransposeFlowGraph> result = finder.findTransposeFlowGraphs().stream()
                .map(it -> this.transformFlowGraph((DFDTransposeFlowGraph) it))
                .toList();
        return result;
    }
    
    private DFDGDPRTransposeFlowGraph transformFlowGraph(DFDTransposeFlowGraph transposeFlowGraph) {
    	Map<DFDVertex, DFDVertex> mapping = new IdentityHashMap<>();
    	transposeFlowGraph.getVertices().stream()
    		.map(DFDVertex.class::cast)
    		.forEach(vertex -> mapping.put(vertex, this.getDFDGDPRVertex(vertex, new IdentityHashMap<>())));
    	return new DFDGDPRTransposeFlowGraph(mapping.get(transposeFlowGraph.getSink()), this.determineContextDependentAttributes(transposeFlowGraph));
    }
    
    private DFDGDPRVertex getDFDGDPRVertex(DFDVertex vertex, Map<DFDVertex, DFDVertex> mapping) {
        if (!(this.resourceProvider instanceof GDPRResourceProvider gdprResourceProvider)) {
            this.logger.error("Resource provider is not a GDPR resource provider!");
            throw new IllegalArgumentException();
        }
    	AbstractGDPRElement gdprElement = gdprResourceProvider.getTransformationManager().getElement(vertex.getReferencedElement()).orElseThrow();
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        vertex.getPinDFDVertexMap().keySet()
                .forEach(key -> copiedPinDFDVertexMap.put(key, mapping.getOrDefault(vertex.getPinDFDVertexMap().get(key), this.getDFDGDPRVertex(vertex.getPinDFDVertexMap().get(key), mapping))));
    	List<AbstractGDPRElement> relatedElements = this.determineRelatedElements(gdprElement);
        return new DFDGDPRVertex(vertex.getReferencedElement(), vertex.getPinDFDVertexMap(), new HashMap<>(vertex.getPinFlowMap()), relatedElements);
    }
    
    private List<AbstractGDPRElement> determineRelatedElements(AbstractGDPRElement gdprElement) {
    	List<AbstractGDPRElement> result = new ArrayList<>();
    	// TODO: What elements are related?
    	return result;
    }
    
    private List<ContextDependentAttribute> determineContextDependentAttributes(DFDTransposeFlowGraph transposeFlowGraph) {
        if (!(this.resourceProvider instanceof GDPRResourceProvider gdprResourceProvider)) {
            this.logger.error("Resource provider is not a GDPR resource provider!");
            throw new IllegalArgumentException();
        }
        List<AbstractGDPRElement> gdprElements = transposeFlowGraph.getVertices().stream()
        		.map(it -> gdprResourceProvider.getTransformationManager().getElement((Node) it))
        		.filter(Optional::isPresent)
        		.map(Optional::get)
        		.toList();
        return gdprResourceProvider.getTransformationManager().getContextDependentAttributes().stream()
        		.filter(it -> gdprElements.contains(it.getReferencedElement()))
        		.toList();
    }

    public DFDGDPRFlowGraphCollection resolveContextDependentAttributes() {
        List<DFDGDPRTransposeFlowGraph> resultingTransposeFlowGraphs = this.getTransposeFlowGraphs().stream()
                .filter(DFDGDPRTransposeFlowGraph.class::isInstance)
                .map(DFDGDPRTransposeFlowGraph.class::cast)
                .map(DFDGDPRTransposeFlowGraph::determineAlternateFlowGraphs)
                .flatMap(Collection::stream)
                .toList();
        return new DFDGDPRFlowGraphCollection(resultingTransposeFlowGraphs, this.resourceProvider);
    }
}
