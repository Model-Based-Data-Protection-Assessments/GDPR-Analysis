package mdpa.gdpr.analysis.dfd;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

public record DataFlowDiagramAndDataDictionary(DataFlowDiagram dataFlowDiagram, DataDictionary dataDictionary) {
}
