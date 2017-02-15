package edu.pitt.apollo.apollosvprocessor;

import edu.pitt.apollo.apollosvprocessor.exception.ApolloSVProcessorException;
import edu.pitt.apollo.apollosvprocessor.ontology.OntologyLoader;
import edu.pitt.apollo.apollosvprocessor.types.ApolloSVTerm;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 *
 * Author: Nick Millett Email: nick.millett@gmail.com Date: Mar 11, 2013 Time: 12:30:29 PM Class: ApolloSVProcessor IDE: NetBeans 6.9.1
 */
public class ApolloSVProcessor {

//    public static final String APOLLO_ONTOLOGY_RELEASE_URL = "https://apollo.googlecode.com/svn/apollo-sv/tags/release-2.0.1/apollo-sv.owl";
	public static final String APOLLO_ONTOLOGY_RELEASE_URL = "https://raw.githubusercontent.com/ApolloDev/apollo-sv/master/src/ontology/apollo-sv.owl";
	public static final String APOLLO_GEO_OWL_RELEASE_URL = "https://raw.githubusercontent.com/ufbmi/geographical-entity-ontology/development/geo.owl";
	private static final String APOLLO_SV_TERMS_OUTPUT_FILE_HEADER = "URI\tName\tDefinition\tElucidation\tClassHierarchy";
	private static final String CLASS_NAME_IDENTIFIER = "http://www.w3.org/2000/01/rdf-schema#label";
	private static final String ELUCIDATION_IDENTIFIER = "http://purl.obolibrary.org/obo/IAO_0000600";
	private static final String DEFINITION_IDENTIFIER = "http://purl.obolibrary.org/obo/IAO_0000115";
	private static final String TERM_NAME_IDENTIFIER = "http://purl.obolibrary.org/obo/apollo_sv.owl/APOLLO_SV_0000040";
	private static final String XSD_STRING_FILTER = "^^xsd:string";
	private static final String NEW_LINE_FILTER = "\n";
	private static final String LANGUAGE_FILTER = "@en";
	private static final String QUOTE_FILTER = "\"";
	private static final String TERM_CLASS_SEPARATOR = ";";

	public static void loadOntologyAndCreateApolloSVTermsFile(String apolloSVOntologyURL, String apolloSVTermsOutputFilePath) throws ApolloSVProcessorException, IOException {
		// First get the hierarchies from the geo.owl file
		OWLOntology geoOntology = OntologyLoader.loadOntologyFromUrl(APOLLO_GEO_OWL_RELEASE_URL);
		OWLReasoner geoOntologyReasoner = createOWLReasonerFromOntology(geoOntology);
		Map<String, ApolloSVTerm> geoTerms = getMapOfTermsFromApolloSV(geoOntologyReasoner, geoOntology);

		OWLOntology apolloOntology = OntologyLoader.loadOntologyFromUrl(apolloSVOntologyURL);
		OWLReasoner apolloOntologyReasoner = createOWLReasonerFromOntology(apolloOntology);
		Map<String, ApolloSVTerm> apolloSVTerms = getMapOfTermsFromApolloSV(apolloOntologyReasoner, apolloOntology);

		incorporateHierarchiesFromImportedOntologies(apolloSVTerms, geoTerms);

//        Set<OWLOntology> ontologies = apolloOntology.getDirectImports();
//        for (OWLOntology o : ontologies) {
//            apolloOntologyReasoner = createOWLReasonerFromOntology(o);
//            getMapOfTermsFromApolloSV(apolloSVTermsOutputFilePath, apolloOntologyReasoner, o);
//        }
		PrintStream apolloSVTermsPrintStream = createApolloSVTermsPrintStream(apolloSVTermsOutputFilePath);
		printListOfApolloSVTermsToFile(apolloSVTerms, apolloSVTermsPrintStream);
	}

	private static OWLReasoner createOWLReasonerFromOntology(OWLOntology ontology) {
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
		reasoner.precomputeInferences();

		boolean ontologyIsConsistent = reasoner.isConsistent();
		System.out.printf("Consistent: %s\n", ontologyIsConsistent);

		return reasoner;
	}

	private static PrintStream createApolloSVTermsPrintStream(String apolloSVTermsOutputFilePath) throws FileNotFoundException {
		PrintStream ps = new PrintStream(new File(apolloSVTermsOutputFilePath));
		ps.println(APOLLO_SV_TERMS_OUTPUT_FILE_HEADER);
		return ps;
	}

	private static Map<String, ApolloSVTerm> getMapOfTermsFromApolloSV(OWLReasoner apolloOntologyReasoner, OWLOntology apolloSVOntology) throws FileNotFoundException {
//        PrintStream apolloSVTermsPrintStream = null;

		List<String> classHierarchy = new ArrayList<String>();
		Node<OWLClass> topNode = apolloOntologyReasoner.getTopClassNode();
		Map<String, ApolloSVTerm> apolloSVTerms = new HashMap<String, ApolloSVTerm>();

		getListOfTerms(apolloSVTerms, topNode, apolloOntologyReasoner, apolloSVOntology, classHierarchy);

		return apolloSVTerms;
	}

	private static void getListOfTerms(Map<String, ApolloSVTerm> apolloSVTerms, Node<OWLClass> parentNode, OWLReasoner reasoner,
			OWLOntology ontology, List<String> classHierarchyList) {

		if (reachedBottomNode(parentNode)) {
			return;
		}

		apolloSVTerms.putAll(getListOfApolloSVTermsFromNode(parentNode, classHierarchyList, ontology));
//        printListOfApolloSVTermsToFile(apolloSVTerms);

		NodeSet<OWLClass> childNodes = reasoner.getSubClasses(parentNode.getRepresentativeElement(), true);
		for (Node<OWLClass> child : childNodes) {
			List<String> newClassHierarchy = new ArrayList<String>(classHierarchyList);
			getListOfTerms(apolloSVTerms, child, reasoner, ontology, newClassHierarchy);
		}
	}

	private static boolean reachedBottomNode(Node<OWLClass> node) {
		return node.isBottomNode();
	}

	private static Map<String, ApolloSVTerm> getListOfApolloSVTermsFromNode(Node<OWLClass> node, List<String> apolloSVClassHierarchyList,
			OWLOntology apolloOntology) {

		DefaultPrefixManager prefixManagerForProcessingURI = new DefaultPrefixManager("");
		Map<String, ApolloSVTerm> apolloSVTerms = new HashMap<String, ApolloSVTerm>();

		for (OWLClass apolloSVClass : node.getEntities()) {

			ApolloSVTerm term = new ApolloSVTerm();

			String uri = prefixManagerForProcessingURI.getShortForm(apolloSVClass);
			term.setUri(uri);

			Set<OWLAnnotation> owlAnnotationSetForApolloSVClass = apolloSVClass.getAnnotations(apolloOntology);

			if (owlAnnotationSetForApolloSVClass.size() > 0) {
				for (OWLAnnotation owlAnnotation : owlAnnotationSetForApolloSVClass) {

					String propertyIRIStringForOwlAnnotation = getPropertyIRIStringFromOWLAnnotation(owlAnnotation);
					String valueStringForOwlAnnotation = getValueStringForOwlAnnotation(owlAnnotation);
					valueStringForOwlAnnotation = filterUnneccesaryTextFromOwlAnnotationValueString(valueStringForOwlAnnotation);
					if (propertyIRIStringForOwlAnnotation.equals(CLASS_NAME_IDENTIFIER)) {
						if (valueStringForOwlAnnotation.contains("pathogen")) {
							System.out.println();
						}
						valueStringForOwlAnnotation = filterUnneccesaryTextFromOwlAnnotationValueStringForClassHierarchy(valueStringForOwlAnnotation);
						apolloSVClassHierarchyList.add(valueStringForOwlAnnotation);
					} else if (propertyIRIStringForOwlAnnotation.equals(ELUCIDATION_IDENTIFIER)) {
						term.setElucidation(valueStringForOwlAnnotation);
					} else if (propertyIRIStringForOwlAnnotation.equals(DEFINITION_IDENTIFIER)) {
						term.setDefinition(valueStringForOwlAnnotation);
					} else if (propertyIRIStringForOwlAnnotation.equals(TERM_NAME_IDENTIFIER)) {
						term.setName(valueStringForOwlAnnotation);
					}
				}
			} else {
//                apolloSVClassHierarchyList.add(apolloSVClass.getIRI().toString());
//                foundApolloSVTerm = true;
			}
			term.setClassHierarchyList(apolloSVClassHierarchyList);
			apolloSVTerms.put(uri, term);

		}
		return apolloSVTerms;
	}

	private static void incorporateHierarchiesFromImportedOntologies(Map<String, ApolloSVTerm> apolloSVTerms, Map<String, ApolloSVTerm> importedOntologyTerms) {

		for (String apolloTermURI : apolloSVTerms.keySet()) {
			if (importedOntologyTerms.containsKey(apolloTermURI)) {
				ApolloSVTerm apolloTerm = apolloSVTerms.get(apolloTermURI);
				if (apolloTerm.getName() != null) {
					ApolloSVTerm importedTerm = importedOntologyTerms.get(apolloTermURI);
					if (apolloTerm.getClassHierarchyList() == null || apolloTerm.getClassHierarchyList().isEmpty()
							|| apolloTerm.getClassHierarchyList().get(0).equals("Undefined")) {
						apolloTerm.setClassHierarchyList(importedTerm.getClassHierarchyList());
					}
				}
			}
		}

	}

	private static String filterUnneccesaryTextFromOwlAnnotationValueString(String owlAnnotationValueString) {
		String filteredOwlAnnotationValueString = owlAnnotationValueString;
		if (filteredOwlAnnotationValueString.contains(XSD_STRING_FILTER)) {
			filteredOwlAnnotationValueString = filteredOwlAnnotationValueString.replace(XSD_STRING_FILTER, "");
		}
		if (filteredOwlAnnotationValueString.contains(NEW_LINE_FILTER)) {
			filteredOwlAnnotationValueString = filteredOwlAnnotationValueString.replaceAll(NEW_LINE_FILTER, "");
		}
		if (filteredOwlAnnotationValueString.contains(LANGUAGE_FILTER)) {
			filteredOwlAnnotationValueString = filteredOwlAnnotationValueString.replace(LANGUAGE_FILTER, "");
		}
		return filteredOwlAnnotationValueString;
	}

	private static String filterUnneccesaryTextFromOwlAnnotationValueStringForClassHierarchy(String owlAnnotationValueString) {
		String filteredOwlAnnotationValueString = owlAnnotationValueString;
		if (filteredOwlAnnotationValueString.contains(QUOTE_FILTER)) {
			filteredOwlAnnotationValueString = filteredOwlAnnotationValueString.replace(QUOTE_FILTER, "");
		}

		return filteredOwlAnnotationValueString;
	}

	private static String getPropertyIRIStringFromOWLAnnotation(OWLAnnotation owlAnnotation) {
		return owlAnnotation.getProperty().getIRI().toString();
	}

	private static String getValueStringForOwlAnnotation(OWLAnnotation owlAnnotation) {
		return owlAnnotation.getValue().toString();
	}

	private static void appendStringToStringBuilderWithSeparator(StringBuilder stringBuilder, String string, String separator) {
		if (stringBuilder.length() > 0) {
			stringBuilder.append(separator).append(string);
		} else {
			stringBuilder.append(string);
		}
	}

	private static String getClassHierarchyStringForApolloSVTerm(ApolloSVTerm apolloSVTerm) {

		List<String> classHierarchyList = apolloSVTerm.getClassHierarchyList();
		StringBuilder stringBuilderForClassHierarchy = new StringBuilder("");
		if (classHierarchyList != null && !classHierarchyList.isEmpty()) {
			for (String ontologyClass : classHierarchyList) {
				appendStringToStringBuilderWithSeparator(stringBuilderForClassHierarchy, ontologyClass, TERM_CLASS_SEPARATOR);
			}
		}

		return stringBuilderForClassHierarchy.toString();
	}

	private static void printListOfApolloSVTermsToFile(Map<String, ApolloSVTerm> apolloSVTerms, PrintStream apolloSVTermsPrintStream) {

		for (ApolloSVTerm apolloSVTerm : apolloSVTerms.values()) {
			if (apolloSVTerm.getName() == null) {
				continue;
			}
			String classHierarchyString = getClassHierarchyStringForApolloSVTerm(apolloSVTerm);
			apolloSVTermsPrintStream.println(apolloSVTerm.getUri()
					+ "\t" + apolloSVTerm.getName()
					+ "\t" + apolloSVTerm.getDefinition()
					+ "\t" + apolloSVTerm.getElucidation()
					+ "\t" + classHierarchyString);
		}
	}

	public static void main(String[] args) {
		try {
			ApolloSVProcessor.loadOntologyAndCreateApolloSVTermsFile(APOLLO_ONTOLOGY_RELEASE_URL, "apollo_sv_terms_new.tab");
		} catch (ApolloSVProcessorException ex) {
			System.err.println("ApolloSVProcessorException: " + ex.getMessage());
		} catch (IOException ex) {
			System.err.println("IOException: " + ex.getMessage());
		}
	}
}
