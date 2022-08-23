package edu.stanford.bmir.protege.examples;

import edu.stanford.bmir.protege.examples.models.MODProperty;
import edu.stanford.bmir.protege.examples.models.OntologyMetadataContainer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semarglproject.vocab.OWL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public Main() throws OWLOntologyCreationException, IOException {
    }

    public static OWLOntology loadMod(InputStream inputStream) throws OWLOntologyCreationException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        return m.loadOntologyFromOntologyDocument(inputStream);
    }



    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {


        // 0 - Clean le projet (fait)
        // 1 - Modéslisation (fait)
        // 2 - getModProperties(fichierSource) (fait)
        // 3 - Afficher les MOD properties
        OntologyMetadataContainer root = new OntologyMetadataContainer();

        URL url = Main.class.getClassLoader().getResource("mod-v2.0_profile.ttl");

        Set<MODProperty> properties = new HashSet<MODProperty>();


        try {
            properties = root.getModProperties(url.openConnection().getInputStream());
        } catch (OWLOntologyCreationException | IOException e) {
            e.printStackTrace();
        }


        OWLOntology o = loadMod(url.openConnection().getInputStream());

        for (MODProperty prop : properties){
            for (OWLNamedIndividual indiv : prop.getEquivalentProperty()){
                System.out.println(indiv.toString());
            }
        }

        /*
       OWLOntologyManager man = o.getOWLOntologyManager();
        addDataPropertyCopies(o,man);
        Iterator<OWLNamedIndividual> it = o.getIndividualsInSignature().iterator();
        Collection<OWLNamedIndividual> inds = o.getIndividualsInSignature();
        System.out.println(inds);
        while (it.hasNext()) {
            OWLNamedIndividual next = it.next();
            //System.out.println(next.toString());
            Collection<OWLObjectPropertyAssertionAxiom> props = o.getObjectPropertyAssertionAxioms(next);
            // Collection<OWLAnnotationProperty> props = ;
            for (OWLObjectPropertyAssertionAxiom prop : props){
                System.out.println(prop);
                System.out.println(prop.getIndividualsInSignature());
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }
            System.out.println(props);

        }
        */

    }



    public static void  addDataPropertyCopies(OWLOntology ontology, OWLOntologyManager manager) throws OWLOntologyStorageException {
        // We need a data factory to create various object from.
        OWLDataFactory factory = manager.getOWLDataFactory();
        // Loop through all roles and concepts in the ontology
        for (OWLAnnotation r : ontology.getAnnotations()) {

            // Create new existential Restriction
            OWLDataProperty parent = factory.getOWLTopDataProperty();
            OWLDataProperty newProperty = factory.getOWLDataProperty(r.getProperty().getIRI());
            OWLAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(newProperty, parent);
            // We now add the axiom to the ontology
            AddAxiom addAxiom = new AddAxiom(ontology, axiom);
            // We now use the manager to apply the change
            manager.applyChange(addAxiom);
        }
        File file = new File("/home/walid/Desktop/protege-plugin-examples/src/main/resources", "mod2.0.ttl");
        manager.saveOntology(ontology, IRI.create(file.toURI()));
    }



    private static HashMap<MODProperty, List<String>> mapModToActiveOntology(Set<MODProperty> properties, OWLOntology ontology) throws IOException, OWLOntologyCreationException {
        URL url = Main.class.getClassLoader().getResource("mod-v2.0_profile.ttl");

        OWLOntology o = loadMod(url.openConnection().getInputStream());

        HashMap<MODProperty, List<String>> out = new HashMap<>();
        Collection<OWLAnnotation> annotations = ontology.getAnnotations();
        for (MODProperty prop : properties) {
            Collection<OWLNamedIndividual> equivalentProperties = prop.getEquivalentProperty();
            MODProperty usedProperty = null;
            String value = null;
            if (ontology != null) {
             /*  if (!out.containsKey(prop)) {
                    out.put(prop, new ArrayList<String>());
                }*/
                for (OWLAnnotation annotation : annotations) {
                    for (OWLNamedIndividual indiv : equivalentProperties) {
                        if (annotation.getProperty().getIRI().toURI().toString().equals(indiv.getIRI().toString())) {
                            OWLLiteral literal = annotation.getValue().asLiteral().orNull();
                            IRI iri = annotation.getValue().asIRI().orNull();
                            value = "";
                            if (literal != null) {
                                value = literal.getLiteral();
                            } else if (iri != null) {
                                value = iri.toString();
                            }
                            usedProperty = new MODProperty(indiv, o);
                            if (!out.containsKey(usedProperty)) {
                                out.put(usedProperty, new ArrayList<String>());
                            }
                            out.get(usedProperty).add(value);
                        }
                    }
                }
            }
        }
        return out;
    }


       /* for (MODProperty prop : properties){
            System.out.println(prop.getEquivalentProperty().toString());
            i++;
        }
        System.out.println(i);*/

      /*  OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        URL url2 = Main.class.getClassLoader().getResource("ephy-v1.1.owl");
        try {
            OWLOntology ont = man.loadOntologyFromOntologyDocument(url2.openConnection().getInputStream());
            HashMap<MODProperty, List<String>> modProperties = mapModToActiveOntology(properties, ont);
            for (Map.Entry<MODProperty, List<String>> prop : modProperties.entrySet()) {
                System.out.println(prop.getKey().getLabel() + " = " + prop.getValue().stream().map(x -> x.toString()).collect(Collectors.joining()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

}