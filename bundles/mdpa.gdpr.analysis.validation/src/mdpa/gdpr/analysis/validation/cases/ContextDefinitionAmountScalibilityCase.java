package mdpa.gdpr.analysis.validation.cases;

import java.util.ArrayList;
import java.util.List;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.GDPRModelBuilder;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.metamodel.GDPR.Purpose;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;

public class ContextDefinitionAmountScalibilityCase extends AbstractScalibilityCase {

	@Override
	public void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		// ------------ Model creation ---------------
		GDPRModelBuilder builder = new GDPRModelBuilder();
		List<Purpose> purposes = new ArrayList<>(parameter.getModelSize());
		purposes.add(builder.getDefaultPurpose());
		for(int i = 1; i < parameter.getModelSize(); i++) {
			purposes.add(builder.createPurpose("Purpose " + i));
		}
		builder.getFirstElement().getPurpose().clear();
		builder.getFirstElement().getPurpose().addAll(purposes);
		builder.createStoringElement("Storing");
		
		//-------- Context Dependent Attribute -------------------
		for (int i = 0; i < parameter.getModelSize(); i++) {
			Property property = builder.createProperty("Type" + i, List.of("True", "False"));
			PropertyAnnotation propertyAnnotation = builder.createPropertyAnnotation(builder.getDefaultPersonalData(), property);
			ContextAnnotation contextAnnotation = builder.createContextAnnotation("Annotation" + i, List.of(property.getPropertyvalue().get(0)), propertyAnnotation);
			builder.createContextDefinition("Definition" + i, purposes.get(i), contextAnnotation);
		}
		
		// ------------ Analysis Execution ------------------
		analysisExecutor.executeAnalysis(parameter, builder);
	}

	@Override
	public int getScalibilityStep(int index) {
		return 10 * index;
	}

	@Override
	public String getTestName() {
		return "ContextDefinitionAmount";
	}


}
