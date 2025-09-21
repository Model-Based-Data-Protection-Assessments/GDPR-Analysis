package mdpa.gdpr.analysis.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mdpa.gdpr.analysis.dfd.DFDGDPRFlowGraphCollection;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import org.junit.jupiter.api.Test;

public class BankingTest extends BaseTest {

    @Override
    String getFolderName() {
        return "Banking";
    }

    @Override
    String getFilesName() {
        return "default";
    }

    @Test
    public void testAnalysis() {
        DFDGDPRFlowGraphCollection flowGraphs = (DFDGDPRFlowGraphCollection) analysis.findFlowGraphs();
        DFDGDPRFlowGraphCollection resolvedFlowGraphs = flowGraphs.resolveContextDependentAttributes();
        resolvedFlowGraphs.evaluate();

        List<DFDGDPRVertex> violations = resolvedFlowGraphs.getTransposeFlowGraphs()
                .stream()
                .map(it -> analysis.queryDataFlow(it, vertex -> true))
                .flatMap(List::stream)
                .map(DFDGDPRVertex.class::cast)
                .toList();

        // Smoke test
        assertTrue(true);
    }
}
