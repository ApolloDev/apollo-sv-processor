package edu.pitt.apollo.apollosvprocessor.ontology;

import edu.pitt.apollo.apollosvprocessor.exception.ApolloSVProcessorException;
import java.io.File;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * Author: Nick Millett
 * Email: nick.millett@gmail.com
 * Date: Mar 11, 2013
 * Time: 12:12:05 PM
 * Class: OntologyLoader
 * IDE: NetBeans 6.9.1
 */
public class OntologyLoader {

    public static OWLOntology loadOntologyFromUrl(String url) throws ApolloSVProcessorException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI iri = IRI.create(url);
        OWLOntology ontology;

        try {
            ontology = manager.loadOntologyFromOntologyDocument(iri);
        } catch (OWLOntologyCreationException ex) {
            if (ex.getMessage().contains("Connection reset")) {
                return loadOntologyFromUrl(url);
            } else {
                ex.printStackTrace();
                throw new ApolloSVProcessorException("OwlOntologyCreationException loading OWL file from URL: " + ex.getMessage());
            }
        }

        return ontology;
    }

    public static OWLOntology loadOntologyFromFile(String filepath) throws ApolloSVProcessorException {

        File file = new File(filepath);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException ex) {
            throw new ApolloSVProcessorException("OwlOntologyCreationException loading OWL file from file: " + ex.getMessage());
        }

        return ontology;
    }
}
