package analysis;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.*;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.json.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.lucene.store.Directory;
import prep.Definition;
import prep.Graph;

import java.io.*;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static indexation.GraphIndexer.indexGraph;

public class Main {

    private static Directory graphDirectory;
    private static String logString = "";
    private static String lineSeparator = System.getProperty("line.separator");
    private static Dictionary dictionary;

    private static Hashtable<String, Boolean> resultTab = new Hashtable<>();
    private static Hashtable<String, Double> simTab = new Hashtable<>();


    public static void main(String[] args) throws Exception {
        JWNL.initialize(new FileInputStream("src/main/resources/properties.xml"));
        dictionary = Dictionary.getInstance();

        String versionName = "W2VGnewsIterationNoAbs";
        graphDirectory = FSDirectory.open(Paths.get("src", "main", "resources", "index"));
        Graph graph = new Graph("WN_DSR_model_XML.rdf");
        indexGraph(graph, graphDirectory);


        FileWriter logWriter = new FileWriter("src/main/resources/" + versionName + ".log");
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        logWriter.write("Log for " + versionName + " of discriminative attribute detection model"
                + lineSeparator);
        logWriter.write("logged on " + sdf.format(new Date()) + lineSeparator);
        logWriter.write("-------------------------------------------------------------------------------"
                + lineSeparator);

        Double maxScore = -1.0;
        Double bestTresh = -1.0;

        for (Double tresh = 0.0; tresh.compareTo(1.0) < 0; tresh += 0.01) {
            FileWriter resultWriter = new FileWriter("src/main/resources/trial/res/answer.txt");
            Scanner fileScanner = new Scanner(new File("src/main/resources/test.txt"));

            // record scores with treshold set to tresh
            while (fileScanner.hasNext()) {
                String[] currentLine = fileScanner.nextLine().split(",");
                int currentResult = compare(currentLine[0], currentLine[1], currentLine[2], graph, tresh);
                resultWriter.write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2]
                        + "," + currentResult
                        + lineSeparator);

            }
            resultWriter.close();

            // evaluate the scores
            Process eval = Runtime.getRuntime().exec("python3 src/main/resources/trial/evaluation.py src/main/resources/trial/ src/main/resources/trial/");
            eval.waitFor();
            Scanner scoreScanner = new Scanner(new File("src/main/resources/trial/scores.txt"));
            Double result = scoreScanner.nextDouble();
            logWriter.write("Result for " + tresh + ": " + result + lineSeparator);
            if (Double.compare(result, maxScore) > 0) {
                bestTresh = tresh;
                maxScore = result;
            }

        }
        logWriter.write(lineSeparator + "Best performing treshold: " + bestTresh + lineSeparator);
        logWriter.write("Best result: " + maxScore + lineSeparator);
        logWriter.close();

    }

    private static List<String> getHypernyms(String term) throws JWNLException {

        IndexWord indexWord = dictionary.getIndexWord(POS.NOUN, term);
        List<String> result = new ArrayList<>();

        try {
            Synset[] senses = indexWord.getSenses();
            for (Synset sense : senses) {
                PointerTargetNodeList hypernyms = PointerUtils.getInstance().getDirectHypernyms(sense);
                for (Iterator itr = hypernyms.iterator(); itr.hasNext(); ) {
                    PointerTargetNode node = (PointerTargetNode) itr.next();
                    Synset synset = node.getSynset();
                    for (Word compositeHypernym : synset.getWords()) {
                        String[] hypernymArray = compositeHypernym.getLemma().split("_");
                        for (String hypernym : hypernymArray) {
                            if (!result.contains(hypernym)) {
                                result.add(hypernym);
                            }
                        }
                    }
                }
            }

        } catch (JWNLException e) {
            System.out.println(e);
        }
        return result;

    }

    public static int compare(String pivot, String comparison, String feature, Graph graph, Double tresh) throws Exception {
        pivot = pivot.toLowerCase();
        comparison = comparison.toLowerCase();
        feature = feature.toLowerCase();

        String pivotComparisonFeature = pivot + comparison + feature;

        if (resultTab.containsKey(pivotComparisonFeature)){
            if (resultTab.get(pivotComparisonFeature)) {
                return 1;
            }
        }else{
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

            // logString += "Occurrences of " + feature + " in definition of " + pivot + " :" + lineSeparator;
            ScoreDoc[] resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
            /*
            for (ScoreDoc result : resultsPivot) {
                logString += searcher.doc(result.doc) + lineSeparator;
            }
            */

            // logString += "Occurrences of " + feature + " in definition of " + comparison + " :" + lineSeparator;
            ScoreDoc[] resultsComparison = searcher.search(queryComparison, 10).scoreDocs;
            /*
            for (ScoreDoc result : resultsComparison) {
                logString += searcher.doc(result.doc) + lineSeparator;
            }
            */


            Boolean result = resultsPivot.length != 0 && resultsComparison.length == 0;
            resultTab.put(pivotComparisonFeature, result);
            if (result) {
                return 1;
            }
        }

        double similarity;
        if (simTab.containsKey(pivotComparisonFeature)) {
            similarity = simTab.get(pivotComparisonFeature);
        } else {

            similarity = similarityDiff(pivot, comparison, feature);
            simTab.put(pivotComparisonFeature, similarity);
        }
        if (Double.compare(similarity, tresh) > 0) {
            return 1;
        }
        return 0;
    }

    private static double hypernymSimilarityDiff(String pivot, String comparison, String feature, Graph graph) throws IOException {
        Double diff = similarityDiff(pivot, comparison, feature);

        List<String> pivotHypernyms, comparisonHypernyms;
        try {
            pivotHypernyms = getHypernyms(pivot);
            comparisonHypernyms = getHypernyms(comparison);
        } catch (Exception e) {
            logString += e + lineSeparator;
            return diff;
        }

        logString += "Pivot Hypernym: " + pivotHypernyms + " Comparison Hypernym: " + comparisonHypernyms
                + lineSeparator;

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/relatedness");
        httppost.setHeader("Content-Type", "application/json");

// Request parameters and other properties.
        Double pivotHypernymSim = getMaxSim(feature, pivotHypernyms, httpclient, httppost);
        Double comparisonHypernymSim = getMaxSim(feature, comparisonHypernyms, httpclient, httppost);
        Double hypernymDiff = Math.abs(pivotHypernymSim - comparisonHypernymSim);
        logString += "Difference between hypernyms: " + hypernymDiff + lineSeparator;

        if (Double.compare(hypernymDiff, diff) > 0) {
            return hypernymDiff;
        } else {
            return diff;
        }

    }

    private static double getMaxSim(String feature, List<String> hypernymList, HttpClient httpclient, HttpPost httppost) throws IOException {
        String pivotParams = "{\n" +
                "\t\"corpus\": \"googlenews300neg\",\n" +
                "\t\"model\": \"W2V\",\n" +
                "\t\"language\": \"EN\",\n" +
                "\t\"scoreFunction\": \"COSINE\",\n" +
                "\t\"pairs\": [";

        for (int i = 0; i < (hypernymList.size() - 1); i++) {
            pivotParams += "{ \"t1\": \"" + hypernymList.get(i) + "\", \"t2\": \"" + feature + "\"}, ";
        }
        pivotParams += "{ \"t1\": \"" + hypernymList.get(hypernymList.size() - 1) + "\", \"t2\" : \"" + feature + "\"}]}";
        httppost.setEntity(new ByteArrayEntity(pivotParams.getBytes("UTF-8")));

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
                JSONArray pairs = new JSONObject(theString).getJSONArray("pairs");

                String maxHypernym = pairs.getJSONObject(0).get("t1").toString();
                Double pivotMaxSim = pairs.getJSONObject(0).getDouble("score");
                for (int i = 1; i < hypernymList.size(); i++) {
                    Double currentSim = pairs.getJSONObject(i).getDouble("score");
                    if (Double.compare(pivotMaxSim, currentSim) < 0) {
                        pivotMaxSim = currentSim;
                        maxHypernym = pairs.getJSONObject(i).get("t1").toString();
                    }
                }
                logString += "Hypernym with the highest similarity to feature: " + maxHypernym + "(" + pivotMaxSim + ")"
                        + lineSeparator;
                return pivotMaxSim;

            } catch (Exception e) {
                System.out.println(status);
                System.out.println(e);
            } finally {
                instream.close();
            }
        }
        return 0;
    }

    private static double similarityDiff(String pivot, String comparison, String feature) throws IOException{
        try {
            Thread.sleep(500);
        }catch (InterruptedException e){}

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/relatedness");
        httppost.setHeader("Content-Type", "application/json");

// Request parameters and other properties.
        String params = "{\n" +
                "\t\"corpus\": \"googlenews300neg\",\n" +
                "\t\"model\": \"W2V\",\n" +
                "\t\"language\": \"EN\",\n" +
                "\t\"scoreFunction\": \"COSINE\",\n" +
                "\t\"pairs\": [{\n" +
                "\t\t\"t1\": \"" + pivot + "\",\n" +
                "\t\t\"t2\": \"" + feature + "\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"t1\": \"" + comparison + "\",\n" +
                "\t\t\"t2\": \"" + feature + "\"\n" +
                "\t}]\n" +
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
                JSONArray pairs = new JSONObject(theString).getJSONArray("pairs");

                // note: the similarity between last request is served first
                Double similarity1 = pairs.getJSONObject(1).getDouble("score");
                /*
                logString += "Similarity score between " + pairs.getJSONObject(0).getString("t1") + " and "
                        + pairs.getJSONObject(0).getString("t2") + " is " + similarity1
                        + lineSeparator;
                */

                Double similarity2 = pairs.getJSONObject(0).getDouble("score");
                /*
                logString += "Similarity score between " + pairs.getJSONObject(1).getString("t1") + " and "
                        + pairs.getJSONObject(1).getString("t2") + " is " + similarity2
                        + lineSeparator;

                logString += "The difference between similarity scores is " + Math.abs(similarity1 - similarity2)
                        + lineSeparator;
                */

                return similarity1 - similarity2;

            } catch (Exception e) {
                /*
                logString += "While finding similarity scores for " + pivot + " " + comparison
                        + " " + feature + " exception occurred " + e + lineSeparator;
                */
                System.out.println(status);
                System.out.println(e);
                System.out.println(pivot + " " + comparison + " " + feature);
            } finally {
                instream.close();
            }
        }

        return 0;

    }

    /**
     * Checks whether <code>test</code> can be found within k nearest neighbours of <code>center</code>.
     *
     * @param center term for which k nearest neighbours are found
     * @param test   term for which to search for amongst the k nearest neighbours
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
                "        \"model\": \"" + model + "\",\n" +
                "        \"language\": \"EN\",\n" +
                "        \"topk\": " + k + ",\n" +
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
                while (key.hasNext()) {
                    if (key.next().toString().equals(test)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println(center + " " + test);
            } finally {
                instream.close();
            }
        }
        return false;
    }
}
