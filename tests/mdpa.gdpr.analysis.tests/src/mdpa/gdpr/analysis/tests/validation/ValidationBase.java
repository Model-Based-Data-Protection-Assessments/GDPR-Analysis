package mdpa.gdpr.analysis.tests.validation;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysis;
import mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysisBuilder;
import mdpa.gdpr.analysis.dfd.DFDGDPRTransposeFlowGraph;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.analysis.testmodels.Activator;

@TestInstance(Lifecycle.PER_CLASS)
public class ValidationBase {
	private final String modelName;
	private final String modelFolder;
	
	protected GDPRLegalAssessmentAnalysis analysis;

	
	public ValidationBase(String modelName, String modelFolder) {
		this.modelName = modelName;
		this.modelFolder = modelFolder;
	}
	
	@BeforeEach
	public void loadModel() {
		analysis = new GDPRLegalAssessmentAnalysisBuilder()
				.standalone()
				.modelProjectName("mdpa.gdpr.analysis.testmodels")
				.usePluginActivator(Activator.class)
				.useGDPRModel(modelFolder + "/" + modelName + ".gdpr")
				.useProperties(modelFolder + "/" + modelName + ".contextproperties")
				.build();
		analysis.initializeAnalysis();
	}
	
	private boolean isImpacted(DFDGDPRVertex vertex) {
		if (!vertex.getContextDependentAttributes().isEmpty()) {
			return true;
		}
		return vertex.getPreviousElements().stream().anyMatch(it -> this.isImpacted((DFDGDPRVertex) it));
	}
	
	protected List<DFDGDPRVertex> getImpactedElements(DFDGDPRTransposeFlowGraph transposeFlowGraph) {
		List<DFDGDPRVertex> impactedElements = new ArrayList<>();
			for (DFDGDPRVertex vertex : transposeFlowGraph.getVertices().stream().filter(DFDGDPRVertex.class::isInstance).map(DFDGDPRVertex.class::cast).toList()) {
				if (this.isImpacted(vertex)) {
					impactedElements.add(vertex);
				}
			}
		return impactedElements;
	}
	
	protected boolean hasVertexCharacteristic(DFDGDPRVertex vertex, String characteristicType, String characteristicValue) {
		return vertex.getVertexCharacteristicNames(characteristicType)
				.contains(characteristicValue);
	}
	
	protected boolean hasDataCharacteristic(DFDGDPRVertex vertex, String characteristicType, String characteristicValue) {
		return vertex.getAllDataCharacteristics().stream()
				.anyMatch(it -> {
					return it.getCharacteristicsWithName(characteristicType).stream()
							.anyMatch(cv -> cv.getValueName().equals(characteristicValue));
				});
	}
}
