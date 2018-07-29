package indexation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.lang.Runtime.getRuntime;

public class GoogleNGramsIndexer {
    public static void main(String[] args) throws IOException, InterruptedException {
        indexNGrams("/home/sartre/Documents/SemEval/GoogleNgram/", FSDirectory.open(Paths.get("src/main/resources/NGramsIndex")));
    }

    public static void indexNGrams(String ngramsDirectory, Directory indexDirectory) throws IOException, InterruptedException {
        File dir = new File(ngramsDirectory);
        String ngramPattern = "googlebooks-eng-all-2gram-20120701-[a-z][a-z].gz";

        File[] NGramArchives = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().matches(ngramPattern);
            }
        });

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

        ProcessBuilder extractionProcess = new ProcessBuilder("");
        extractionProcess.directory(dir);
        extractionProcess.inheritIO();

        for (File NGramArchive : NGramArchives) {
            String archiveName = NGramArchive.getName();
            String extractedName = archiveName.substring(0, archiveName.lastIndexOf("."));

            // Extracts NGram archive into file named extractedName while preserving the archive
            extractionProcess.command("sh", "extract.sh", archiveName, extractedName);
            extractionProcess.start().waitFor();
            System.out.println("Extracted " + archiveName);

            Scanner ngramScanner = new Scanner(new File(dir, extractedName));

            String lastT1 = "";
            String lastT2 = "";

            String currentT1;
            String currentT2;

            int lastMatchCount = 0;
            int currentMatchCount;

            int lastVolumeCount = 0;
            int currentVolumeCount;
            boolean firstRun = true;

            while (ngramScanner.hasNext()) {
                String[] currentLine = ngramScanner.nextLine().split("\t");
                if (currentLine[0].matches("(?i-:^([a-z]{3}[a-z]*(_[a-z]*_?)?) ([a-z]{3}[a-z]*(_[a-z]*_?)?)$)")) {
                    currentT1 = currentLine[0].split(" ")[0];
                    currentT2 = currentLine[0].split(" ")[1];

                    if (currentLine[0].contains("_")) {
                        int pos1 = currentT1.indexOf("_");
                        int pos2 = currentT2.indexOf("_");

                        if (pos1 > -1) {
                            currentT1 = currentT1.substring(0, pos1);
                        }
                        if (pos2 > -1) {
                            currentT2 = currentT2.substring(0, pos2);
                        }
                    }

                    currentMatchCount = Integer.valueOf(currentLine[2]);
                    currentVolumeCount = Integer.valueOf(currentLine[3]);

                    if (currentT2.equals(lastT2) & currentT1.equals(lastT1) | firstRun) {
                        lastVolumeCount += currentVolumeCount;
                        lastMatchCount += currentMatchCount;
                        firstRun = false;
                    } else {
                        add2gramDocument(lastT1, lastT2, lastMatchCount, lastVolumeCount, indexWriter);

                        lastT1 = currentT1;
                        lastT2 = currentT2;
                        lastMatchCount = currentMatchCount;
                        lastVolumeCount = currentVolumeCount;
                    }
                }
            }

            // Adds the last values to the index
            add2gramDocument(lastT1, lastT2, lastMatchCount, lastVolumeCount, indexWriter);

            // Removes the extracted archive
            extractionProcess.command("rm", extractedName);
            extractionProcess.start().waitFor();
        }

        indexWriter.close();
    }

    /**
     * Constructs and adds a document containing given 2gram data to index.
     *
     * @param term1       first term of 2gram
     * @param term2       second term of 2gram
     * @param matchCount  number of occurrences of this 2gram
     * @param volumeCount number of publications containing this 2gram
     * @param indexWriter the index writer used for the desired index
     */
    private static void add2gramDocument(String term1, String term2,
                                         int matchCount, int volumeCount, IndexWriter indexWriter) throws IOException {
        Document currentDocument = new Document();
        currentDocument.add(new StringField("term", term1, Field.Store.NO));
        currentDocument.add(new StringField("term", term2, Field.Store.NO));
        currentDocument.add(new StoredField("matchCount", matchCount));
        currentDocument.add(new StoredField("volumeCount", volumeCount));
        indexWriter.addDocument(currentDocument);

    }
}
