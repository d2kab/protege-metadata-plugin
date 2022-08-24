package edu.stanford.bmir.protege.examples.models;


import edu.stanford.bmir.protege.examples.models.MODProperty;
//import edu.stanford.bmir.protege.examples.view.OWLOntologyMetadataView;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;


import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class OntologyMetadataContainer {

    public Set<MODProperty> getModProperties(InputStream inputStream) throws OWLOntologyCreationException {



        Set<MODProperty> properties = new HashSet<>();
        OWLOntology o = loadMod(inputStream);





        addProperties(o.getIndividualsInSignature(), properties, o);



        return properties;
    }


    private OWLOntology loadMod(InputStream inputStream) throws OWLOntologyCreationException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        return m.loadOntologyFromOntologyDocument(inputStream);
    }

    private void addProperties(Set<? extends OWLNamedIndividual> individuals, Set<MODProperty> modProperties, OWLOntology ontology) {
        for (OWLNamedIndividual op : individuals) {
            MODProperty prop = new MODProperty(op , ontology);
            if (MODProperty.isValid(prop)){
                modProperties.add(new MODProperty(op,ontology));
            }
        }
    }

}
