package mdpa.gdpr.analysis.dfd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mdpa.gdpr.analysis.core.ContextDependentAttributeSource;
import mdpa.gdpr.analysis.core.resource.GDPRResourceProvider;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.GDPR.Collecting;
import mdpa.gdpr.metamodel.GDPR.PersonalData;
import mdpa.gdpr.metamodel.GDPR.Role;
import mdpa.gdpr.metamodel.GDPR.Storing;
import mdpa.gdpr.metamodel.GDPR.Transferring;
import mdpa.gdpr.metamodel.GDPR.Usage;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;

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
        DataFlowDiagramAndDataDictionary dfd = gdprResourceProvider.getTransformationManager()
                .transform(gdprResourceProvider.getGDPRModel(), gdprResourceProvider.getContextDependentProperties());
        DFDTransposeFlowGraphFinder finder = new DFDTransposeFlowGraphFinder(dfd.dataDictionary(), dfd.dataFlowDiagram());
        List<DFDGDPRTransposeFlowGraph> completeFlowGraphs = finder.findTransposeFlowGraphs()
                .stream()
                .map(it -> this.transformFlowGraph((DFDTransposeFlowGraph) it, dfd.dataDictionary()))
                .toList();
        List<DFDGDPRTransposeFlowGraph> result = new ArrayList<>(completeFlowGraphs);
        /*
         * result.addAll(completeFlowGraphs.stream() .map(it -> this.getPartialTransposeFlowGraphs(it, dfd.dataDictionary()))
         * .flatMap(List::stream) .toList());
         */
        return result;
    }

    private DFDGDPRTransposeFlowGraph transformFlowGraph(DFDTransposeFlowGraph transposeFlowGraph, DataDictionary dd) {
        Map<DFDVertex, DFDGDPRVertex> mapping = new IdentityHashMap<>();
        transposeFlowGraph.getVertices()
                .stream()
                .map(DFDVertex.class::cast)
                .forEach(vertex -> mapping.put(vertex, this.getDFDGDPRVertex(vertex, new IdentityHashMap<>())));
        return new DFDGDPRTransposeFlowGraph(mapping.get((DFDVertex) transposeFlowGraph.getSink()),
                this.determineContextDependentAttributes(transposeFlowGraph, mapping.values()), dd);
    }

    private List<DFDGDPRTransposeFlowGraph> getPartialTransposeFlowGraphs(DFDGDPRTransposeFlowGraph transposeFlowGraph, DataDictionary dd) {
        List<DFDGDPRTransposeFlowGraph> result = new ArrayList<>();
        Map<Role, List<DFDGDPRVertex>> roleMap = new HashMap<>();
        transposeFlowGraph.getVertices()
                .stream()
                .filter(DFDGDPRVertex.class::isInstance)
                .map(DFDGDPRVertex.class::cast)
                .forEach(vertex -> {
                    Role role = vertex.getResponsibilityRole();
                    List<DFDGDPRVertex> roleVertices = roleMap.getOrDefault(role, new ArrayList<>());
                    roleVertices.add(vertex);
                    roleMap.put(role, roleVertices);
                });
        if (roleMap.size() < 2) {
            return List.of();
        }

        for (Map.Entry<Role, List<DFDGDPRVertex>> entry : roleMap.entrySet()) {
            List<DFDGDPRVertex> roleVertices = entry.getValue();
            List<DFDGDPRVertex> previousElements = roleVertices.stream()
                    .map(DFDGDPRVertex::getPreviousElements)
                    .flatMap(List::stream)
                    .filter(DFDGDPRVertex.class::isInstance)
                    .map(DFDGDPRVertex.class::cast)
                    .toList();
            List<DFDGDPRVertex> sinks = roleVertices.stream()
                    .filter(it -> !previousElements.contains(it))
                    .toList();
            for (DFDGDPRVertex sink : sinks) {
                Map<DFDVertex, DFDVertex> mapping = new HashMap<>();
                result.add(new DFDGDPRTransposeFlowGraph(this.getMappingForSink(sink, roleVertices, mapping),
                        new ArrayList<>(transposeFlowGraph.getContextDependentAttributeSources()), dd));
            }
        }
        return result;
    }

    private DFDGDPRVertex getMappingForSink(DFDGDPRVertex sink, List<DFDGDPRVertex> roleVertices, Map<DFDVertex, DFDVertex> mapping) {
        Map<Pin, DFDVertex> pinVertexMap = new HashMap<>();
        sink.getPinDFDVertexMap()
                .entrySet()
                .stream()
                .filter(it -> roleVertices.contains(it.getValue()))
                .forEach(it -> {
                    pinVertexMap.put(it.getKey(), this.getMappingForSink((DFDGDPRVertex) it.getValue(), roleVertices, mapping));
                });
        return new DFDGDPRVertex(sink.getReferencedElement(), pinVertexMap, new HashMap<>(sink.getPinFlowMap()),
                new ArrayList<>(sink.getRelatedElements()));
    }

    private DFDGDPRVertex getDFDGDPRVertex(DFDVertex vertex, Map<DFDVertex, DFDVertex> mapping) {
        if (!(this.resourceProvider instanceof GDPRResourceProvider gdprResourceProvider)) {
            this.logger.error("Resource provider is not a GDPR resource provider!");
            throw new IllegalArgumentException();
        }
        AbstractGDPRElement gdprElement = gdprResourceProvider.getTransformationManager()
                .getElement(vertex.getReferencedElement())
                .orElseThrow();
        Map<Pin, DFDVertex> copiedPinDFDVertexMap = new HashMap<>();
        vertex.getPinDFDVertexMap()
                .keySet()
                .forEach(key -> copiedPinDFDVertexMap.put(key, mapping.getOrDefault(vertex.getPinDFDVertexMap()
                        .get(key),
                        this.getDFDGDPRVertex(vertex.getPinDFDVertexMap()
                                .get(key), mapping))));
        List<AbstractGDPRElement> relatedElements = this.determineRelatedElements(gdprElement);
        return new DFDGDPRVertex(vertex.getReferencedElement(), copiedPinDFDVertexMap, new HashMap<>(vertex.getPinFlowMap()), relatedElements);
    }

    private List<AbstractGDPRElement> determineRelatedElements(AbstractGDPRElement gdprElement) {
        List<AbstractGDPRElement> result = new ArrayList<>();
        result.add(gdprElement);
        if (gdprElement instanceof Collecting collecting) {
            result.addAll(collecting.getInputData());
            result.addAll(collecting.getInputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(collecting.getOutputData());
            result.addAll(collecting.getOutputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(collecting.getPurpose());
            result.addAll(collecting.getOnTheBasisOf());
            result.add(collecting.getResponsible());
        } else if (gdprElement instanceof Usage usage) {
            result.addAll(usage.getInputData());
            result.addAll(usage.getInputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(usage.getOutputData());
            result.addAll(usage.getOutputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(usage.getPurpose());
            result.addAll(usage.getOnTheBasisOf());
            result.add(usage.getResponsible());
        } else if (gdprElement instanceof Transferring transferring) {
            result.addAll(transferring.getInputData());
            result.addAll(transferring.getInputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(transferring.getOutputData());
            result.addAll(transferring.getOutputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(transferring.getPurpose());
            result.addAll(transferring.getOnTheBasisOf());
            result.add(transferring.getResponsible());

        } else if (gdprElement instanceof Storing storing) {
            result.addAll(storing.getInputData());
            result.addAll(storing.getInputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(storing.getOutputData());
            result.addAll(storing.getOutputData()
                    .stream()
                    .filter(PersonalData.class::isInstance)
                    .map(PersonalData.class::cast)
                    .map(PersonalData::getDataReferences)
                    .flatMap(List::stream)
                    .toList());
            result.addAll(storing.getPurpose());
            result.addAll(storing.getOnTheBasisOf());
            result.add(storing.getResponsible());
        }
        return result;
    }

    private List<ContextDependentAttributeSource> determineContextDependentAttributes(DFDTransposeFlowGraph transposeFlowGraph,
            Collection<DFDGDPRVertex> vertices) {
        if (!(this.resourceProvider instanceof GDPRResourceProvider gdprResourceProvider)) {
            this.logger.error("Resource provider is not a GDPR resource provider!");
            throw new IllegalArgumentException();
        }
        return gdprResourceProvider.getTransformationManager()
                .getContextDependentAttributes()
                .stream()
                .filter(it -> it.applicable(vertices))
                .toList();
    }

    public DFDGDPRFlowGraphCollection resolveContextDependentAttributes() {
        List<DFDGDPRTransposeFlowGraph> resultingTransposeFlowGraphs = this.getTransposeFlowGraphs()
                .stream()
                .filter(DFDGDPRTransposeFlowGraph.class::isInstance)
                .map(DFDGDPRTransposeFlowGraph.class::cast)
                .map(DFDGDPRTransposeFlowGraph::determineAlternateFlowGraphs)
                .flatMap(Collection::stream)
                .toList();
        return new DFDGDPRFlowGraphCollection(resultingTransposeFlowGraphs, this.resourceProvider);
    }
}
