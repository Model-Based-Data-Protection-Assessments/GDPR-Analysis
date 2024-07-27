package mdpa.gdpr.analysis.validation.cases;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;

public abstract class AbstractScalibilityCase {
	
	public abstract void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor);
	
	public abstract int getScalibilityStep(int index);
	
	public abstract String getTestName();
}
