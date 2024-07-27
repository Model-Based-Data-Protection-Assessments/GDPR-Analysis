package mdpa.gdpr.analysis.validation.exporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.ScalibilityCaseRunner;
import mdpa.gdpr.analysis.validation.ScalibilityEvent;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.analysis.validation.cases.AbstractScalibilityCase;

public class ResultExporter {

	public void exportResults(List<AbstractScalibilityCase> tests, AnalysisExecutor analysisExecutor) {
		tests.forEach(it -> this.exportResult(it, analysisExecutor));
	}
	
	public void exportResult(AbstractScalibilityCase test, AnalysisExecutor analysisExecutor) {
		try {
			FileInputStream input = new FileInputStream(ScalibilityCaseRunner.BASE_PATH +  "/results/" + test.getTestName() + ".ser");
			ObjectInputStream inputObjects = new ObjectInputStream(input);
			List<ScalibilityParameter> inputData = (ArrayList<ScalibilityParameter>) inputObjects.readObject();
			Instant timestamp = Instant.now();
			FileOutputStream output = new FileOutputStream(ScalibilityCaseRunner.BASE_PATH + "/results/" + test.getTestName() + timestamp.toString() + ".csv");
			this.writeHeader(output);
			for (ScalibilityParameter parameter : inputData) {
				if(!parameter.getTestName().equals(test.getTestName())) {
					continue;
				}
				exportParameter(parameter, output);
				output.write(System.lineSeparator().getBytes());
			}
			inputObjects.close();
			input.close();
			System.out.println("Exported test " + test.getTestName());
		} catch (FileNotFoundException e) {
			System.out.println("Skipping test " + test.getTestName());
			return;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeHeader(FileOutputStream file) {
		StringJoiner string = new StringJoiner(",");
		string.add("TestName");
		string.add("ModelSize");
		string.add(ScalibilityEvent.ANALYSIS_INITIALZATION.getName());
		string.add(ScalibilityEvent.TFG_FINDING.getName());
		string.add(ScalibilityEvent.ATTRIBUTE_RESOLVING.getName());
		string.add(ScalibilityEvent.PROPAGATION.getName());
		string.add("Total");
		
		try {
			file.write((string.toString() + System.lineSeparator()).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void exportParameter(ScalibilityParameter parameter, FileOutputStream file) {
		long start = parameter.getStartDate().toInstant().toEpochMilli();
		
		StringJoiner string = new StringJoiner(",");
		string.add(parameter.getTestName());
		string.add(Integer.toString(parameter.getModelSize()));
		string.add(Long.toString(parameter.getScalibilityEvents().get(ScalibilityEvent.ANALYSIS_INITIALZATION).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getScalibilityEvents().get(ScalibilityEvent.TFG_FINDING).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getScalibilityEvents().get(ScalibilityEvent.ATTRIBUTE_RESOLVING).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getScalibilityEvents().get(ScalibilityEvent.PROPAGATION).toInstant().toEpochMilli() - start));
		string.add(Long.toString(parameter.getStopDate().toInstant().toEpochMilli() - start));
		
		try {
			file.write(string.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
