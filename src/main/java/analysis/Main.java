package analysis;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import prep.Graph;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static indexation.GraphIndexer.indexGraph;

public class Main {
    private static Directory graphDirectory;
    public static void main(String[] args) throws Exception{
        compare("foo", "bar", "tar");
        /*
        graphDirectory = FSDirectory.open(Paths.get("src", "main", "resources", "index"));
        Graph graph = new Graph("WN_DSR_model_XML.rdf");
        indexGraph(graph, graphDirectory);
        Scanner fileScanner = new Scanner(new File("src/main/resources/validation.txt"));
        FileWriter resultWriter = new FileWriter("src/main/resources/standardAnalyzerResults.txt");
        while(fileScanner.hasNext()){
            String[] currentLine = fileScanner.nextLine().split(",");
            resultWriter.write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2]
                    + "," + compare(currentLine[0], currentLine[1], currentLine[2])
                    + System.getProperty("line.separator"));

        }
        resultWriter.close();
        */

    }

    public static int compare(String pivot, String comparison, String feature) throws Exception{
        pivot = pivot.toLowerCase();
        comparison = comparison.toLowerCase();
        feature = feature.toLowerCase();

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/neighbors/relatedness");
        httppost.setHeader("Content-Type", "application/json");

// Request parameters and other properties.
        String params = "{\n" +
                "        \"corpus\": \"wiki-2014\",\n" +
                "        \"model\": \"W2V\",\n" +
                "        \"language\": \"EN\",\n" +
                "        \"topk\": 10,\n" +
                "\"scoreFunction\": \"COSINE\",\n" +
                "        \"terms\" : [\"mother\"]\n" +
                "}";
        httppost.setEntity(new ByteArrayEntity(params.getBytes("UTF-8")));

//Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        StatusLine status = response.getStatusLine();
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(instream, writer, "UTF-8");
                String theString = writer.toString();
                System.out.println(theString);
                System.out.println(status);
            } finally {
                instream.close();
            }
        }
        return 1;

        /*
        BooleanQuery.Builder builderPivot = new BooleanQuery.Builder();
        BooleanQuery.Builder builderComparison = new BooleanQuery.Builder();
        builderPivot.add(new TermQuery(new Term("definiendum", pivot)), BooleanClause.Occur.MUST);
        builderPivot.add(new WildcardQuery(new Term("property", "*" + feature + "*")), BooleanClause.Occur.MUST);

        builderComparison.add(new TermQuery(new Term("definiendum", comparison)), BooleanClause.Occur.MUST);
        builderComparison.add(new WildcardQuery(new Term("property", feature + "*")), BooleanClause.Occur.MUST);

        BooleanQuery queryPivot = builderPivot.build();
        BooleanQuery queryComparison = builderComparison.build();

        DirectoryReader reader = DirectoryReader.open(graphDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

        ScoreDoc[] results = searcher.search(queryPivot, 10).scoreDocs;

        ScoreDoc[] resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
        ScoreDoc[] resultsComparison = searcher.search(queryComparison, 10).scoreDocs;

        if(resultsPivot.length != 0 && resultsComparison.length == 0){
            return 1;
        }else{
            return 0;
        }
        */
    }
}
