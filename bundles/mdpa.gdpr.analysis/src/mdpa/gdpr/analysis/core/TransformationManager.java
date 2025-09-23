package mdpa.gdpr.analysis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mdpa.gdpr.analysis.dfd.DataFlowDiagramAndDataDictionary;
import mdpa.gdpr.dfdconverter.GDPR2DFD;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.GDPR.Processing;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;
import org.apache.log4j.Logger;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;

/**
 * Manages the transformation from GDPR to DFD that is required to find
 * {@link org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph} for the analysis.
 */
public class TransformationManager {
    private final Logger logger = Logger.getLogger(TransformationManager.class);

    private final Map<Node, Processing> dfdToGDPRMapping;
    private final List<ContextDependentAttributeSource> contextDependentAttributes;

    /**
     * Creates a new empty {@link TransformationManager}
     */
    public TransformationManager() {
        this.dfdToGDPRMapping = new HashMap<>();
        this.contextDependentAttributes = new ArrayList<>();
    }

    /**
     * Converts model to DFD and saves tracemodel
     * @param gdprModel Input GDPR Model
     * @param contextDependentProperties Input context property model
     * @return Returns the data flow diagram and data dictionary of the converted model
     */
    public DataFlowDiagramAndDataDictionary transform(LegalAssessmentFacts gdprModel, ContextDependentProperties contextDependentProperties) {
        GDPR2DFD converter = new GDPR2DFD(gdprModel);
        converter.transform();
        processTransformation(converter.getDataFlowDiagram(), gdprModel);
        processContextDependentAttributes(contextDependentProperties, converter.getDataDictionary());
        return new DataFlowDiagramAndDataDictionary(converter.getDataFlowDiagram(), converter.getDataDictionary());
    }

    /**
     * Runs some postprocessing on the resulting DFD model for keeping track of the trace between nodes and processing
     * elements TODO: This can be replaced with the tracemodel
     * @param dataFlowDiagram Data flow diagram of the Transformation
     * @param gdprModel GDPR model of the transformation
     */
    private void processTransformation(DataFlowDiagram dataFlowDiagram, LegalAssessmentFacts gdprModel) {
        List<Node> nodes = dataFlowDiagram.getNodes();
        for (Node node : nodes) {
            Processing gdprElement = gdprModel.getProcessing()
                    .stream()
                    .filter(it -> it.getId()
                            .equals(node.getId()))
                    .findAny()
                    .orElseThrow();
            this.addMapping(gdprElement, node);
        }
    }

    /**
     * Creates the {@link ContextDependentAttributeSource}s and {@link ContextDependentAttributeScenario} for the context
     * property model. Additionally, it creates the required labels in the data dictionary.
     * @param propertyModel Context Property Model of the transformation
     * @param dataDictionary Data Dictionary of the transformation
     */
    private void processContextDependentAttributes(ContextDependentProperties propertyModel, DataDictionary dataDictionary) {
        for (Property property : propertyModel.getProperty()) {
            LabelType type = datadictionaryFactory.eINSTANCE.createLabelType();
            type.setEntityName(property.getEntityName());
            type.setId(property.getId());
            dataDictionary.getLabelTypes()
                    .add(type);
            for (PropertyValue propertyValue : property.getPropertyvalue()) {
                Label label = datadictionaryFactory.eINSTANCE.createLabel();
                label.setEntityName(propertyValue.getEntityName());
                label.setId(propertyValue.getId());
                type.getLabel()
                        .add(label);
            }
        }
        for (PropertyAnnotation propertyAnnotation : propertyModel.getPropertyannotation()) {
            if (propertyAnnotation.getContextannotation()
                    .isEmpty()) {
                this.contextDependentAttributes.add(new ContextDependentAttributeSource(propertyAnnotation, propertyAnnotation.getProperty()
                        .getPropertyvalue(), List.of()));
            } else {
                List<ContextDependentAttributeSource> sources = new ArrayList<>();
                propertyAnnotation.getContextannotation()
                        .forEach(it -> {
                            var source = new ContextDependentAttributeSource(propertyAnnotation, it);
                            sources.add(source);
                            this.contextDependentAttributes.add(source);
                        });
                this.contextDependentAttributes.add(new ContextDependentAttributeSource(propertyAnnotation, propertyAnnotation.getProperty()
                        .getPropertyvalue(), sources));
            }
        }
        logger.info("Parsed " + this.contextDependentAttributes.size() + " CDA!");
    }

    /**
     * Adds a new mapping between the given GDPR {@link Processing} element an the DFD {@link Node}
     * @param gdprElement Given {@link Processing} element
     * @param dfdElement Given {@link Node} element
     */
    private void addMapping(Processing gdprElement, Node dfdElement) {
        this.dfdToGDPRMapping.put(dfdElement, gdprElement);
    }

    /**
     * Returns the GDPR {@link Processing} element that corresponds to the given node
     * @param node Given DFD {@link Node}
     * @return Returns the {@link Processing} element, if one exits
     */
    public Optional<Processing> getElement(Node node) {
        return Optional.ofNullable(this.dfdToGDPRMapping.get(node));
    }

    /**
     * Returns the list of {@link ContextDependentAttributeSource} that were parsed by the transformation
     * @return Returns the list of parsed context dependent attributes from the metamodel instance
     */
    public List<ContextDependentAttributeSource> getContextDependentAttributes() {
        return this.contextDependentAttributes;
    }
}
