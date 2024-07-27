package mdpa.gdpr.analysis.validation;

public enum ScalibilityEvent {
	ANALYSIS_INITIALZATION("AnalysisInitialization"), 
	TFG_FINDING("TFGFinding"), 
	ATTRIBUTE_RESOLVING("ContextDependentAttributeResolving"),
	PROPAGATION("Propagation");
	
	private String name;
	
	ScalibilityEvent(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
