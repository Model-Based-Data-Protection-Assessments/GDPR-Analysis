package mdpa.gdpr.analysis.dfd;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

/**
 * Contains a full metamodel required to run a DFD-based
 * {@link org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis}
 * @param dataFlowDiagram Data Flow Diagram of the model
 * @param dataDictionary Data Dictionary of the model
 */
public record DataFlowDiagramAndDataDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {

}
