package mdpa.gdpr.analysis.validation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import mdpa.gdpr.analysis.validation.cases.AbstractScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.ContextDefinitionAmountScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.ContextDefinitionSizeScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.DataDefinedContextDependentAttributeScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.DataScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.DataUndefinedContextDependentAttributeScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.NodeDefinedContextDependentAttributeScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.NodeUndefinedContextDependentAttribute;
import mdpa.gdpr.analysis.validation.cases.PropagationScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.PurposeScalibilityCase;
import mdpa.gdpr.analysis.validation.cases.RoleScalibilityCase;
import mdpa.gdpr.analysis.validation.exporter.GraphExporter;
import mdpa.gdpr.analysis.validation.exporter.ResultExporter;
import org.apache.log4j.Logger;

public class ScalibilityCaseRunner {
    public final static String BASE_PATH = ".";

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("-export")) {
            ResultExporter exporter = new ResultExporter();
            exporter.exportResults(ScalibilityCaseRunner.getAllTests(), new AnalysisExecutor());
        } else if (args.length > 1 && args[0].equalsIgnoreCase("-start")) {
            int start = Integer.parseInt(args[1]);
            ScalibilityCaseRunner runner = new ScalibilityCaseRunner(ScalibilityCaseRunner.getTests(), new AnalysisExecutor());
            runner.runTests(start);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("-graph")) {
            GraphExporter exporter = new GraphExporter();
            exporter.exportResults(ScalibilityCaseRunner.getAllTests(), new AnalysisExecutor());
        } else {
            ScalibilityCaseRunner runner = new ScalibilityCaseRunner(ScalibilityCaseRunner.getTests(), new AnalysisExecutor());
            runner.runTests();
        }
    }

    private static final int RUNS_PER_STAGE = 10;
    private static final int ITERATIONS = 5;
    private final Logger logger = Logger.getLogger(ScalibilityCaseRunner.class);

    private List<AbstractScalibilityCase> tests;
    private List<ScalibilityParameter> results;
    private AnalysisExecutor analysisExecutor;

    public ScalibilityCaseRunner(List<AbstractScalibilityCase> tests, AnalysisExecutor analysisExecutor) {
        this.tests = tests;
        this.analysisExecutor = analysisExecutor;
        this.results = new ArrayList<>();
    }

    public void runTests() {
        this.runTests(0);
    }

    public void runTests(int start) {
        for (int i = start; i < ITERATIONS; i++) {
            for (AbstractScalibilityCase test : this.tests) {
                logger.info("Running test with name " + test.getTestName());
                this.runTest(test, i);
            }
        }
    }

    private void runTest(AbstractScalibilityCase test, int index) {
        for (int i = 0; i < 5; i++) {
            logger.info("Running warmup " + i + "/5");
            ScalibilityParameter parameter = new ScalibilityParameter(10, test.getTestName());
            test.runScalibilityCase(parameter, analysisExecutor);
        }
        for (int j = 0; j < RUNS_PER_STAGE; j++) {
            int modelSize = test.getScalibilityStep(index);
            String modelName = test.getTestName();
            logger.info("Running test with model " + modelName + " and size " + modelSize + ", Iteration: " + j);
            ScalibilityParameter parameter = new ScalibilityParameter(modelSize, test.getTestName());
            test.runScalibilityCase(parameter, analysisExecutor);
            this.results.add(parameter);
            saveResults(parameter.getTestName());
        }
    }

    private void saveResults(String testName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(ScalibilityCaseRunner.BASE_PATH + "/results/" + testName + ".ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this.results);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<AbstractScalibilityCase> getTests() {
        return List.of(new ContextDefinitionAmountScalibilityCase(), new ContextDefinitionSizeScalibilityCase(),
                new DataDefinedContextDependentAttributeScalibilityCase(), new DataScalibilityCase(),
                new DataUndefinedContextDependentAttributeScalibilityCase(), new NodeDefinedContextDependentAttributeScalibilityCase(),
                new NodeUndefinedContextDependentAttribute(), new PropagationScalibilityCase(), new PurposeScalibilityCase(),
                new RoleScalibilityCase());
    }

    public static List<AbstractScalibilityCase> getAllTests() {
        return List.of(new ContextDefinitionAmountScalibilityCase(), new ContextDefinitionSizeScalibilityCase(),
                new DataDefinedContextDependentAttributeScalibilityCase(), new DataScalibilityCase(),
                new DataUndefinedContextDependentAttributeScalibilityCase(), new NodeDefinedContextDependentAttributeScalibilityCase(),
                new NodeUndefinedContextDependentAttribute(), new PropagationScalibilityCase(), new PurposeScalibilityCase(),
                new RoleScalibilityCase());
    }
}
