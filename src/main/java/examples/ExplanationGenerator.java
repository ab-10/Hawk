package examples;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Scanner;

public class ExplanationGenerator {
    public static void main(String args[]) throws IOException, JSONException {
        Scanner taskScanner = new Scanner(new File("src/main/resources/truth.txt"));
        FileWriter output = new FileWriter(new File("out/explainedTriples.txt"));
        HttpClient client = HttpClients.createDefault();

        while (taskScanner.hasNext()) {
            String currentLine = taskScanner.nextLine();
            String[] splitLine = currentLine.split(",");
            String request = MessageFormat.format("http://localhost:8080/api/?pivot={0}&comparison={1}&feature={2}"
                    , splitLine[0], splitLine[1], splitLine[2]);
            output.write(currentLine + ";");

            HttpGet get = new HttpGet(request);
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            StringWriter writer = new StringWriter();
            IOUtils.copy(entity.getContent(), writer, "UTF-8");
            JSONObject responseJSON = new JSONObject(writer.toString());

            JSONArray WKP_Graph = responseJSON.getJSONArray("WKP_Graph");
            String decision = WKP_Graph.get(0).toString();
            if (decision.equals("-1")) {
                decision = "0";
            }
            String correct = (decision.equals(splitLine[3]) ? "T" : "F");
            output.write(WKP_Graph.get(0) + correct + ";");
            output.write(WKP_Graph.get(1) + ";");

            JSONArray WKT = responseJSON.getJSONArray("WKT");
            decision = WKT.get(0).toString();
            if (decision.equals("-1")) {
                decision = "0";
            }
            correct = (decision.equals(splitLine[3]) ? "T" : "F");
            output.write(WKT.get(0) + correct + ";");
            output.write(WKT.get(1) + ";");

            JSONArray WN = responseJSON.getJSONArray("WN");
            decision = WN.get(0).toString();
            if (decision.equals("-1")) {
                decision = "0";
            }
            correct = (decision.equals(splitLine[3]) ? "T" : "F");
            output.write(WN.get(0) + correct + ";");
            output.write(WN.get(1) + "\n");
        }

        output.close();
        taskScanner.close();
    }
}
