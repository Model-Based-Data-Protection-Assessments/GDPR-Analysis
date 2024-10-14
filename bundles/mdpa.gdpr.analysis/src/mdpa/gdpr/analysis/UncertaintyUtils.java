package mdpa.gdpr.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behaviour;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.util.EcoreUtil;

import mdpa.gdpr.analysis.core.ContextDependentAttributeScenario;
import mdpa.gdpr.analysis.core.ContextDependentAttributeSource;
import mdpa.gdpr.analysis.dfd.DFDGDPRVertex;
import mdpa.gdpr.metamodel.GDPR.Data;
import mdpa.gdpr.metamodel.GDPR.NaturalPerson;
import mdpa.gdpr.metamodel.GDPR.PersonalData;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.GDPRContextElement;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

public class UncertaintyUtils {
	private static final Logger logger = Logger.getLogger(UncertaintyUtils.class);
	
	public static Behaviour createBehaviour(DFDGDPRVertex impactedElement, DataDictionary dd, ContextDependentAttributeSource source, ContextDependentAttributeScenario scenario, Data targetedData) {
		logger.setLevel(Level.INFO);
		
		if (impactedElement.getReferencedElement().getBehaviour().getOutPin().stream()
				.noneMatch(it -> it.getEntityName().equals(targetedData.getEntityName()))) {
			logger.info("Scenario" + scenario.getName() + " does not impact " + impactedElement.getName());
			return impactedElement.getReferencedElement().getBehaviour();
		}
		
		logger.debug("Impacting element " + impactedElement.getReferencedElement().getEntityName());
		Behaviour behaviour = datadictionaryFactory.eINSTANCE.createBehaviour();
		dd.getBehaviour().add(behaviour);
		
		if (impactedElement.getReferencedElement().getBehaviour().getOutPin().isEmpty()) {
			return behaviour;
		}
		
		behaviour.getInPin().addAll(impactedElement.getReferencedElement().getBehaviour().getInPin().stream()
				.map(it -> EcoreUtil.copy(it))
				.toList());
		behaviour.getOutPin().addAll(impactedElement.getReferencedElement().getBehaviour().getOutPin().stream()
				.map(it -> EcoreUtil.copy(it))
				.toList());

		LabelType type = dd.getLabelTypes().stream()
				.filter(it -> it.getEntityName().equals(source.getPropertyType().getEntityName()))
				.findAny().orElseThrow();
		List<Label> values = new ArrayList<>();
		for(PropertyValue propertyValue : scenario.getPropertyValues()) {
			Label value = type.getLabel().stream().filter(it -> it.getEntityName().equals(propertyValue.getEntityName())).findAny().orElseThrow();
			values.add(value);
		}
		
		List<Pin> inputPins = behaviour.getInPin().stream()
				.map(it -> EcoreUtil.copy(it))
				.toList();
		Pin outputPin = behaviour.getOutPin().stream()
				.filter(it -> it.getEntityName().equals(targetedData.getEntityName()))
				.map(it -> EcoreUtil.copy(it))
				.findAny().orElseThrow();
		
		List<Assignment> assignments = new ArrayList<>();
		Assignment attributeAssignment = datadictionaryFactory.eINSTANCE.createAssignment();
		attributeAssignment.setTerm(datadictionaryFactory.eINSTANCE.createTRUE());
		attributeAssignment.getInputPins().addAll(inputPins);
		attributeAssignment.setOutputPin(outputPin);
		attributeAssignment.getOutputLabels().addAll(values);
		assignments.add(attributeAssignment);
		
		//Set Natural Person Data attributes
		for (Data data : impactedElement.getOutgoingData()) {
			if (!(data instanceof PersonalData personalData)) {
				continue;
			}
			Assignment assignment = datadictionaryFactory.eINSTANCE.createAssignment();
			assignment.getInputPins().addAll(inputPins);
			Pin dataOutputPin = impactedElement.getReferencedElement().getBehaviour().getOutPin().stream()
					.filter(pin -> pin.getEntityName().equals(personalData.getEntityName()))
					.findAny().orElseThrow();
			assignment.setOutputPin(dataOutputPin);
			assignment.setTerm(datadictionaryFactory.eINSTANCE.createTRUE());
			personalData.getDataReferences().forEach(person -> {
				LabelType labelType = dd.getLabelTypes().stream()
							.filter(it -> it.getEntityName().equals("NaturalPerson"))
							.findAny().orElseThrow();
				Label label = labelType.getLabel().stream()
							.filter(it -> it.getEntityName().equals(person.getEntityName()))
							.findAny().orElseThrow();
				assignment.getOutputLabels().add(label);
			});
			assignment.setEntityName("Send " + personalData.getEntityName());
			assignments.add(assignment);
		}
		behaviour.getAssignment().addAll(assignments);
		return behaviour;
	}

	public static Behaviour createBehaviour(DFDGDPRVertex impactedElement, DataDictionary dd, ContextDependentAttributeSource source, ContextDependentAttributeScenario scenario, NaturalPerson targetedPerson) {
		logger.setLevel(Level.WARN);
		logger.debug("Impacting element " + impactedElement.getReferencedElement().getEntityName());
		Behaviour behaviour = datadictionaryFactory.eINSTANCE.createBehaviour();
		dd.getBehaviour().add(behaviour);
		
		if (impactedElement.getReferencedElement().getBehaviour().getOutPin().isEmpty()) {
			return behaviour;
		}

		behaviour.getInPin().addAll(impactedElement.getReferencedElement().getBehaviour().getInPin().stream()
				.map(it -> EcoreUtil.copy(it))
				.toList());
		behaviour.getOutPin().addAll(impactedElement.getReferencedElement().getBehaviour().getOutPin().stream()
				.map(it -> EcoreUtil.copy(it))
				.toList());
		
		List<Assignment> assignments = new ArrayList<>();
		List<PersonalData> targetedData = impactedElement.getOutgoingData().stream()
				.filter(PersonalData.class::isInstance)
				.map(PersonalData.class::cast)
				.distinct()
				.filter(it -> it.getDataReferences().contains(targetedPerson))
				.toList();
		
		for (PersonalData targetData : targetedData) {
			LabelType type = dd.getLabelTypes().stream()
					.filter(it -> it.getEntityName().equals(source.getPropertyType().getEntityName()))
					.findAny().orElseThrow();
			List<Label> values = new ArrayList<>();
			for(PropertyValue propertyValue : scenario.getPropertyValues()) {
				Label value = type.getLabel().stream().filter(it -> it.getEntityName().equals(propertyValue.getEntityName())).findAny().orElseThrow();
				values.add(value);
			}
			
			List<Pin> inputPins = behaviour.getInPin();
			Pin outputPin = behaviour.getOutPin().stream()
					.filter(it -> it.getEntityName().equals(targetData.getEntityName()))
					.findAny().orElseThrow();
			Assignment attributeAssignment = datadictionaryFactory.eINSTANCE.createAssignment();
			attributeAssignment.setTerm(datadictionaryFactory.eINSTANCE.createTRUE());
			attributeAssignment.getInputPins().addAll(inputPins);
			attributeAssignment.setOutputPin(outputPin);
			attributeAssignment.getOutputLabels().addAll(values);
			assignments.add(attributeAssignment);
			
			//Set Natural Person Data attributes
			for (Data data : impactedElement.getOutgoingData()) {
				if (!(data instanceof PersonalData personalData)) {
					continue;
				}
				Assignment assignment = datadictionaryFactory.eINSTANCE.createAssignment();
				assignment.getInputPins().addAll(inputPins);
				Pin dataOutputPin = impactedElement.getReferencedElement().getBehaviour().getOutPin().stream()
						.filter(pin -> pin.getEntityName().equals(personalData.getEntityName()))
						.findAny().orElseThrow();
				assignment.setOutputPin(dataOutputPin);
				assignment.setTerm(datadictionaryFactory.eINSTANCE.createTRUE());
				personalData.getDataReferences().forEach(person -> {
					LabelType labelType = dd.getLabelTypes().stream()
								.filter(it -> it.getEntityName().equals("NaturalPerson"))
								.findAny().orElseThrow();
					Label label = labelType.getLabel().stream()
								.filter(it -> it.getEntityName().equals(person.getEntityName()))
								.findAny().orElseThrow();
					assignment.getOutputLabels().add(label);
				});
				assignment.setEntityName("Send " + personalData.getEntityName());
				assignments.add(assignment);
			}
			behaviour.getAssignment().addAll(assignments);
		}
		return behaviour;
	}
	
	public static boolean matchesContextDefinition(DFDGDPRVertex vertex, ContextDefinition contextDefinition) {
		return contextDefinition.getGdprElements().stream().allMatch(it -> UncertaintyUtils.matchesContextElement(vertex, it));
	}
	
	public static boolean matchesContextElement(DFDGDPRVertex vertex, GDPRContextElement contextElement) {
		boolean matches = vertex.getRelatedElements().contains(contextElement.getGdprElement());
		if (contextElement.isNegated()) {
			return !matches;
		} else {
			return matches;
		}
	}
}
