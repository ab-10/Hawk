package examples;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class ConceptNetTester {
    public static void main(String args[]) throws IOException{
        BooleanQuery.Builder builderPivot = new BooleanQuery.Builder();
        builderPivot.add(new TermQuery(new Term("property", "raisin")), BooleanClause.Occur.MUST);
        builderPivot.add(new WildcardQuery(new Term("property", "black")), BooleanClause.Occur.MUST);
        String indexLocation = "src/main/resources/ConceptNet";


        BooleanQuery queryPivot = builderPivot.build();

        DirectoryReader reader;
        IndexSearcher searcher;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid WordNet Index directory specified.");
        }

        ScoreDoc[] resultsPivot;
        try {
            resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain search results for WordNet Index query.");
        }

        for(ScoreDoc result: resultsPivot){
            System.out.println(searcher.doc(result.doc));
        }

    }
}
