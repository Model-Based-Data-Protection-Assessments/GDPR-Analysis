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

public class GDPRURIResourceProvider extends GDPRResourceProvider {
    private final URI modelURI;
    private final URI propertyURI;
    private LegalAssessmentFacts model;
    private ContextDependentProperties contextDependentProperties;
    private final TransformationManager transformationManager;

    public GDPRURIResourceProvider(URI modelURI, URI propertyURI) {
        this.modelURI = modelURI;
        this.propertyURI = propertyURI;
        this.transformationManager = new TransformationManager();
    }

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
    public LegalAssessmentFacts getModel() {
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
