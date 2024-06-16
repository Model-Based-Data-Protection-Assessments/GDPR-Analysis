package mdpa.gdpr.analysis.tests;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;

import mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysis;
import mdpa.gdpr.analysis.GDPRLegalAssessmentAnalysisBuilder;
import mdpa.gdpr.analysis.testmodels.Activator;

public abstract class BaseTest {
	public static final String TEST_MODEL_PROJECT_NAME = "mdpa.gdpr.analysis.testmodels";
	protected GDPRLegalAssessmentAnalysis analysis = null;

	abstract String getFolderName();
	abstract String getFilesName();
	
	protected String getBaseFolder() {
		return "models";
	}
	
	@BeforeEach
    public void setup() {
        final var gdprModelPath = Paths.get(getBaseFolder(), getFolderName(), getFilesName() + ".gdpr")
                .toString();
        final var propertyPath = Paths.get(getBaseFolder(), getFolderName(), getFilesName() + ".contextproperties")
                .toString();
        var builder = new GDPRLegalAssessmentAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useGDPRModel(gdprModelPath)
                .useProperties(propertyPath);

        GDPRLegalAssessmentAnalysis analysis = builder.build();
        analysis.initializeAnalysis();

        this.analysis = analysis;
    }
}
