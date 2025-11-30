package mdpa.gdpr.analysis.core.resource;

import mdpa.gdpr.analysis.core.TransformationManager;
import mdpa.gdpr.metamodel.GDPR.GDPRPackage;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.contextproperties.ContextpropertiesPackage;
import mdpa.gdpr.metamodel.contextproperties.ScopeDependentAssessmentFacts;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * A {@link ResourceProvider} providing the necessary resources to run a
 * {@link mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysis}
 */
public abstract class GDPRResourceProvider extends ResourceProvider {
    @Override
    public void setupResources() {
        this.resources.getPackageRegistry()
                .put(GDPRPackage.eNS_URI, GDPRPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(GDPRPackage.eNAME, new XMIResourceFactoryImpl());
        this.resources.getPackageRegistry()
                .put(ContextpropertiesPackage.eNS_URI, ContextpropertiesPackage.eINSTANCE);
        this.resources.getResourceFactoryRegistry()
                .getExtensionToFactoryMap()
                .put(ContextpropertiesPackage.eNAME, new XMIResourceFactoryImpl());
    }

    /**
     * Returns the loaded GDPR model
     * @return Returns the GDPR model that is loaded by the resource provider
     */
    public abstract LegalAssessmentFacts getGDPRModel();

    /**
     * Returns the {@link ScopeDependentAssessmentFacts} metamodel that is required to run a
     * {@link mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysis}
     * @return Returns the loaded Context Property model
     */
    public abstract ScopeDependentAssessmentFacts getScopeDependentAssessmentFacts();

    /**
     * Returns the transformation manager that should be used for the transformation from gdpr to dfd
     * @return Returns the {@link TransformationManager} of the running analysis
     */
    public abstract TransformationManager getTransformationManager();

    @Override
    public boolean sufficientResourcesLoaded() {
        return this.getGDPRModel() != null;
    }
}
