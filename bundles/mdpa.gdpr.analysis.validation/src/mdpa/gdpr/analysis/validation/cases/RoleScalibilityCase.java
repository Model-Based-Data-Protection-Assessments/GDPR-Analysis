package mdpa.gdpr.analysis.validation.cases;

import java.util.List;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.GDPRModelBuilder;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.metamodel.GDPR.Role;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
public class RoleScalibilityCase extends AbstractScalibilityCase {

	@Override
	public void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
		// ------------ Model creation ---------------
		GDPRModelBuilder builder = new GDPRModelBuilder();
		
		Role[] roles = new Role[] {builder.getDefaultController(), builder.createController("Other Controller")};
		builder.getFirstElement().setResponsible(roles[0]);
		for(int i = 1; i < parameter.getModelSize() - 1; i++) {
			builder.createProcessingElement("Processing " + i, roles[i % 2]);
		}
		builder.createStoringElement("Storing", roles[parameter.getModelSize() % 2]);

		//-------- Context Dependent Attribute -------------------
		/*
		Property property = builder.createProperty("Type", List.of("True", "False"));
		PropertyAnnotation propertyAnnotation = builder.createPropertyAnnotation(builder.getDefaultPersonalData(), property);
		ContextAnnotation contextAnnotation = builder.createContextAnnotation("Annotation", List.of(property.getPropertyvalue().get(0)), propertyAnnotation);
		builder.createContextDefinition("Definition", builder.getDefaultController(), contextAnnotation);
		*/
		// ------------ Analysis Execution ------------------
		analysisExecutor.executeAnalysis(parameter, builder);
	}

	@Override
	public int getScalibilityStep(int index) {
		return 10 * index;
	}

	@Override
	public String getTestName() {
		return "Role";
	}

}
