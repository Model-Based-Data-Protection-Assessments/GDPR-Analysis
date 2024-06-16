package mdpa.gdpr.analysis.core;

import mdpa.gdpr.analysis.dfd.DataFlowDiagramAndDataDictionary;
import mdpa.gdpr.dfdconverter.DFDAndTracemodel;
import mdpa.gdpr.dfdconverter.GDPR2DFD;
import mdpa.gdpr.dfdconverter.tracemodel.tracemodel.TraceModel;
import mdpa.gdpr.metamodel.GDPR.AbstractGDPRElement;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;

import org.dataflowanalysis.dfd.dataflowdiagram.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransformationManager {
    private final Map<AbstractGDPRElement, Node> gdprToDFDMapping;
    private final Map<Node, AbstractGDPRElement> dfdToGDPRMapping;
    private final List<ContextDependentAttribute> contextDependentAttributes;
    private TraceModel tracemodel;

    public TransformationManager() {
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
        DFDAndTracemodel result = converter.transform();
        this.tracemodel = result.tracemodel();
        processTracemodel();
        processContextDependentAttributes(contextDependentProperties);
        return new DataFlowDiagramAndDataDictionary(result.dataFlowDiagram(), result.dataDictionary());
    }
    
    private void processTracemodel() {
    	this.tracemodel.getTracesList().forEach(trace -> this.addMapping(trace.getProcessing(), trace.getNode()));
    }
    
    private void processContextDependentAttributes(ContextDependentProperties propertyModel) {
    	for (PropertyAnnotation propertyAnnotation : propertyModel.getPropertyannotation()) {
    		propertyAnnotation.getContextannotation().forEach(it -> this.contextDependentAttributes.add(new ContextDependentAttribute(propertyAnnotation, it)));
    	}
    }

    private void addMapping(AbstractGDPRElement gdprElement, Node dfdElement) {
        this.gdprToDFDMapping.put(gdprElement, dfdElement);
        this.dfdToGDPRMapping.put(dfdElement, gdprElement);
    }

    public Optional<AbstractGDPRElement> getElement(Node node) {
        return Optional.ofNullable(this.dfdToGDPRMapping.get(node));
    }

    public Optional<Node> getElement(AbstractGDPRElement gdprElement) {
        return Optional.ofNullable(this.gdprToDFDMapping.get(gdprElement));
    }
    
    public List<ContextDependentAttribute> getContextDependentAttributes() {
		return this.contextDependentAttributes;
	}
}
