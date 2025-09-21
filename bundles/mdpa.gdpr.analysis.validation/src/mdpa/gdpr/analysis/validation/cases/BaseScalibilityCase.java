package mdpa.gdpr.analysis.validation.cases;

import java.util.List;
import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.GDPRModelBuilder;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.metamodel.GDPR.Storing;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;

public class BaseScalibilityCase extends AbstractScalibilityCase {

    @Override
    public void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
        // ------------ Model creation ---------------
        GDPRModelBuilder builder = new GDPRModelBuilder();
        Storing storing = builder.createStoringElement("Storing");

        // -------- Context Dependent Attribute -------------------
        Property property = builder.createProperty("Type", List.of("True", "False"));
        PropertyAnnotation propertyAnnotation = builder.createPropertyAnnotation(storing, property);
        ContextAnnotation contextAnnotation = builder.createContextAnnotation("Annotation", List.of(property.getPropertyvalue()
                .get(0)), propertyAnnotation);
        ContextDefinition contextDefinition = builder.createContextDefinition("Definition", builder.getDefaultController(), contextAnnotation);

        // ------------ Analysis Execution ------------------
        analysisExecutor.executeAnalysis(parameter, builder);
    }

    @Override
    public String getTestName() {
        return "Base";
    }

}
