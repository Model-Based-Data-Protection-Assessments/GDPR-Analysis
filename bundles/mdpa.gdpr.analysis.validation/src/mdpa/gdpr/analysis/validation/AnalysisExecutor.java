package mdpa.gdpr.analysis.validation;

import mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysis;
import mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysisBuilder;
import mdpa.gdpr.analysis.dfd.DFDGDPRFlowGraphCollection;
import mdpa.gdpr.analysis.validation.testmodels.Activator;

public class AnalysisExecutor {
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, GDPRModelBuilder gdprModelBuilder) {		
		scalibilityParameter.startTiming();
		
		GDPRLegalAssessmentAnalysis analysis = new GDPRLegalAssessmentAnalysisBuilder()
				.standalone()
				.modelProjectName("mdpa.gdpr.analysis.validation.testmodels")
				.usePluginActivator(Activator.class)
				.useCustomResourceProvider(new MemoryGDPRResourceProvider(gdprModelBuilder))
				.build();
		
		analysis.initializeAnalysis();
		scalibilityParameter.recordScalibilityEvent(ScalibilityEvent.ANALYSIS_INITIALZATION);
		
		DFDGDPRFlowGraphCollection flowGraphs = (DFDGDPRFlowGraphCollection) analysis.findFlowGraphs();
		scalibilityParameter.recordScalibilityEvent(ScalibilityEvent.TFG_FINDING);
		
		DFDGDPRFlowGraphCollection evaluatedFlowGraphs = flowGraphs.resolveContextDependentAttributes();
		scalibilityParameter.recordScalibilityEvent(ScalibilityEvent.ATTRIBUTE_RESOLVING);
		
		evaluatedFlowGraphs.evaluate();
		scalibilityParameter.recordScalibilityEvent(ScalibilityEvent.PROPAGATION);
		
		scalibilityParameter.stopTiming();
	}
}
