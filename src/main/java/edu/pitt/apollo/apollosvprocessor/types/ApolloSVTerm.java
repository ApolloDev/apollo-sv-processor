package edu.pitt.apollo.apollosvprocessor.types;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author: Nick Millett
 * Email: nick.millett@gmail.com
 * Date: Mar 12, 2013
 * Time: 11:50:58 AM
 * Class: ApolloSVTerm
 * IDE: NetBeans 6.9.1
 */
public class ApolloSVTerm {

    private static final String MISSING_DEFINITION_TEXT = "Undefined";
    private static final String MISSING_ELUCIDATION_TEXT = "Undefined";
    private static final String MISSING_HIERARCHY_TEXT = "Undefined";
    private String uri;
    private String name;
    private String definition;
    private String elucidation;
    private List<String> classHierarchyList;

    /**
     * @return the uri
     */
    public ApolloSVTerm() {
        definition = MISSING_DEFINITION_TEXT;
        elucidation = MISSING_ELUCIDATION_TEXT;
        classHierarchyList = null;
    }

    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri.replaceAll("\n", " ").replaceAll("@en", "");
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name.replaceAll("\n", " ").replaceAll("@en", "");
    }

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition.replaceAll("\n", " ").replaceAll("@en", "").replaceAll("“", "\"").replaceAll("”", "\"").replaceAll("‘", "'").replaceAll("’", "'");
        this.definition = this.definition.substring(1, this.definition.length() - 1);
    }

    /**
     * @return the elucidation
     */
    public String getElucidation() {
        return elucidation;
    }

    /**
     * @param elucidation the elucidation to set
     */
    public void setElucidation(String elucidation) {
        this.elucidation = elucidation.replaceAll("\n", " ").replaceAll("@en", "").replaceAll("“", "\"").replaceAll("”", "\"").replaceAll("‘", "'").replaceAll("’", "'");
        this.elucidation = this.elucidation.substring(1, this.elucidation.length() - 1);
    }

    /**
     * @return the classHierarchyList
     */
    public List<String> getClassHierarchyList() {
        return classHierarchyList;
    }

    /**
     * @param classHierarchyList the classHierarchyList to set
     */
    public void setClassHierarchyList(List<String> classHierarchyList) {

        if (classHierarchyList == null || classHierarchyList.isEmpty()) {
            this.classHierarchyList = new ArrayList<String>();
            this.classHierarchyList.add(MISSING_HIERARCHY_TEXT);
        } else {
            this.classHierarchyList = classHierarchyList;
        }
    }
}
