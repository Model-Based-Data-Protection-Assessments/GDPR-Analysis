package mdpa.gdpr.analysis.validation.exporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import mdpa.gdpr.analysis.validation.AnalysisExecutor;
import mdpa.gdpr.analysis.validation.ScalibilityCaseRunner;
import mdpa.gdpr.analysis.validation.ScalibilityEvent;
import mdpa.gdpr.analysis.validation.ScalibilityParameter;
import mdpa.gdpr.analysis.validation.cases.AbstractScalibilityCase;

public class GraphExporter {

    public void exportResults(List<AbstractScalibilityCase> tests, AnalysisExecutor analysisExecutor) {
        tests.forEach(it -> this.exportResult(it, analysisExecutor));
    }

    public void exportResult(AbstractScalibilityCase test, AnalysisExecutor analysisExecutor) {
        try {
            FileInputStream input = new FileInputStream(ScalibilityCaseRunner.BASE_PATH + "/results/" + test.getTestName() + ".ser");
            ObjectInputStream inputObjects = new ObjectInputStream(input);
            List<ScalibilityParameter> inputData = (ArrayList<ScalibilityParameter>) inputObjects.readObject();
            Instant timestamp = Instant.now();
            FileOutputStream output = new FileOutputStream(
                    ScalibilityCaseRunner.BASE_PATH + "/results/graphs/" + test.getTestName() + timestamp.toString() + ".csv");
            this.writeHeader(output);
            Map<String, List<ScalibilityParameter>> indexedData = new HashMap<>();
            for (ScalibilityParameter parameter : inputData) {
                if (!parameter.getTestName()
                        .equals(test.getTestName())) {
                    continue;
                }
                String key = parameter.getTestName() + parameter.getModelSize();
                if (indexedData.containsKey(key)) {
                    indexedData.get(key)
                            .add(parameter);
                } else {
                    List<ScalibilityParameter> data = new ArrayList<>();
                    data.add(parameter);
                    indexedData.put(key, data);
                }
            }
            TreeMap<String, List<ScalibilityParameter>> sortedData = new TreeMap<>(indexedData);
            var parametersSorted = new ArrayList<>(sortedData.values());
            parametersSorted.sort((List<ScalibilityParameter> list1, List<ScalibilityParameter> list2) -> list1.get(0)
                    .compareTo(list2.get(0)));
            for (List<ScalibilityParameter> parameters : parametersSorted) {
                if (parameters.size() != 10) {
                    continue;
                }
                exportParameter(parameters, output);
                output.write(System.lineSeparator()
                        .getBytes());
            }
            inputObjects.close();
            input.close();
            System.out.println("Created Graph data: " + test.getTestName());
        } catch (FileNotFoundException e) {
            System.out.println("Skipping test: " + test.getTestName());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeHeader(FileOutputStream file) {
        StringJoiner string = new StringJoiner(",");
        string.add("index");
        string.add("median");
        string.add("box_top");
        string.add("box_bottom");
        string.add("whisker_top");
        string.add("whisker_bottom");

        try {
            file.write((string.toString() + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportParameter(List<ScalibilityParameter> parameters, FileOutputStream file) {
        long whisherTop = 0;
        long whiskerBottom = Long.MAX_VALUE;
        List<Long> totals = new ArrayList<>();
        for (ScalibilityParameter parameter : parameters) {
            long total = parameter.getStopDate()
                    .toInstant()
                    .toEpochMilli()
                    - parameter.getScalibilityEvents()
                            .get(ScalibilityEvent.ANALYSIS_INITIALZATION)
                            .toInstant()
                            .toEpochMilli();
            totals.add(total);
            whisherTop = Math.max(whisherTop, total);
            whiskerBottom = Math.min(whiskerBottom, total);
        }
        Collections.sort(totals);
        long median = totals.get(totals.size() / 2);
        long boxTop = totals.get((int) (totals.size() - (0.25f * totals.size())));
        long boxBottom = totals.get((int) (totals.size() - (0.75f * totals.size())));
        StringJoiner string = new StringJoiner(",");
        string.add(Long.toString(parameters.get(0)
                .getModelSize()));
        string.add(Long.toString(median));
        string.add(Long.toString(boxTop));
        string.add(Long.toString(boxBottom));
        string.add(Long.toString(whisherTop));
        string.add(Long.toString(whiskerBottom));

        try {
            file.write(string.toString()
                    .getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}