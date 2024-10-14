package mdpa.gdpr.analysis.validation.cases;

import java.util.ArrayList;
import java.util.List;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.GDPRModelBuilder;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.metamodel.contextproperties.Property;

public class DataUndefinedContextDependentAttributeScalibilityCase extends AbstractScalibilityCase {

	@Override
	public void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		// ------------ Model creation ---------------
		GDPRModelBuilder builder = new GDPRModelBuilder();
		builder.createStoringElement("Storing");
		
		//-------- Context Dependent Attribute -------------------
		List<String> values = new ArrayList<>(parameter.getModelSize());
		for (int i = 0; i < parameter.getModelSize(); i++) {
			values.add("Value" + i);
		}
		Property property = builder.createProperty("Type", values);
		builder.createPropertyAnnotation(builder.getDefaultPersonalData(), property);
		
		// ------------ Analysis Execution ------------------
		analysisExecutor.executeAnalysis(parameter, builder);
	}

	@Override
	public int getScalibilityStep(int index) {
		return (int) Math.floor(Math.pow(2, index));
	}

	@Override
	public String getTestName() {
		return "DataUndefinedContextDependentAttribute";
	}


}
