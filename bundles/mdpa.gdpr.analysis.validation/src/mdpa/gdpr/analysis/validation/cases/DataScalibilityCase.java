package mdpa.gdpr.analysis.validation.cases;

import java.util.ArrayList;
import java.util.List;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.GDPRModelBuilder;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.metamodel.GDPR.Data;
import mdpa.gdpr.metamodel.GDPR.Storing;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;

public class DataScalibilityCase extends AbstractScalibilityCase {

	@Override
	public void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		// ------------ Model creation ---------------
		GDPRModelBuilder builder = new GDPRModelBuilder();
		List<Data> data = new ArrayList<>(parameter.getModelSize());
		data.add(builder.getDefaultPersonalData());
		for(int i = 1; i < parameter.getModelSize(); i++) {
			data.add(builder.createPersonalData("Data " + i, builder.getDefaultNaturalPerson()));
		}
		builder.getFirstElement().getOutputData().clear();
		builder.getFirstElement().getOutputData().addAll(data);
		
		Storing storing = builder.createStoringElement("Storing");
		storing.getInputData().clear();
		storing.getInputData().addAll(data);

		//-------- Context Dependent Attribute -------------------
		Property property = builder.createProperty("Type", List.of("True", "False"));
		PropertyAnnotation propertyAnnotation = builder.createPropertyAnnotation(builder.getDefaultPersonalData(), property);
		ContextAnnotation contextAnnotation = builder.createContextAnnotation("Annotation", List.of(property.getPropertyvalue().get(0)), propertyAnnotation);
		builder.createContextDefinition("Definition", builder.getDefaultController(), contextAnnotation);
		
		// ------------ Analysis Execution ------------------
		analysisExecutor.executeAnalysis(parameter, builder);
	}

	@Override
	public int getScalibilityStep(int index) {
		return 10 * index;
	}

	@Override
	public String getTestName() {
		return "Data";
	}

}
