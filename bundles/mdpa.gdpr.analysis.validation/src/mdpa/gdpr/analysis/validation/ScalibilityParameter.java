package mdpa.gdpr.analysis.validation;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScalibilityParameter implements Serializable, Comparable<ScalibilityParameter> {
    private static final long serialVersionUID = 1L;

    private final int modelSize;
    private final String testName;

    private Date startDate;
    private Date stopDate;
    private final Map<ScalibilityEvent, Date> scalibilityEvents;

    public ScalibilityParameter(int modelSize, String testName) {
        this.modelSize = modelSize;
        this.testName = testName;
        this.scalibilityEvents = new HashMap<>();
    }

    public void startTiming() {
        this.startDate = Date.from(Instant.now());
    }

    public void stopTiming() {
        this.stopDate = Date.from(Instant.now());
    }

    public void recordScalibilityEvent(ScalibilityEvent scalibilityEvent) {
        this.scalibilityEvents.put(scalibilityEvent, Date.from(Instant.now()));
    }

    public int getModelSize() {
        return modelSize;
    }

    public String getTestName() {
        return testName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public Map<ScalibilityEvent, Date> getScalibilityEvents() {
        return scalibilityEvents;
    }

    @Override
    public int compareTo(ScalibilityParameter other) {
        return Integer.compare(modelSize, other.getModelSize());
    }
}
