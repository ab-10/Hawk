package analysis;

import net.didion.jwnl.data.Exc;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import edu.stanford.nlp.simple.Sentence;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of discriminativity classifiers based on dictionary models
 */
public class DictionaryClassifiers {
    public static int wordNetVote(String pivot, String comparison, String feature, String indexLocation) {
        return wordNetVote(pivot, comparison, feature, indexLocation, false);
    }

    public static int wordNetVote(String pivot, String comparison, String feature, String indexLocation, boolean printExpl) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);

        boolean decision;
        if (printExpl) {
            decision = wnExplanativeQuery("definiendum", "property", pivot, comparison,
                    feature, indexLocation);
        } else {
            decision = discriminativeQuery("definiendum", "property", pivot, comparison,
                    feature, indexLocation);
        }

        if (decision) {
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
        return visualGenomeVote(pivot, comparison, feature, indexLocation, false);
    }

    public static int visualGenomeVote(String pivot, String comparison, String feature, String indexLocation, boolean printExpl) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);

        boolean isDiscriminative;
        if (printExpl) {
            isDiscriminative = vgExplanativeQuery("attribute", "attribute", pivot, comparison, feature, indexLocation);
        } else {
            isDiscriminative = discriminativeQuery("attribute", "attribute", pivot, comparison, feature, indexLocation);
        }

        if (isDiscriminative) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int vgRelationshipVote(String pivot, String comparison, String feature, String indexLocation) {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);
        if (discriminativeQuery("relationship", "relationship", pivot, comparison, feature, indexLocation)) {
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
    private static boolean discriminativeQuery(String documentLabel, String documentBody, String pivot, String
            comparison, String feature, String indexLocation) {
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

        return resultsPivot.length != 0 && resultsComparison.length == 0;
    }

    private static boolean wnExplanativeQuery(String documentLabel, String documentBody, String pivot, String
            comparison, String feature, String indexLocation) {
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

        List<String> supertypes = new LinkedList<>();
        try {
            for (ScoreDoc scoreDoc : resultsPivot) {
                IndexableField supertype = searcher.doc(scoreDoc.doc).getField("has_supertype");
                if (supertype != null) {
                    supertypes.add(supertype.stringValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean result = resultsPivot.length != 0 && resultsComparison.length == 0;

        if (result) {
            BooleanQuery.Builder explQueryBuilder = new BooleanQuery.Builder();
            explQueryBuilder.add(new TermQuery(new Term("definiendum", pivot)), BooleanClause.Occur.MUST);
            explQueryBuilder.add(new WildcardQuery(new Term("property", feature)), BooleanClause.Occur.MUST);


            BooleanQuery explQuery = explQueryBuilder.build();
            DirectoryReader wnDefReader;
            IndexSearcher wnDefSearcher;
            try {
                wnDefReader = DirectoryReader.open(FSDirectory.open(Paths.get("src/main/resources/WNDefinitions")));
                wnDefSearcher = new IndexSearcher(wnDefReader);
            } catch (IOException e) {
                e.printStackTrace();
                return result;
            }

            ScoreDoc[] explResults;
            try {
                explResults = wnDefSearcher.search(explQuery, 10).scoreDocs;
            } catch (IOException e) {
                e.printStackTrace();
                return result;
            }
            if (explResults.length > 0) {
                try {
                    System.out.print("The triple: " + pivot + "," + comparison + "," + feature);
                    System.out.println(" is discriminative, because of the following definition of " + pivot + ":");
                    System.out.println(wnDefSearcher.doc(explResults[0].doc).getField("definition").stringValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                for (String supertype : supertypes) {
                    try {
                        explQueryBuilder = new BooleanQuery.Builder();
                        explQueryBuilder.add(new TermQuery(new Term("definiendum", pivot)), BooleanClause.Occur.MUST);
                        explQueryBuilder.add(new TermQuery(new Term("property", supertype)), BooleanClause.Occur.MUST);
                        explQuery = explQueryBuilder.build();

                        ScoreDoc[] baseResults = wnDefSearcher.search(explQuery, 10).scoreDocs;
                        if (baseResults.length == 0) {
                            continue;
                        }
                        explQueryBuilder = new BooleanQuery.Builder();
                        explQueryBuilder.add(new TermQuery(new Term("definiendum", supertype)), BooleanClause.Occur.MUST);
                        explQueryBuilder.add(new TermQuery(new Term("property", feature)), BooleanClause.Occur.MUST);
                        explQuery = explQueryBuilder.build();

                        ScoreDoc[] supertypeResults = wnDefSearcher.search(explQuery, 10).scoreDocs;
                        if (supertypeResults.length == 0) {
                            continue;
                        }

                        System.out.print("The triple: " + pivot + "," + comparison + "," + feature);
                        System.out.println(" is discriminative, because of the following definition of " + pivot + ":");
                        System.out.println(wnDefSearcher.doc(baseResults[0].doc).getField("definition").stringValue());
                        System.out.println("And the following definition for " + supertype);
                        System.out.println(wnDefSearcher.doc(supertypeResults[0].doc).getField("definition").stringValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return result;
                    }
                    break;
                }
            }
        }

        return result;
    }

    private static boolean vgExplanativeQuery(String documentLabel, String documentBody, String pivot,
            String comparison, String feature, String indexLocation) {

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
            throw new IllegalArgumentException("Invalid Visual Genome Index directory specified.");
        }

        ScoreDoc[] resultsComparison, resultsPivot;
        try {
            resultsComparison = searcher.search(queryComparison, 10).scoreDocs;
            resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain search results for Visual Genome Index query.");
        }

        boolean isDiscriminative =  resultsPivot.length != 0 && resultsComparison.length == 0;

        if(isDiscriminative){
            for(ScoreDoc doc : resultsPivot){
                int image_id, y, x, h,w;
                try {
                    image_id = searcher.doc(doc.doc).getField("image_id").numericValue().intValue();
                    y = searcher.doc(doc.doc).getField("y").numericValue().intValue();
                    x = searcher.doc(doc.doc).getField("x").numericValue().intValue();
                    h = searcher.doc(doc.doc).getField("h").numericValue().intValue();
                    w = searcher.doc(doc.doc).getField("w").numericValue().intValue();
                }catch (IOException e){
                    e.printStackTrace();
                    return isDiscriminative;
                }
                if(image_id == -1 || y == -1 || x == -1 || h == -1 || w == -1){
                    continue;
                }

                // TODO: retrieve explanatory image
                // TODO: draw a bounding box
                System.out.printf("As can be seen in image %d %s can be %s\n", image_id, pivot, feature);
                break;
            }

        }
        return isDiscriminative;
    }


}
