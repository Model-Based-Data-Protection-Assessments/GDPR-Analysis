package mdpa.gdpr.analysis.core.resource;

import java.util.ArrayList;
import java.util.List;
import mdpa.gdpr.analysis.core.TransformationManager;
import mdpa.gdpr.metamodel.GDPR.GDPRPackage;
import mdpa.gdpr.metamodel.GDPR.LegalAssessmentFacts;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import mdpa.gdpr.metamodel.contextproperties.ContextpropertiesPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Implementation of an {@link GDPRResourceProvider} using modelling project URIs to load the required models
 */
public class GDPRURIResourceProvider extends GDPRResourceProvider {
    private final URI modelURI;
    private final URI propertyURI;
    private LegalAssessmentFacts model;
    private ContextDependentProperties contextDependentProperties;
    private final TransformationManager transformationManager;

    /**
     * Creates a new {@link GDPRURIResourceProvider} using the provided URIs to the models
     * <p/>
     * Usually, the resource provider will be created automatically when using {@link mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysisBuilder}
     * @param modelURI URI path to the GDPR model
     * @param propertyURI URI path to the context property model
     */
    public GDPRURIResourceProvider(URI modelURI, URI propertyURI) {
        this.modelURI = modelURI;
        this.propertyURI = propertyURI;
        this.transformationManager = new TransformationManager();
    }

    @Override
    public void loadRequiredResources() {
        this.model = (LegalAssessmentFacts) this.loadModelContent(modelURI);
        this.contextDependentProperties = (ContextDependentProperties) this.loadModelContent(propertyURI);
        List<Resource> loadedResources;
        do {
            loadedResources = new ArrayList<>(this.resources.getResources());
            loadedResources.forEach(EcoreUtil::resolveAll);
        } while (loadedResources.size() != this.resources.getResources()
                .size());
    }

    @Override
    public LegalAssessmentFacts getGDPRModel() {
        return this.model;
    }

    @Override
    public ContextDependentProperties getContextDependentProperties() {
        return this.contextDependentProperties;
    }

    @Override
    public TransformationManager getTransformationManager() {
        return this.transformationManager;
    }
}
