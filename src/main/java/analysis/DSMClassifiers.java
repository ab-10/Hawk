package analysis;

import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * A collection of discriminativity classifiers based on Distributional Semantic Models
 */
public class DSMClassifiers {
    // best performing tresholds for each model
    public static final Double W2V_TRESH = 0.12;
    public static final Double LSA_TRESH = 0.13;
    public static final Double GLOVE_TRESH = 0.04;

    public static int w2vVote(String pivot, String comparison, String feature)throws IOException, JSONException {
        pivot = new Sentence(pivot).lemma(0);
        comparison = new Sentence(comparison).lemma(0);
        feature = new Sentence(feature).lemma(0);
        if (Double.compare(getSimilarityDiff(pivot, comparison, feature, "W2V"), W2V_TRESH) > 0) {
            return 1;
        }else{
            return 0;
        }
    }

    public static int lsaVote(String pivot, String comparison, String feature) throws IOException, JSONException{
        if (Double.compare(getSimilarityDiff(pivot, comparison, feature, "LSA"), LSA_TRESH) > 0) {
            return 1;
        }else{
            return 0;
        }
    }

    public static int gloVeVote(String pivot, String comparison, String feature) throws IOException, JSONException{
        if (Double.compare(getSimilarityDiff(pivot, comparison, feature, "GloVe"), GLOVE_TRESH) > 0) {
            return 1;
        }else{
            return 0;
        }
    }

    private static Double getSimilarityDiff(String pivot, String comparison, String feature, String model)
            throws IOException, JSONException{
        // waits in order to ensure that consecutive requests do not exceed 2 requests per second
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/relatedness");
        httppost.setHeader("Content-Type", "application/json");


        String params = "{" +
                "\"corpus\": \"wiki-2018\"," +
                "\"model\": \"" + model + "\"," +
                "\"language\": \"EN\"," +
                "\"scoreFunction\": \"COSINE\"," +
                "\"pairs\": [{" +
                "\"t1\": \"" + pivot + "\"," +
                "\"t2\": \"" + feature + "\"" +
                "}," +
                "{" +
                "\"t1\": \"" + comparison + "\"," +
                "\"t2\": \"" + feature + "\"" +
                "}]" +
                "}";


        httppost.setEntity(new ByteArrayEntity(params.getBytes("UTF-8")));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        InputStream instream = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(instream, writer, "UTF-8");
        String theString = writer.toString();
        JSONArray pairs = new JSONObject(theString).getJSONArray("pairs");
        instream.close();
        Double pivotSim, comparisonSim;

        // checks in which order the results have been returned
        if (pairs.getJSONObject(0).getString("t1").equals(pivot)) {
            pivotSim = pairs.getJSONObject(0).getDouble("score");
            comparisonSim = pairs.getJSONObject(1).getDouble("score");
        } else {
            pivotSim = pairs.getJSONObject(1).getDouble("score");
            comparisonSim = pairs.getJSONObject(0).getDouble("score");
        }
        return pivotSim - comparisonSim;
    }
}
