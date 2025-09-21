package mdpa.gdpr.analysis.validation.cases;

import java.util.ArrayList;
import java.util.List;
import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.GDPRModelBuilder;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;

public class NodeDefinedContextDependentAttributeScalibilityCase extends AbstractScalibilityCase {

    @Override
    public void runScalibilityCase(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor) {
        // ------------ Model creation ---------------
        GDPRModelBuilder builder = new GDPRModelBuilder();
        builder.createStoringElement("Storing");

        // -------- Context Dependent Attribute -------------------
        List<String> values = new ArrayList<>(parameter.getModelSize());
        for (int i = 0; i < parameter.getModelSize(); i++) {
            values.add("Value" + i);
        }
        Property property = builder.createProperty("Type", values);
        PropertyAnnotation propertyAnnotation = builder.createPropertyAnnotation(builder.getFirstElement(), property);
        ContextAnnotation contextAnnotation = builder.createContextAnnotation("Annotation", property.getPropertyvalue(), propertyAnnotation);
        builder.createContextDefinition("Definition", builder.getDefaultController(), contextAnnotation);

        // ------------ Analysis Execution ------------------
        analysisExecutor.executeAnalysis(parameter, builder);
    }

    @Override
    public String getTestName() {
        return "NodeDefinedContextDependentAttribute";
    }

}
