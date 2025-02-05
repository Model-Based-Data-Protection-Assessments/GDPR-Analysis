package mdpa.gdpr.analysis.validation.cases;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;

public abstract class AbstractScalibilityCase {
	
	public abstract void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor);
	
	public int getScalibilityStep(int index) {
		return (int) Math.floor(Math.pow(10, index));
	};
	
	public abstract String getTestName();
}
