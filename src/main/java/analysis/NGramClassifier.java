package analysis;

import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class NGramClassifier {
    public static void main(String[] args) throws IOException {
        Scanner truthScanner = new Scanner(new File("src/main/resources/truth.txt"));
        ArrayList<Integer> discVolumeCountDif = new ArrayList<>();
        ArrayList<Integer> ndVolumeCountDif = new ArrayList<>();
        ArrayList<Integer> discMatchCountDif = new ArrayList<>();
        ArrayList<Integer> ndMatchCountDif = new ArrayList<>();

        ArrayList<Double> discVolumeCountCoef = new ArrayList<>();
        ArrayList<Double> ndVolumeCountCoef = new ArrayList<>();
        ArrayList<Double> discMatchCountCoef = new ArrayList<>();
        ArrayList<Double> ndMatchCountCoef = new ArrayList<>();


        while (truthScanner.hasNext()) {
            String[] currentLine = truthScanner.nextLine().split(",");
            int pivotMatchCount = getMatchCount(currentLine[0], currentLine[2]);
            int comparisonMatchCount = getMatchCount(currentLine[1], currentLine[2]);
            int pivotVolumeCount = getVolumeCount(currentLine[0], currentLine[2]);
            int comparisonVolumeCount = getVolumeCount(currentLine[1], currentLine[2]);

            if (currentLine[3].equals("0")) {
                ndMatchCountDif.add(pivotMatchCount - comparisonMatchCount);
                if(comparisonVolumeCount > 0) {
                    ndMatchCountCoef.add((double) pivotMatchCount / comparisonMatchCount);
                }else {
                    ndMatchCountCoef.add(0.0);
                }

                ndVolumeCountDif.add(pivotVolumeCount - comparisonVolumeCount);
                if(comparisonVolumeCount > 0) {
                    ndVolumeCountCoef.add((double) pivotVolumeCount / comparisonVolumeCount);
                }else {
                    ndVolumeCountCoef.add(0.0);
                }
            } else {
                discMatchCountDif.add(pivotMatchCount - comparisonMatchCount);
                if(comparisonMatchCount > 0) {
                    discMatchCountCoef.add((double) pivotMatchCount / comparisonMatchCount);
                }else{
                    discVolumeCountCoef.add(0.0);
                }

                discVolumeCountDif.add(pivotVolumeCount - comparisonVolumeCount);
                if(comparisonVolumeCount > 0) {
                    discVolumeCountCoef.add((double) pivotVolumeCount / comparisonVolumeCount);
                }else {
                    discVolumeCountCoef.add(0.0);
                }
            }
        }

        System.out.println("VOLUME COUNT (D)");
        System.out.println("diff");
        System.out.println("min " + getMin(discVolumeCountDif));
        System.out.println("max " + getMax(discVolumeCountDif));
        System.out.println("mean " + getMean(discVolumeCountDif));
        System.out.println("coef");
        System.out.println("min " + getMin(discVolumeCountCoef));
        System.out.println("max " + getMax(discVolumeCountCoef));
        System.out.println("mean " + getMean(discVolumeCountCoef));
        System.out.println("VOLUME COUNT (N/D)");
        System.out.println("diff");
        System.out.println("min " + getMin(ndVolumeCountDif));
        System.out.println("max " + getMax(ndVolumeCountDif));
        System.out.println("mean " + getMean(ndVolumeCountDif));
        System.out.println("coef");
        System.out.println("min " + getMin(ndVolumeCountCoef));
        System.out.println("max " + getMax(ndVolumeCountCoef));
        System.out.println("mean " + getMean(ndVolumeCountCoef));
        System.out.println("MATCH COUNT (D)");
        System.out.println("diff");
        System.out.println("min " + getMin(discMatchCountDif));
        System.out.println("max " + getMax(discMatchCountDif));
        System.out.println("mean " + getMean(discMatchCountDif));
        System.out.println("coef");
        System.out.println("min " + getMin(discMatchCountCoef));
        System.out.println("max " + getMax(discMatchCountCoef));
        System.out.println("mean " + getMean(discMatchCountCoef));
        System.out.println("MATCH COUNT (N/D)");
        System.out.println("diff");
        System.out.println("min " + getMin(ndMatchCountDif));
        System.out.println("max " + getMax(ndMatchCountDif));
        System.out.println("mean " + getMean(ndMatchCountDif));
        System.out.println("coef");
        System.out.println("min " + getMin(ndMatchCountCoef));
        System.out.println("max " + getMax(ndMatchCountCoef));
        System.out.println("mean " + getMean(ndMatchCountCoef));

    }

    public static int getMatchCount(String term1, String term2) throws IOException {
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        queryBuilder.add(new TermQuery(new Term("term", term1)), BooleanClause.Occur.MUST);
        queryBuilder.add(new TermQuery(new Term("term", term2)), BooleanClause.Occur.MUST);

        BooleanQuery query = queryBuilder.build();

        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("src/main/resources/NGramsIndex")));
        IndexSearcher searcher = new IndexSearcher(reader);
        ScoreDoc[] results = searcher.search(query, 10).scoreDocs;

        int matchCount = 0;
        for (ScoreDoc result : results) {
            StoredField matchField = (StoredField) searcher.doc(result.doc).getField("matchCount");
            matchCount += (int) matchField.numericValue();
        }
        return matchCount;
    }

    public static int getVolumeCount(String term1, String term2) throws IOException {
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        queryBuilder.add(new TermQuery(new Term("term", term1)), BooleanClause.Occur.MUST);
        queryBuilder.add(new TermQuery(new Term("term", term2)), BooleanClause.Occur.MUST);

        BooleanQuery query = queryBuilder.build();

        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("src/main/resources/NGramsIndex")));
        IndexSearcher searcher = new IndexSearcher(reader);
        ScoreDoc[] results = searcher.search(query, 10).scoreDocs;

        int volumeCount = 0;
        for (ScoreDoc result : results) {
            StoredField matchField = (StoredField) searcher.doc(result.doc).getField("volumeCount");
            volumeCount += (int) matchField.numericValue();
        }
        return volumeCount;
    }

    private static <T extends Number> double getMean(ArrayList<T> list) {
        double sum = 0;
        for (T number : list) {
            sum += number.doubleValue();
        }
        return sum / list.size();
    }

    private static <T extends Comparable<? super T>> T getMin(ArrayList<T> list) {
        T min = list.get(0);
        for (T number : list) {
            if (number.compareTo(min) < 0) {
                min = number;
            }
        }
        return min;
    }

    private static <T extends Comparable<? super T>> T getMax(ArrayList<T> list) {
        T max = list.get(0);
        for (T number : list) {
            if (number.compareTo(max) > 0) {
                max = number;
            }
        }
        return max;
    }

}
