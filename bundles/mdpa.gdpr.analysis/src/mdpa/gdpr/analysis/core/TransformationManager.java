package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DataFlowDiagramAndDataDictionary;
import mdpa.gdpr.dfdconverter.DataFlowDiagramAndDictionary;
import mdpa.gdpr.dfdconverter.GDPR2DFD;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.GDPR.Collecting;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.GDPR.Processing;
import mdpa.gdpr.metamodel.GDPR.Storing;
import mdpa.gdpr.metamodel.GDPR.Usage;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;

import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransformationManager {
    private final Map<Node, List<AbstractGDPRElement>> relatedElementMapping;
    private final Map<Processing, Node> gdprToDFDMapping;
    private final Map<Node, Processing> dfdToGDPRMapping;
    private final List<ContextDependentAttributeSource> contextDependentAttributes;

    public TransformationManager() {
        this.relatedElementMapping = new HashMap<>();
        this.gdprToDFDMapping = new HashMap<>();
        this.dfdToGDPRMapping = new HashMap<>();
        this.contextDependentAttributes = new ArrayList<>();
    }

    /**
     * Converts model to DFD and saves tracemodel
     * @param gdprModel
     * @return
     */
    public DataFlowDiagramAndDataDictionary transform(LegalAssessmentFacts gdprModel, ContextDependentProperties contextDependentProperties) {
        GDPR2DFD converter = new GDPR2DFD(gdprModel);
        DataFlowDiagramAndDictionary result = converter.transform();
        processTransformation(result, gdprModel);
        processContextDependentAttributes(contextDependentProperties);
        addAssignments(result);
        return new DataFlowDiagramAndDataDictionary(result.dataFlowDiagram(), result.dataDictionary());
    }
    
    private void processTransformation(DataFlowDiagramAndDictionary dfd, LegalAssessmentFacts gdprModel) {
        List<Node> nodes = dfd.dataFlowDiagram().getNodes();
        for(Node node : nodes) {
            Processing gdprElement = gdprModel.getProcessing().stream()
                    .filter(it -> it.getId().equals(node.getId()))
                    .findAny().orElseThrow();
            this.addMapping(gdprElement, node);
        }
    }
    
    private void processContextDependentAttributes(ContextDependentProperties propertyModel) {
    	for (PropertyAnnotation propertyAnnotation : propertyModel.getPropertyannotation()) {
    		this.contextDependentAttributes.add(new ContextDependentAttributeSource(propertyAnnotation));
    	}
    }

    private void addAssignments(DataFlowDiagramAndDictionary dfd) {
        for(Node node : dfd.dataFlowDiagram().getNodes()) {
            Processing gdprElement = this.getElement(node).orElseThrow();
            if (!(gdprElement instanceof Storing) && !(gdprElement instanceof Collecting)) {
                ForwardingAssignment assignment = datadictionaryFactory.eINSTANCE.createForwardingAssignment();
                assignment.getInputPins().addAll(node.getBehaviour().getInPin());
                if (!node.getBehaviour().getOutPin().isEmpty()) {
                    assignment.setOutputPin(node.getBehaviour().getOutPin().get(0));
                }
                node.getBehaviour().getAssignment().add(assignment);
            }
        }
    }

    private void addMapping(Processing gdprElement, Node dfdElement) {
        this.gdprToDFDMapping.put(gdprElement, dfdElement);
        this.dfdToGDPRMapping.put(dfdElement, gdprElement);
    }

    public Optional<Processing> getElement(Node node) {
        return Optional.ofNullable(this.dfdToGDPRMapping.get(node));
    }

    public Optional<Node> getElement(Processing gdprElement) {
        return Optional.ofNullable(this.gdprToDFDMapping.get(gdprElement));
    }
    
    public List<ContextDependentAttributeSource> getContextDependentAttributes() {
		return this.contextDependentAttributes;
	}
}
