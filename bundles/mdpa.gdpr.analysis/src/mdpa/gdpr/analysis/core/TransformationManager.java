package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DataFlowDiagramAndDataDictionary;
import mdpa.gdpr.dfdconverter.GDPR2DFD;
import mdpa.gdpr.dfdconverter.tracemodel.tracemodel.TracemodelFactory;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.GDPR.Collecting;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.GDPR.Processing;
import mdpa.gdpr.metamodel.GDPR.Storing;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

import org.apache.log4j.Logger;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransformationManager {
	private final Logger logger = Logger.getLogger(TransformationManager.class);
	
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
        GDPR2DFD converter = new GDPR2DFD(gdprModel, TracemodelFactory.eINSTANCE.createTraceModel());
        converter.transform();
        processTransformation(converter.getDataFlowDiagram(), converter.getDataDictionary(), gdprModel);
        processContextDependentAttributes(contextDependentProperties, converter.getDataDictionary());
        return new DataFlowDiagramAndDataDictionary(converter.getDataFlowDiagram(), converter.getDataDictionary());
    }
    
    private void processTransformation(DataFlowDiagram dfd, DataDictionary dd, LegalAssessmentFacts gdprModel) {
        List<Node> nodes = dfd.getNodes();
        for(Node node : nodes) {
            Processing gdprElement = gdprModel.getProcessing().stream()
                    .filter(it -> it.getId().equals(node.getId()))
                    .findAny().orElseThrow();
            this.addMapping(gdprElement, node);
        }
    }
    
    private void processContextDependentAttributes(ContextDependentProperties propertyModel, DataDictionary dd) {
    	for (Property property : propertyModel.getProperty()) {
    		LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
    		type.setEntityName(property.getEntityName());
    		type.setId(property.getId());
    		dd.getLabelTypes().add(type);
    		for (PropertyValue propertyValue : property.getPropertyvalue()) {
    			Label label = datadictionaryFactory.eINSTANCE.createLabel();
    			label.setEntityName(propertyValue.getEntityName());
    			label.setId(propertyValue.getId());
    			type.getLabel().add(label);
    		}
    	}
    	for (PropertyAnnotation propertyAnnotation : propertyModel.getPropertyannotation()) {
    		if (propertyAnnotation.getContextannotation().isEmpty()) {
    			this.contextDependentAttributes.add(new ContextDependentAttributeSource(propertyAnnotation, propertyAnnotation.getProperty().getPropertyvalue(), List.of()));
    		} else {
    			List<ContextDependentAttributeSource> sources = new ArrayList<>();
    			propertyAnnotation.getContextannotation().stream()
    				.forEach(it -> {
    					var source = new ContextDependentAttributeSource(propertyAnnotation, it);
    	    			sources.add(source);
    					this.contextDependentAttributes.add(source);
    			});
    			this.contextDependentAttributes.add(new ContextDependentAttributeSource(propertyAnnotation, propertyAnnotation.getProperty().getPropertyvalue(), sources));
    		}
    	}
    	logger.info("Parsed " + this.contextDependentAttributes.size() + " CDA!");
    }

    private void addAssignments(DataFlowDiagram dfd, DataDictionary dd) {
        for(Node node : dfd.getNodes()) {
            Processing gdprElement = this.getElement(node).orElseThrow();
            if (!(gdprElement instanceof Storing) && !(gdprElement instanceof Collecting)) {
                ForwardingAssignment assignment = datadictionaryFactory.eINSTANCE.createForwardingAssignment();
                assignment.getInputPins().addAll(node.getBehavior().getInPin());
                if (!node.getBehavior().getOutPin().isEmpty()) {
                    assignment.setOutputPin(node.getBehavior().getOutPin().get(0));
                }
                node.getBehavior().getAssignment().add(assignment);
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
