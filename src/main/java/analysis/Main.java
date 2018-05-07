package analysis;

import indexation.VisualGenomeIndexer;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Scanner;

import static analysis.DSMClassifiers.*;
import static analysis.DictionaryClassifiers.visualGenomeVote;
import static analysis.DictionaryClassifiers.wordNetVote;

public class Main {

    public static void main(String[] args) throws Exception {

        String wnNoHypIndexDirectory = "src/main/resources/WNNoHyp";
        String wnHypIndexDirectory = "src/main/resources/WNWithHyp";
        String vgDirectory = "src/main/resources/VG";

        File JSONFile = new File("src/main/resources/attributes.json");
        VisualGenomeIndexer.indexGenomeAttributes(JSONFile, FSDirectory.open(Paths.get(vgDirectory)));

        Hashtable<String, Integer> w2vResults = new Hashtable<>();
        Hashtable<String, Integer> wnResults = new Hashtable<>();
        Hashtable<String, Integer> vgResults = new Hashtable<>();


        String[] versions = new String[]{"WNwithHyp", "WNnoHyp", "LSA", "W2V", "GloVe", "VG", "GloVeWN", "GloVeWNVG"};

        for (int i = 0; i < versions.length; i++) {
            FileWriter resultWriter = new FileWriter("src/main/resources/" + versions[i] + ".results");
            Scanner taskScanner = new Scanner(new File("src/main/resources/truth.txt"));
            while (taskScanner.hasNext()) {
                String[] nextLine = taskScanner.nextLine().split(",");
                if (nextLine[0].charAt(0) == '-') {
                    resultWriter.write(nextLine[0] + "\n");
                } else {
                    switch (i) {
                        case (0): // evaluation of WordNet model without hypernyms
                            int wn = wordNetVote(nextLine[0], nextLine[1], nextLine[2], wnNoHypIndexDirectory);
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + wn + "\n");
                            break;

                        case (1): // evaluation of WordNet model with hypernyms
                            wn = wordNetVote(nextLine[0], nextLine[1], nextLine[2], wnHypIndexDirectory);
                            wnResults.put(nextLine[0] + nextLine[1] + nextLine[2], wn);
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + wn + "\n");
                            break;

                        case (2): // evaluation of LSA similarity
                            int lsa = lsaVote(nextLine[0], nextLine[1], nextLine[2]);
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + lsa + "\n");
                            break;

                        case (3): // evaluation of W2V similarity
                            int w2v = w2vVote(nextLine[0], nextLine[1], nextLine[2]);
                            w2vResults.put(nextLine[0] + nextLine[1] + nextLine[2], w2v);
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + w2v + "\n");
                            break;

                        case (4): // evaluation of GloVe similarity
                            int gloVe = gloVeVote(nextLine[0], nextLine[1], nextLine[2]);
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + gloVe + "\n");
                            break;

                        case (5): // evaluation of Visual Genome
                            int vg = visualGenomeVote(nextLine[0], nextLine[1], nextLine[2], vgDirectory);
                            vgResults.put(nextLine[0] + nextLine[1] + nextLine[2], vg);
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + vg + "\n");
                            break;

                        case (6): // evaluation of combined similarity of W2V and WordNet with hypernyms
                            int result = 0;
                            String terms = nextLine[0] + nextLine[1] + nextLine[2];
                            if (wnResults.get(terms) == 1 | w2vResults.get(terms) == 1) {
                                result = 1;
                            }
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + result + "\n");
                            break;

                        case (7): // evaluation of combined similarity of Visual Genome, W2V and WordNet with hypernyms
                            result = 0;
                            terms = nextLine[0] + nextLine[1] + nextLine[2];
                            if (vgResults.get(terms) == 1 | wnResults.get(terms) == 1 | w2vResults.get(terms) == 1) {
                                result = 1;
                            }
                            resultWriter.write(nextLine[0] + "," + nextLine[1] + "," + nextLine[2] + ","
                                    + result + "\n");
                            break;
                    }
                }
            }
            resultWriter.close();
        }


    }

}
