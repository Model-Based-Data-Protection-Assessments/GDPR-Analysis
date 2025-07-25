package mdpa.gdpr.analysis;

import mdpa.gdpr.analysis.core.resource.GDPRResourceProvider;
import mdpa.gdpr.analysis.dfd.DFDGDPRFlowGraphCollection;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

import java.util.Optional;

public class GDPRLegalAssessmentAnalysis extends DataFlowConfidentialityAnalysis {
	public static final String PLUGIN_PATH = "mdpa.gdpr.analysis";
	
	private final Logger logger = Logger.getLogger(GDPRLegalAssessmentAnalysis.class);

	private final GDPRResourceProvider resourceProvider;
	private final Optional<Class<? extends Plugin>> modelProjectActivator;
	private final String modelProjectName;

	public GDPRLegalAssessmentAnalysis(GDPRResourceProvider resourceProvider, Optional<Class<? extends Plugin>> modelProjectActivator,
										String modelProjectName) {
		this.resourceProvider = resourceProvider;
		this.modelProjectActivator = modelProjectActivator;
		this.modelProjectName = modelProjectName;
	}

	@Override
	public void initializeAnalysis() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
				.put("gdpr", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
				.put("contextproperties", new XMIResourceFactoryImpl());
		
		EcorePlugin.ExtensionProcessor.process(null);

		try {
			super.setupLoggers();
			var initializationBuilder = StandaloneInitializerBuilder.builder()
					.registerProjectURI(GDPRLegalAssessmentAnalysis.class, GDPRLegalAssessmentAnalysis.PLUGIN_PATH);

			this.modelProjectActivator
					.ifPresent(projectActivator -> initializationBuilder.registerProjectURI(projectActivator, this.modelProjectName));

			initializationBuilder.build()
					.init();

			logger.info("Successfully initialized standalone environment for the legal assessment analysis.");

		} catch (StandaloneInitializationException e) {
			logger.error("Could not initialize analysis", e);
			throw new IllegalStateException("Could not initialize analysis");
		}
		this.resourceProvider.loadRequiredResources();
		if (!this.resourceProvider.sufficientResourcesLoaded()) {
			logger.error("Insufficient amount of resources loaded");
			throw new IllegalStateException("Could not initialize analysis");
		}
	}

	@Override
	public FlowGraphCollection findFlowGraphs() {
		return new DFDGDPRFlowGraphCollection(this.resourceProvider);
	}

	@Override
	public void setLoggerLevel(Level level) {
		logger.setLevel(level);
	}
}
