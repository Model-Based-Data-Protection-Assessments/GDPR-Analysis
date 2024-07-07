package mdpa.gdpr.analysis.validation;

import mdpa.gdpr.metamodel.GDPR.*;
import mdpa.gdpr.metamodel.contextproperties.ContextAnnotation;
import mdpa.gdpr.metamodel.contextproperties.ContextDefinition;
import mdpa.gdpr.metamodel.contextproperties.ContextDependentProperties;
import mdpa.gdpr.metamodel.contextproperties.ContextpropertiesFactory;
import mdpa.gdpr.metamodel.contextproperties.GDPRContextElement;
import mdpa.gdpr.metamodel.contextproperties.Property;
import mdpa.gdpr.metamodel.contextproperties.PropertyAnnotation;
import mdpa.gdpr.metamodel.contextproperties.PropertyValue;

import java.util.List;
import java.util.UUID;

public class GDPRModelBuilder {
    private final LegalAssessmentFacts gdprModel;
    private final ContextDependentProperties contextDependentAttributes;

    private Processing lastElement;
    private final Controller defaultController;
    private final Purpose defaultPurpose;
    private final LegalBasis defaultLegalBasis;
    private final PersonalData defaultPersonalData;
    private final NaturalPerson defaultNaturalPerson;

    public GDPRModelBuilder() {
        this.gdprModel = GDPRFactory.eINSTANCE.createLegalAssessmentFacts();
        this.contextDependentAttributes = ContextpropertiesFactory.eINSTANCE.createContextDependentProperties();
        this.defaultController = this.createController("Default Controller");
        this.defaultPurpose = this.createPurpose("Default Purpose");
        this.defaultNaturalPerson = this.createNaturalPerson("Default Natural Person");
        this.defaultLegalBasis = this.createConsentLegalBasis("Default Legal Basis", this.defaultPurpose, this.defaultNaturalPerson);
        this.defaultPersonalData = this.createPersonalData("Default Personal Data", this.defaultNaturalPerson);
        Collecting element = GDPRFactory.eINSTANCE.createCollecting();
        element.setEntityName("Start");
        element.setId(String.valueOf(UUID.randomUUID()));
        element.setResponsible(this.defaultController);
        element.getPurpose().add(this.defaultPurpose);
        element.getOnTheBasisOf().add(this.defaultLegalBasis);
        element.getOutputData().add(this.defaultPersonalData);
        gdprModel.getProcessing().add(element);
        this.lastElement = element;
    }

    public void createFlowElement(String name) {
        Processing element = GDPRFactory.eINSTANCE.createProcessing();
        element.setEntityName(name);
        element.setId(String.valueOf(UUID.randomUUID()));
        element.setResponsible(this.defaultController);
        element.getInputData().add(this.defaultPersonalData);
        element.getPurpose().add(this.defaultPurpose);
        element.getOnTheBasisOf().add(this.defaultLegalBasis);
        this.lastElement.getFollowingProcessing().add(element);
        this.gdprModel.getProcessing().add(element);
        this.lastElement = element;
    }

    public Controller createController(String name) {
        Controller role = GDPRFactory.eINSTANCE.createController();
        role.setName(name);
        role.setEntityName(name);
        role.setId(String.valueOf(UUID.randomUUID()));
        this.gdprModel.getInvolvedParties().add(role);
        return role;
    }

    public Purpose createPurpose(String name) {
        Purpose purpose = GDPRFactory.eINSTANCE.createPurpose();
        purpose.setEntityName(name);
        purpose.setId(String.valueOf(UUID.randomUUID()));
        this.gdprModel.getPurposes().add(purpose);
        return purpose;
    }

    public Consent createConsentLegalBasis(String name, Purpose purpose, NaturalPerson consentee) {
        Consent legalBasis = GDPRFactory.eINSTANCE.createConsent();
        legalBasis.setEntityName(name);
        legalBasis.setId(String.valueOf(UUID.randomUUID()));
        legalBasis.getForPurpose().add(purpose);
        legalBasis.setConsentee(consentee);
        this.gdprModel.getLegalBases().add(legalBasis);
        return legalBasis;
    }

    public NaturalPerson createNaturalPerson(String name) {
        NaturalPerson naturalPerson = GDPRFactory.eINSTANCE.createNaturalPerson();
        naturalPerson.setName(name);
        naturalPerson.setEntityName(name);
        naturalPerson.setId(String.valueOf(UUID.randomUUID()));
        this.gdprModel.getInvolvedParties().add(naturalPerson);
        return naturalPerson;
    }

    public PersonalData createPersonalData(String name, NaturalPerson naturalPerson) {
        PersonalData personalData = GDPRFactory.eINSTANCE.createPersonalData();
        personalData.setEntityName(name);
        personalData.setId(String.valueOf(UUID.randomUUID()));
        personalData.getDataReferences().add(naturalPerson);
        this.gdprModel.getData().add(personalData);
        return personalData;
    }

    public Property createProperty(String name, List<String> values) {
        Property property = ContextpropertiesFactory.eINSTANCE.createProperty();
        property.setEntityName(name);
        property.setId(String.valueOf(UUID.randomUUID()));
        for(String value : values) {
            PropertyValue propertyValue = ContextpropertiesFactory.eINSTANCE.createPropertyValue();
            propertyValue.setParentProperty(property);
            propertyValue.setEntityName(value);
            propertyValue.setId(String.valueOf(UUID.randomUUID()));
        }
        this.contextDependentAttributes.getProperty().add(property);
        return property;
    }

    public PropertyAnnotation createPropertyAnnotation(AbstractGDPRElement annotatedElement, Property property) {
        PropertyAnnotation propertyAnnotation = ContextpropertiesFactory.eINSTANCE.createPropertyAnnotation();
        propertyAnnotation.setAnnotatedElement(annotatedElement);
        propertyAnnotation.setProperty(property);
        this.contextDependentAttributes.getPropertyannotation().add(propertyAnnotation);
        return propertyAnnotation;
    }

    public ContextAnnotation createContextAnnotation(String name, List<PropertyValue> propertyValues, PropertyAnnotation propertyAnnotation) {
        ContextAnnotation contextAnnotation = ContextpropertiesFactory.eINSTANCE.createContextAnnotation();
        contextAnnotation.setEntityName(name);
        contextAnnotation.setId(String.valueOf(UUID.randomUUID()));
        contextAnnotation.getPropertyvalue().addAll(propertyValues);
        propertyAnnotation.getContextannotation().add(contextAnnotation);
        return contextAnnotation;
    }

    public ContextDefinition createContextDefinition(String name, AbstractGDPRElement requiredElement, ContextAnnotation contextAnnotation) {
        ContextDefinition contextDefinition = ContextpropertiesFactory.eINSTANCE.createContextDefinition();
        contextDefinition.setEntityName(name);
        contextDefinition.setId(String.valueOf(UUID.randomUUID()));

        GDPRContextElement gdprContextElement = ContextpropertiesFactory.eINSTANCE.createGDPRContextElement();
        gdprContextElement.setGdprElement(requiredElement);
        contextDefinition.getGdprElements().add(gdprContextElement);

        contextAnnotation.getContextdefinition().add(contextDefinition);
        this.contextDependentAttributes.getContextdefinition().add(contextDefinition);
        return contextDefinition;
    }
}
