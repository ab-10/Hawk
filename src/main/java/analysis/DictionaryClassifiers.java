package analysis;

import net.didion.jwnl.data.Exc;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import edu.stanford.nlp.simple.Sentence;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * A collection of discriminativity classifiers based on dictionary models
 */
public class DictionaryClassifiers {
    public static int wordNetVote(String pivot, String comparison, String feature, String indexLocation) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);
        if (discriminativeQuery("rawGloss", "rawGloss", pivot, comparison, feature, indexLocation)) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int wikipediaVote(String pivot, String comparison, String feature, String indexLocation) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);
        if (discriminativeQuery("property", "property", pivot, comparison, feature, indexLocation)) {
            return 1;
        } else {
            return 0;
        }
    }


    public static int visualGenomeVote(String pivot, String comparison, String feature, String indexLocation) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);
        if (discriminativeQuery("property", "property", pivot, comparison, feature, indexLocation)) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int vgRelationshipVote(String pivot, String comparison, String feature, String indexLocation) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);
        if (discriminativeQuery("relationship", "property", pivot, comparison, feature, indexLocation)) {
            return 1;
        } else {
            return 0;
        }
    }


    /**
     * Performs a discriminativity query and determines whether given triple is discriminative
     * <p>
     * A discriminativity query is a query for a triple - pivot, comparison, feature
     * that returns TRUE if:
     * number of documents labeled as <code>pivot</code> with <code>feature</code> in their body is > 0
     * AND number of documents labeled as <code>comparison</code> with <code>feature</code> in their body is = 0
     * FALSE otherwise
     *
     * @param documentLabel name for label field of the document (i.e. definiendum for WordNet)
     * @param documentBody  name for body field of the document (i.e. property for WordNet)
     * @param pivot         first element in an attribute triple
     * @param comparison    second element in an attribute triple
     * @param feature       third element in an attribute triple
     * @param indexLocation location of Lucene index to be queried
     * @return true if the triple is discriminative, false otherwise
     */
    private static boolean discriminativeQuery(String documentLabel, String documentBody, String pivot, String comparison, String feature, String indexLocation) {
        BooleanQuery.Builder builderPivot = new BooleanQuery.Builder();
        BooleanQuery.Builder builderComparison = new BooleanQuery.Builder();
        builderPivot.add(new TermQuery(new Term(documentLabel, pivot)), BooleanClause.Occur.MUST);
        builderPivot.add(new WildcardQuery(new Term(documentBody, feature)), BooleanClause.Occur.MUST);

        builderComparison.add(new TermQuery(new Term(documentLabel, comparison)), BooleanClause.Occur.MUST);
        builderComparison.add(new WildcardQuery(new Term(documentBody, feature)), BooleanClause.Occur.MUST);

        BooleanQuery queryPivot = builderPivot.build();
        BooleanQuery queryComparison = builderComparison.build();

        DirectoryReader reader;
        IndexSearcher searcher;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid WordNet Index directory specified.");
        }

        ScoreDoc[] resultsComparison, resultsPivot;
        try {
            resultsComparison = searcher.search(queryComparison, 10).scoreDocs;
            resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain search results for WordNet Index query.");
        }

        Boolean result = resultsPivot.length != 0 && resultsComparison.length == 0;

        return result;
    }

}
