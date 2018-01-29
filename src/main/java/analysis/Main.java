package analysis;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.*;
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
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static indexation.GraphIndexer.indexGraph;

public class Main {
    private static Directory graphDirectory;
    public static void main(String[] args) throws Exception{
        /*
        graphDirectory = FSDirectory.open(Paths.get("src", "main", "resources", "index"));
        Graph graph = new Graph("WN_DSR_model_XML.rdf");
        indexGraph(graph, graphDirectory);
        */
        Scanner fileScanner = new Scanner(new File("src/main/resources/validation.txt"));
        FileWriter[] writers = new FileWriter[8];
        writers[0] = new FileWriter("src/main/resources/LSA10Results.txt");
        writers[1] = new FileWriter("src/main/resources/LSA20Results.txt");

        writers[2] = new FileWriter("src/main/resources/ESA10Results.txt");
        writers[3] = new FileWriter("src/main/resources/ESA20Results.txt");

        writers[4] = new FileWriter("src/main/resources/W2V10Results.txt");
        writers[5] = new FileWriter("src/main/resources/W2V20Results.txt");

        writers[6] = new FileWriter("src/main/resources/GloVe10Results.txt");
        writers[7] = new FileWriter("src/main/resources/GloVe20Results.txt");

        String[] models = new String[]{"LSA", "LSA", "ESA", "ESA", "W2V", "W2V", "GloVe", "GloVe"};

        int[] coef = new int[]{10, 20, 10, 20, 10, 20, 10, 20, 10, 20, 10, 20, 10, 20, 10, 20};

        while(fileScanner.hasNext()){
            String[] currentLine = fileScanner.nextLine().split(",");
            for(int i = 4; i < 8; i++) {
                writers[i].write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2]
                        + "," + compare(currentLine[0], currentLine[1], currentLine[2], models[i], coef[i])
                        + System.getProperty("line.separator"));
                Thread.sleep(500);
            }

        }
        for(int i = 4; i<8; i++){
            writers[i].close();
        }

    }

    public static int compare(String pivot, String comparison, String feature, String model, int k) throws Exception{
        pivot = pivot.toLowerCase();
        comparison = comparison.toLowerCase();
        feature = feature.toLowerCase();

        if(isFoundInKNN(pivot, feature, model, k) & !isFoundInKNN(comparison, feature, model, k)){
            return 1;
        }
        return 0;

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

    /**
     * Checks whether <code>test</code> can be found within k nearest neighbours of <code>center</code>.
     *
     * @param center term for which k nearest neighbours are found
     * @param test term for which to search for amongst the k nearest neighbours
     * @throws IOException
     * @throws JSONException
     */
    private static boolean isFoundInKNN(String center, String test, String model, int k) throws IOException, JSONException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/neighbors/relatedness");
        httppost.setHeader("Content-Type", "application/json");

// Request parameters and other properties.
        String params = "{\n" +
                "        \"corpus\": \"wiki-2014\",\n" +
                "        \"model\": \""+model+"\",\n" +
                "        \"language\": \"EN\",\n" +
                "        \"topk\": "+ k + ",\n" +
                "\"scoreFunction\": \"COSINE\",\n" +
                "        \"terms\" : [\"" + center + "\"]\n" +
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
                Iterator key = new JSONObject(theString).getJSONObject("terms").getJSONObject(center).keys();
                while(key.hasNext()){
                    if(key.next().toString().equals(test)){
                        return true;
                    }
                }
            }catch (Exception e){
                System.out.println(center+ " " + test);
            } finally {
                instream.close();
            }
        }
        return false;
    }
}
