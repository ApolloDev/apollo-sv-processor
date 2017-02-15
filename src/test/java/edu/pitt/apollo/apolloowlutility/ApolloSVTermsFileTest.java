package edu.pitt.apollo.apolloowlutility;

import edu.pitt.apollo.apollosvprocessor.exception.ApolloSVProcessorException;
import edu.pitt.apollo.apollosvprocessor.ApolloSVProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import junit.framework.TestCase;

/**
 *
 * Author: Nick Millett
 * Email: nick.millett@gmail.com
 * Date: May 16, 2014
 * Time: 2:30:57 PM
 * Class: ApolloSVTermsFileTest
 * IDE: NetBeans 6.9.1
 */
public class ApolloSVTermsFileTest extends TestCase {

    private static final String TEST_DIR = "./src/test/resources/";
    private static final String OUTPUT_DIR = TEST_DIR + "output/";
    private static final String TEST_FILES_DIR = TEST_DIR + "test-files/";
    private static final String TEST_FILE_NAME = "apollo_sv_current.tab";
    private static final String OUTPUT_FILE_NAME = "apollo_sv_test.tab";

    public void testLoadingOntologyAndCreatingApolloSVTermsFile() throws ApolloSVProcessorException, IOException {
        ApolloSVProcessor.loadOntologyAndCreateApolloSVTermsFile(ApolloSVProcessor.APOLLO_ONTOLOGY_RELEASE_URL, OUTPUT_DIR + OUTPUT_FILE_NAME);

        File apolloSVTermsOutputFile = new File(OUTPUT_DIR + OUTPUT_FILE_NAME);
        if (apolloSVTermsOutputFile.exists()) {
            assert true;
        } else {
            assert false;
        }
    }
    
    public void testApolloSVTermsFileIsValid() throws FileNotFoundException {
        
        Scanner newTermsFileScanner = new Scanner(new File(OUTPUT_DIR + OUTPUT_FILE_NAME));
        newTermsFileScanner.useDelimiter("\\Z");
        String outputFileContent = newTermsFileScanner.next();
        newTermsFileScanner.close();
        
        Scanner validTermsFileScanner = new Scanner(new File(TEST_FILES_DIR + TEST_FILE_NAME));
        validTermsFileScanner.useDelimiter("\\Z");
        String testFileContent = validTermsFileScanner.next();
        validTermsFileScanner.close();
        
        assert (outputFileContent.equals(testFileContent));
    }
}
