package edu.stanford.bmir.protege.examples.models;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;


public class MODProperty {
    private final String type;

    public void setIRI(org.semanticweb.owlapi.model.IRI IRI) {
        this.IRI = IRI;
    }

    private IRI IRI;
    private final HashSet<String> label = new HashSet<>();



    private String description;
    private String importDate;
    private Collection<OWLNamedIndividual> equivalentProperty;
    private String derivedFrom;
    private String issueDate;
    private String isDefinedBy;

    private final HashMap<IRI, HashSet<String>> optionalProperties = new HashMap();

    public MODProperty(OWLNamedIndividual property, OWLOntology ontology) {


        Collection<OWLAnnotation> properties = EntitySearcher.getAnnotations(property, ontology);


        extractEquivalentPropertiesAsNamedIndividuals(property, ontology);

        //extractDomainAndRange(property, ontology);

        this.IRI = property.getIRI();
        this.type = property.getEntityType().toString();


        mapAnnotationProperties(properties);

    }

    public static boolean isValid(MODProperty property) {
        return (property.getLabel() != null && property.getDescription() != null);
    }

    private static Boolean propertyFoundByIRI(String IRIToFind, OWLAnnotation annotation) {

        return annotation.getProperty().getIRI().toURI().toString().equals(IRIToFind);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MODProperty that = (MODProperty) o;
        return IRI.equals(that.IRI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IRI);
    }

    public String getType() {
        return type;
    }

    public IRI getIRI() {
        return IRI;
    }

    public HashSet<String> getLabel() {
        return label;
    }



    public String getDescription() {
        return description;
    }


    public Collection<OWLNamedIndividual> getEquivalentProperty() {
        return equivalentProperty;
    }


    /* This method is kept here because it might be used in the future for validating metadata values .
    We need the domain and the range of a property to know whether the type of the value is valid or not.

    private void extractDomainAndRange(OWLProperty property, OWLOntology ontology) {
        if (property.isOWLDataProperty()) {
            this.domain = EntitySearcher.getDomains(property.asOWLDataProperty(), ontology).toString();
            this.range = EntitySearcher.getRanges(property.asOWLDataProperty(), ontology).toString();
        } else if (property.isOWLObjectProperty()) {
            this.domain = EntitySearcher.getDomains(property.asOWLObjectProperty(), ontology).toString();
            this.range = EntitySearcher.getRanges(property.asOWLObjectProperty(), ontology).toString();
        } else {
            this.domain = EntitySearcher.getDomains(property.asOWLAnnotationProperty(), ontology).toString();
            this.range = EntitySearcher.getRanges(property.asOWLAnnotationProperty(), ontology).toString();
        }
    }*/

    private void extractEquivalentPropertiesAsNamedIndividuals(OWLNamedIndividual individual, OWLOntology ontology) {
        Collection<OWLNamedIndividual> equivalentProperties = new ArrayList<OWLNamedIndividual>();

        Collection<OWLObjectPropertyAssertionAxiom> props = ontology.getObjectPropertyAssertionAxioms(individual);
        for (OWLObjectPropertyAssertionAxiom prop : props) {
            Collection<OWLNamedIndividual> individuals = prop.getIndividualsInSignature();
            for (OWLNamedIndividual ind : individuals) {
                equivalentProperties.add(ind);
            }
        }
        Set<OWLNamedIndividual> set = new LinkedHashSet<>(equivalentProperties);
        equivalentProperties = new ArrayList<>(set);
        this.equivalentProperty = equivalentProperties != null ? equivalentProperties : new HashSet<OWLNamedIndividual>();
    }

    @Override
    public String toString() {
        return "MODProperty{" +
                "IRI='" + IRI + '\'' +
                ", type='" + type + '\'' +
                ", label=" + label +
                ", description='" + description + '\'' +
                ", importDate='" + importDate + '\'' +
                ", equivalentProperty=" + equivalentProperty +
                ", derivedFrom=" + derivedFrom +
                ", issueDate='" + issueDate + '\'' +
                ", isDefinedBy='" + isDefinedBy + '\'' +
                '}';
    }

    private void mapAnnotationProperties(Collection<OWLAnnotation> properties) {
        for (OWLAnnotation annot : properties) {

            if (propertyFoundByIRI("http://purl.org/dc/terms/description", annot)) {
                this.description = annot.getValue().toString();
            } else if (propertyFoundByIRI("http://purl.org/dc/terms/issued", annot)) {
                this.issueDate = annot.getValue().toString();
            } else if (propertyFoundByIRI("http://purl.org/pav/importedOn", annot)) {
                this.importDate = annot.getValue().toString();
            } else if (propertyFoundByIRI("http://purl.org/pav/derivedFrom", annot)) {
                this.derivedFrom = annot.getValue().toString();
            } else if (propertyFoundByIRI("http://www.w3.org/2000/01/rdf-schema#label", annot)) {
                this.label.add(annot.getValue().toString());
            } else if (propertyFoundByIRI("http://www.w3.org/2000/01/rdf-schema#isDefinedBy", annot)) {
                this.isDefinedBy = annot.getValue().toString();
            } else {
                if (!this.optionalProperties.containsKey(annot.getProperty().getIRI())) {
                    this.optionalProperties.put(annot.getProperty().getIRI(), new HashSet<>());
                }
                this.optionalProperties.get(annot.getProperty().getIRI()).add(annot.getValue().toString());
            }
        }
    }


}
