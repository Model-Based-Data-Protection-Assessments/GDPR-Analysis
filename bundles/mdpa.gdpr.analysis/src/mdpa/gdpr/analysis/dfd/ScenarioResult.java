package mdpa.gdpr.analysis.dfd;

import mdpa.gdpr.analysis.core.ContextAttributeState;

import java.util.List;
import java.util.Optional;

public record ScenarioResult(Optional<DFDGDPRTransposeFlowGraph> transposeFlowGraph, List<ContextAttributeState> states) {
}
