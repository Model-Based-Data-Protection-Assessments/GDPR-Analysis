package mdpa.gdpr.analysis.validation;

import mdpa.gdpr.analysis.core.TransformationManager;
import mdpa.gdpr.analysis.core.resource.GDPRResourceProvider;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;

public class MemoryGDPRResourceProvider extends GDPRResourceProvider {
    private GDPRModelBuilder modelBuilder;
    private final TransformationManager transformationManager = new TransformationManager();

    public MemoryGDPRResourceProvider(GDPRModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    @Override
    public LegalAssessmentFacts getModel() {
        return modelBuilder.getGdprModel();
    }

    @Override
    public ContextDependentProperties getContextDependentProperties() {
        return modelBuilder.getContextDependentAttributes();
    }

    @Override
    public TransformationManager getTransformationManager() {
        return this.transformationManager;
    }

    @Override
    public void loadRequiredResources() {

    }

}
