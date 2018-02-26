package analysis;

import indexation.VisualGenomeIndexer;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.*;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
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

    private static Directory graphDirectory, VGDirectory;
    private static String logString = "";
    private static String lineSeparator = System.getProperty("line.separator");
    private static Dictionary dictionary;

    private static Hashtable<String, Boolean> resultTab;
    private static Hashtable<String, Double> simTab;


    public static void main(String[] args) throws Exception {
        /*
        JWNL.initialize(new FileInputStream("src/main/resources/properties.xml"));
        dictionary = Dictionary.getInstance();
        */

        graphDirectory = FSDirectory.open(Paths.get("src", "main", "resources", "index"));

        Graph graph = new Graph("WN_DSR_model_XML.rdf");
        indexGraph(graph, graphDirectory);

        VGDirectory = FSDirectory.open(Paths.get("src", "main", "resources", "VGIndex"));
        File JSONFile = new File("src/main/resources/attributes.json");
        VisualGenomeIndexer.indexGenomeAttributes(JSONFile, VGDirectory);

        String[] models = new String[]{"LSA", "GloVe", "W2V"};
        // for(String model : models) {
        String model = "W2V";
        Double tresh = 0.03;
            String versionName = "VGPlain";

            resultTab = new Hashtable<>();
            simTab = new Hashtable<>();


            FileWriter logWriter = new FileWriter("src/main/resources/" + versionName + ".log");
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            logWriter.write("Log for " + versionName + " of discriminative attribute detection model"
                    + lineSeparator);
            logWriter.write("logged on " + sdf.format(new Date()) + lineSeparator);
            logWriter.write("-------------------------------------------------------------------------------"
                    + lineSeparator);
            Double maxScore = -1.0;
            Double bestTresh = -1.0;
            // for(Double tresh = 0.0; tresh.compareTo(1.0) < 0; tresh += 0.01) {
                FileWriter resultWriter = new FileWriter("src/main/resources/" + versionName + ".results");
                Scanner fileScanner = new Scanner(new File("src/main/resources/truth.txt"));

                // record scores with treshold set to tresh

                while (fileScanner.hasNext()) {
                    String[] currentLine = fileScanner.nextLine().split(",");
                    int currentResult = compare(currentLine[0], currentLine[1], currentLine[2], graph, tresh, model);
                    resultWriter.write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2]
                            + "," + currentResult
                            + lineSeparator);
                    if (currentResult != Integer.valueOf(currentLine[3])) {
                        logWriter.write("-------------------------------" + lineSeparator);
                        logWriter.write(currentLine[0] + "," + currentLine[1] + "," + currentLine[2]
                                + "," + currentLine[3] + ":" + currentResult
                                + lineSeparator);
                        logWriter.write(logString);
                        logWriter.write("-------------------------------" + lineSeparator);
                    }
                    logString = "";

                }
                resultWriter.close();

                /*
                // evaluate the scores
                Process eval = Runtime.getRuntime().exec("python3 src/main/resources/trial/evaluation.py src/main/resources/trial/ src/main/resources/trial/");
                eval.waitFor();
                Scanner scoreScanner = new Scanner(new File("src/main/resources/trial/scores.txt"));
                Double result;
                try {
                    result = scoreScanner.nextDouble();
                } catch (Exception e){
                    result = 0.0;
                }
                logWriter.write("Result for " + tresh + ": " + result + lineSeparator);
                if (Double.compare(result, maxScore) > 0) {
                    bestTresh = tresh;
                    maxScore = result;
                }

            }
            logWriter.write(lineSeparator + "Best performing treshold: " + bestTresh + lineSeparator);
            logWriter.write("Best result: " + maxScore + lineSeparator);
            */
            logWriter.close();
        //}

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

    public static int compare(String pivot, String comparison, String feature, Graph graph, Double tresh, String model) throws Exception {
        pivot = pivot.toLowerCase();
        comparison = comparison.toLowerCase();
        feature = feature.toLowerCase();

        String pivotComparisonFeature = pivot + comparison + feature;
        /*
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

            ScoreDoc[] resultsPivot = searcher.search(queryPivot, 10).scoreDocs;

            ScoreDoc[] resultsComparison = searcher.search(queryComparison, 10).scoreDocs;

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

            Double[] similarityScores = getSimilarity(pivot, comparison, feature, model);
            similarity = similarityScores[0] - similarityScores[1];
            simTab.put(pivotComparisonFeature, similarity);
        }
        if (Double.compare(similarity, tresh) > 0) {
            return 1;
        }
        return 0;

        */


        // Wordnet Queries
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

        BooleanQuery.Builder builderVGPivot = new BooleanQuery.Builder();
        BooleanQuery.Builder builderVGComparison = new BooleanQuery.Builder();
        builderVGPivot.add(new TermQuery(new Term("name", pivot)), BooleanClause.Occur.MUST);
        builderVGPivot.add(new TermQuery(new Term("attribute", feature)), BooleanClause.Occur.MUST);
        builderVGComparison.add(new TermQuery(new Term("name", comparison)), BooleanClause.Occur.MUST);
        builderVGComparison.add(new TermQuery(new Term("attribute", feature)), BooleanClause.Occur.MUST);

        BooleanQuery queryVGPivot = builderVGPivot.build();
        BooleanQuery queryVGComparison = builderVGComparison.build();
        DirectoryReader readerVG = DirectoryReader.open(VGDirectory);
        IndexSearcher searcherVG = new IndexSearcher(readerVG);

        ScoreDoc[] resultsVGPivot = searcherVG.search(queryVGPivot, 10).scoreDocs;
        ScoreDoc[] resultsVGComparison = searcherVG.search(queryVGComparison, 10).scoreDocs;

        if(resultsVGPivot.length > 0 && resultsVGComparison.length == 0){
            return 1;
        }



        // Wordnet Queries
        //logString += "Occurrences of " + feature + " in definition of " + pivot + " :" + lineSeparator;
        ScoreDoc[] resultsPivot = searcher.search(queryPivot, 10).scoreDocs;
        /*
        for (ScoreDoc result : resultsPivot) {
            IndexableField[] fields = searcher.doc(result.doc).getFields("property");
            for (IndexableField field : fields) {
                logString += field.stringValue() + " ";
            }
            logString += lineSeparator;
        }
        */

        // logString += "Occurrences of " + feature + " in definition of " + comparison + " :" + lineSeparator;
        ScoreDoc[] resultsComparison = searcher.search(queryComparison, 10).scoreDocs;
        /*
        for (ScoreDoc result : resultsComparison) {
            IndexableField[] fields = searcher.doc(result.doc).getFields("property");
            for (IndexableField field : fields) {
                logString += field.stringValue() + " ";
            }
            logString += lineSeparator;
        }
        */

        Boolean result = resultsPivot.length != 0 && resultsComparison.length == 0;
        resultTab.put(pivotComparisonFeature, result);
        if (result) {
            return 1;
        }

        // double similarity = similarityDiff(pivot, comparison, feature);
        Double[] similarityArr = getSimilarity(pivot, comparison, feature, model);
        double similarity = similarityArr[0] - similarityArr[1];

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
        Double comparisonHypernymSim = getMaxSim(comparison, comparisonHypernyms, httpclient, httppost);
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

    private static double similarityDiff(String pivot, String comparison, String feature) throws IOException {
        /*
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
        */

        try {
                /*
                StringWriter writer = new StringWriter();
                IOUtils.copy(instream, writer, "UTF-8");
                String theString = writer.toString();
                JSONArray pairs = new JSONObject(theString).getJSONArray("pairs");

                // note: the similarity between last request is served first
                Double similarity1 = pairs.getJSONObject(1).getDouble("score");
                */
            Double[] similarity = getSimilarity(pivot, comparison, feature, "LSA");
            logString += "LSA similarity" + lineSeparator;
            logString += "Similarity score between " + pivot + " and "
                    + feature + " is " + similarity[0]
                    + lineSeparator;

            logString += "Similarity score between " + comparison + " and "
                    + feature + " is " + similarity[1]
                    + lineSeparator;

            logString += "The difference between similarity scores is " + (similarity[0] - similarity[1])
                    + lineSeparator;


            similarity = getSimilarity(pivot, comparison, feature, "ESA");
            logString += "ESA similarity" + lineSeparator;
            logString += "Similarity score between " + pivot + " and "
                    + feature + " is " + similarity[0]
                    + lineSeparator;

            logString += "Similarity score between " + comparison + " and "
                    + feature + " is " + similarity[1]
                    + lineSeparator;

            logString += "The difference between similarity scores is " + (similarity[0] - similarity[1])
                    + lineSeparator;

            similarity = getSimilarity(pivot, comparison, feature, "GloVe");
            logString += "GloVe similarity" + lineSeparator;
            logString += "Similarity score between " + pivot + " and "
                    + feature + " is " + similarity[0]
                    + lineSeparator;

            logString += "Similarity score between " + comparison + " and "
                    + feature + " is " + similarity[1]
                    + lineSeparator;

            logString += "The difference between similarity scores is " + (similarity[0] - similarity[1])
                    + lineSeparator;

            similarity = getSimilarity(pivot, comparison, feature, "W2V");
            logString += "W2V similarity" + lineSeparator;
            logString += "Similarity score between " + pivot + " and "
                    + feature + " is " + similarity[0]
                    + lineSeparator;

            logString += "Similarity score between " + comparison + " and "
                    + feature + " is " + similarity[1]
                    + lineSeparator;

            logString += "The difference between similarity scores is " + (similarity[0] - similarity[1])
                    + lineSeparator;

            logString += "10 closest neighbours:" + lineSeparator;
            logString += getNeighbours(pivot, comparison, feature, 10);
            logString += lineSeparator;
            return similarity[0] - similarity[1];
        } catch (Exception e) {
            logString += "While finding similarity scores for " + pivot + " " + comparison
                    + " " + feature + " exception occurred " + e + lineSeparator;
            e.printStackTrace();
            System.out.println(pivot + " " + comparison + " " + feature);
        }

        return 0;

    }

    static String getNeighbours(String pivot, String comparison, String feature, int k) throws Exception {
        Thread.sleep(500);
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/neighbors/relatedness");
        httppost.setHeader("Content-Type", "application/json");

// Request parameters and other properties.
        String params = "{\n" +
                "        \"corpus\": \"wiki-2014\",\n" +
                "        \"model\": \"W2V\",\n" +
                "        \"language\": \"EN\",\n" +
                "        \"topk\": " + k + ",\n" +
                "\"scoreFunction\": \"COSINE\",\n" +
                "        \"terms\" : [\"" + pivot + "\", \"" + comparison + "\", \"" + feature + "\"] \n" +
                "}";
        httppost.setEntity(new ByteArrayEntity(params.getBytes("UTF-8")));

//Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(instream, writer, "UTF-8");
            return writer.toString();
        }
        return "";
    }

    private static Double[] getSimilarity(String pivot, String comparison, String feature, String model) throws Exception {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/relatedness");
        httppost.setHeader("Content-Type", "application/json");


        String params = "{\n" +
                "\t\"corpus\": \"wiki-2014\",\n" +
                "\t\"model\": \"" + model + "\",\n" +
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
        HttpEntity entity = response.getEntity();

        InputStream instream = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(instream, writer, "UTF-8");
        String theString = writer.toString();
        JSONArray pairs = new JSONObject(theString).getJSONArray("pairs");
        instream.close();
        Double pivotSim, comparisonSim;
        if(pairs.getJSONObject(0).getString("t1").equals(pivot)) {
            pivotSim = pairs.getJSONObject(0).getDouble("score");
            comparisonSim = pairs.getJSONObject(1).getDouble("score");
        }else {
            pivotSim = pairs.getJSONObject(1).getDouble("score");
            comparisonSim = pairs.getJSONObject(0).getDouble("score");
        }
        return new Double[]{pivotSim, comparisonSim};
    }

    /**
     * Checks whether <code>test</code> can be found within k nearest neighbours of <code>center</code>.
     *
     * @param center term for which k nearest neighbours are found
     * @param test   term for which to search for amongst the k nearest neighbours
     * @throws IOException
     * @throws JSONException
     */
    private static boolean isFoundInKNN(String center, String test, String model, int k) throws IOException {
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
