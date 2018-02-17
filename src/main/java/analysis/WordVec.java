package analysis;

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
 * Class for representing and manipulating word embeddings.
 */
public class WordVec {
    private Double[] embedding;

    /**
     * @param word  word that should be represented by the embedding.
     * @param model vector space model according to which representation should be made.
     */
    public WordVec(String word, String model) {
        try {
            embedding = generateEmbedding(word, model);
        } catch (Exception e) {
            System.err.println("Unable to process requested word representation");
            System.err.println(e);
        }
    }


    /**
     * Requests embedding for <code>word</code> using Indra API and generates an array for it.
     *
     * @param word  word for which to request the embedding.
     * @param model model for which to request the embedding.
     * @return array of embeddings for <code>word</code>, if they are found within Indra API, array of size 0 otherwise.
     * @throws JSONException
     * @throws IOException
     */
    private static Double[] generateEmbedding(String word, String model) throws JSONException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://indra.lambda3.org/vectors");
        httppost.setHeader("Content-Type", "application/json");

        String requestData = "{\n" +
                "\"corpus\": \"wiki-2014\",\n" +
                "\"model\": \"" + model + "\",\n" +
                "\"language\": \"EN\",\n" +
                "\"terms\": [\"" + word + "\"]\n" +
                "}";
        httppost.setEntity(new ByteArrayEntity(requestData.getBytes("UTF-8")));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        String result;
        Double[] embeddingArray;
        if (entity != null) {
            InputStream instream = entity.getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(instream, writer, "UTF-8");
            result = writer.toString();

            JSONArray embedding = new JSONObject(result).getJSONObject("terms").getJSONArray(word);
            embeddingArray = new Double[embedding.length()];
            for (int i = 0; i < embedding.length(); i++) {
                embeddingArray[i] = embedding.getDouble(i);
            }
        } else {
            embeddingArray = new Double[0];
        }
        return embeddingArray;
    }

}
