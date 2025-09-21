package mdpa.gdpr.analysis.core.resource;

import mdpa.gdpr.analysis.core.TransformationManager;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

public abstract class GDPRResourceProvider extends ResourceProvider {
    @Override
    public void setupResources() {

    }

    public abstract LegalAssessmentFacts getModel();

    public abstract ContextDependentProperties getContextDependentProperties();

    public abstract TransformationManager getTransformationManager();

    @Override
    public boolean sufficientResourcesLoaded() {
        return this.getModel() != null;
    }
}
