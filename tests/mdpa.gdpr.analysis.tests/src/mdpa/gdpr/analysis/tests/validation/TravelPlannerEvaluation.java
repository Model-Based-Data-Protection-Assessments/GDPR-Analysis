package mdpa.gdpr.analysis.tests.validation;

import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.Test;

import mdpa.gdpr.analysis.dfd.DFDGDPRFlowGraphCollection;
import mdpa.gdpr.analysis.dfd.DFDGDPRTransposeFlowGraph;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;

public class TravelPlannerEvaluation extends ValidationBase {
	private Logger logger = Logger.getLogger(TravelPlannerEvaluation.class);

	public TravelPlannerEvaluation() {
		super("default", "models/TravelPlanner");
	}
	

	@Test
	public void testFlowGraphAmount() {
		DFDGDPRFlowGraphCollection flowGraphs = (DFDGDPRFlowGraphCollection) this.analysis.findFlowGraphs();
		var alternateFlowGraphs = flowGraphs.resolveContextDependentAttributes();
		
		logger.info("Number of TFGs: " +  alternateFlowGraphs.getTransposeFlowGraphs().size());
		for (var tfg : alternateFlowGraphs.getTransposeFlowGraphs()) {
			var gdprTFG = (DFDGDPRTransposeFlowGraph) tfg;
			System.out.println("---- State: " + gdprTFG.getContextAttributeState() + " -----------");
			for (var vertex : tfg.getVertices()) {
				var referencedElement = (Node) vertex.getReferencedElement();
				System.out.println(referencedElement.getEntityName() + ", " + referencedElement.getId());
			}
			System.out.println();
		}
	}
	
	@Test
	public void testImpactAmount() {
		DFDGDPRFlowGraphCollection flowGraphs = (DFDGDPRFlowGraphCollection) this.analysis.findFlowGraphs();
		var alternateFlowGraphs = flowGraphs.resolveContextDependentAttributes();
		
		for(DFDGDPRTransposeFlowGraph transposeFlowGraph : alternateFlowGraphs.getTransposeFlowGraphs().stream()
				.filter(DFDGDPRTransposeFlowGraph.class::isInstance)
				.map(DFDGDPRTransposeFlowGraph.class::cast)
				.toList()) {
			var impactedElements = this.getImpactedElements(transposeFlowGraph);
			System.out.println("---- State: " + transposeFlowGraph.getContextAttributeState() + " -----------");
			System.out.println("---- Impacted Elements: -----");
			for (var vertex : impactedElements) {
				var referencedElement = (Node) vertex.getReferencedElement();
				System.out.println(referencedElement.getEntityName() + ", " + referencedElement.getId());
			}
			System.out.println();
		}
	}
	
	@Test
	public void testViolations() {
		//logger.setLevel(Level.INFO);
		DFDGDPRFlowGraphCollection flowGraphs = (DFDGDPRFlowGraphCollection) this.analysis.findFlowGraphs();
		var alternateFlowGraphs = flowGraphs.resolveContextDependentAttributes();
		alternateFlowGraphs.evaluate();
		for (DFDGDPRTransposeFlowGraph flowGraph : alternateFlowGraphs.getTransposeFlowGraphs().stream()
				.filter(DFDGDPRTransposeFlowGraph.class::isInstance)
				.map(DFDGDPRTransposeFlowGraph.class::cast)
				.toList()) {
			logger.debug("Starting new TFG with state" + flowGraph.getContextAttributeState().toString());
			var violations = this.analysis.queryDataFlow(flowGraph, it -> {
				var element = (DFDGDPRVertex) it;
				logger.debug("Element: " + ((DFDGDPRVertex) it).getName());
				for(var dc : element.getAllDataCharacteristics()) {
					String result = dc.getAllCharacteristics().stream()
							.map(ch -> ch.getTypeName() + "." + ch.getValueName())
							.collect(Collectors.joining(","));
					logger.debug("DC: " + dc.getVariableName() + " with values [" + result + "]");
				}
					String result = element.getAllVertexCharacteristics().stream()
							.map(ch -> ch.getTypeName() + "." + ch.getValueName())
							.collect(Collectors.joining(","));
					logger.debug("VCs: [" + result + "]");
				logger.debug("Responsibility: " + element.getResponsibilityRole().getEntityName());
				// C1: Not Necessary Data
				if (this.hasDataCharacteristic(element, "Necessary", "False")) {
					return true;
				}
				return false;
			});
			if (violations.isEmpty()) {
				logger.debug("No violations found!");
				logger.debug("------------------------");
				continue;
			}
			var sourcesString = flowGraph.getContextAttributeState().getSelectedScenarios().stream()
					.map(it -> it.getName())
					.collect(Collectors.joining(","));
			logger.info("Violation in state: " + sourcesString);
			logger.info("Violating vertices:" + violations);
			logger.info("------------------------");
		}
	}
}
