package analysis;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;

/***
 * Methods for extracting common or differing properties between two terms.
 */
public class PropertyExtractors {
    public static void main(String args[]) throws IOException {
        String[] terms = {"plane", "train", "jaguar", "drink"};
        ArrayList<ArrayList<Property>> propertyFrame = new ArrayList<>(3 * (terms.length + 1));
        ArrayList<String> headers = new ArrayList<>(11 * (terms.length + 1));

        for (int i = 0; i < 4 * (terms.length + 1); i++) {
            propertyFrame.add(new ArrayList<>());
        }

        // Set headers
        for (String term : terms) {
            headers.add(term + "&" + "car");
            headers.add(term + " count");
            headers.add("Source 1");
            headers.add("car" + " count");
            headers.add("Source 2");
            headers.add(term + "\\" + "car");
            headers.add(term + " count");
            headers.add("Source");
            headers.add("car" + "\\" + term);
            headers.add("car" + " count");
            headers.add("Source");
        }
        headers.add("jaguar" + "&" + "cat");
        headers.add("jaguar" + " count");
        headers.add("Source 1");
        headers.add("cat" + " count");
        headers.add("Source 2");
        headers.add("jaguar" + "\\" + "cat");
        headers.add("jaguar" + " count");
        headers.add("Source");
        headers.add("cat" + "\\" + "jaguar");
        headers.add("cat" + " count");
        headers.add("Source");


        for (int i = 0; i < terms.length; i ++) {
            ArrayList<ArrayList<Property>> commonProperties = commonProperties(terms[i], "car");
            propertyFrame.set(4*i, commonProperties.get(0));
            propertyFrame.set(4*i + 1, commonProperties.get(1));
            propertyFrame.set(4*i + 2, complimentProperties(terms[i], "car"));
            propertyFrame.set(4*i + 3, complimentProperties("car", terms[i]));
        }
        ArrayList<ArrayList<Property>> commonProperties = commonProperties("jaguar", "cat");
        propertyFrame.set(terms.length * 4, commonProperties.get(0));
        propertyFrame.set(terms.length * 4 + 1, commonProperties.get(1));
        propertyFrame.set(terms.length * 4 + 2, complimentProperties("jaguar", "cat"));
        propertyFrame.set(terms.length * 4 + 3, complimentProperties("cat", "jaguar"));

        PrintWriter writer = new PrintWriter("properties.csv");
        for (int i = 0; i < headers.size(); i++) {
            writer.print(headers.get(i) + ",");
        }
        writer.println();

        ArrayList<Iterator<Property>> iterators = new ArrayList<>(propertyFrame.size());
        for (int i = 0; i < propertyFrame.size(); i++) {
            iterators.add(propertyFrame.get(i).iterator());
        }

        while (oneHasNext(iterators)) {
            for (int i = 0; i <= terms.length * 4; i +=4) {
                if (iterators.get(i).hasNext()) {
                    Property currentProperty = iterators.get(i).next();
                    writer.print(currentProperty.name + ",");
                    writer.print(currentProperty.count + ",");
                    String sources = "";
                    for (String source : currentProperty.sources) {
                        sources += " "  + source;
                    }
                    writer.print(sources + ",");
                } else {
                    writer.print(",,,");
                }

                if (iterators.get(i + 1).hasNext()) {
                    Property currentProperty = iterators.get(i + 1).next();
                    writer.print(currentProperty.count + ",");
                    String sources = "";
                    for (String source : currentProperty.sources) {
                        sources += " " + source;
                    }
                    writer.print(sources + ",");
                } else {
                    writer.print(",,");
                }

                if (iterators.get(i + 2).hasNext()) {
                    Property currentProperty = iterators.get(i + 2).next();
                    writer.print(currentProperty.name + ",");
                    writer.print(currentProperty.count + ",");
                    String sources = "";
                    for (String source : currentProperty.sources) {
                        sources += " " + source;
                    }
                    writer.print(sources + ",");
                } else {
                    writer.print(",,,");
                }

                if (iterators.get(i + 3).hasNext()) {
                    Property currentProperty = iterators.get(i + 3).next();
                    writer.print(currentProperty.name + ",");
                    writer.print(currentProperty.count + ",");
                    String sources = "";
                    for (String source : currentProperty.sources) {
                        sources += " " + source;
                    }
                    writer.print(sources + ",");
                } else {
                    writer.print(",,,");
                }
            }
            writer.println();
        }
        writer.close();


    }

    /** Obtains the properties shared by first and second term
     * The method returns two ArrayLists of properties with identical names,
     * but varying counts and sources.
     *
     * @param first term to obtain the properties of.
     * @param second term to obtain the properties of.
     * @return two array lists of properties,
     * each containing the properties shared by the first and the second term
     * @throws IOException
     */
    public static ArrayList<ArrayList<Property>> commonProperties(String first, String second) throws IOException {
        ArrayList<Property> firstProperties = getProperties(first);
        ArrayList<Property> secondProperties = getProperties(second);
        firstProperties.retainAll(secondProperties);
        secondProperties.retainAll(firstProperties);
        ArrayList<ArrayList<Property>> result = new ArrayList<>(2);
        result.add(firstProperties);
        result.add(secondProperties);

        return result;
    }

    public static ArrayList<Property> complimentProperties(String first, String second) throws IOException {
        ArrayList<Property> compliment = getProperties(first);
        compliment.removeAll(getProperties(second));
        return compliment;
    }

    /**
     * returns true if at least one iterator in iterators has next element
     * false otherwise
     *
     * @param iterators to check
     * @return if at least one iterator has next
     */
    private static boolean oneHasNext(ArrayList<Iterator<Property>> iterators) {
        for (Iterator iterator : iterators) {
            if (iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<Property> getProperties(String term) throws IOException {
        ArrayList<Property> properties = new ArrayList<>();
        TermQuery query = new TermQuery(new Term("property", term));

        String[] sources = {"newWNWithHyp", "WKT", "VGAttributes", "VGRelationships", "wikipedia"};

        for (String source : sources) {
            DirectoryReader reader;
            IndexSearcher searcher;
            try {
                reader = DirectoryReader.open(FSDirectory.open(Paths.get("src/main/resources/" + source)));
                searcher = new IndexSearcher(reader);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Invalid WordNet Index directory specified.");
            }

            ScoreDoc[] results;
            results = searcher.search(query, 10000).scoreDocs;

            for (ScoreDoc result : results) {
                try {
                    String[] names = searcher.doc(result.doc).getValues("property");
                    for (String name : names) {
                        Property currentProperty = new Property(name, source);
                        if (properties.contains(currentProperty)) {
                            int index = properties.indexOf(currentProperty);
                            properties.get(index).mergeWith(currentProperty);
                        } else {
                            properties.add(currentProperty);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return properties;
    }

    private static class Property {
        private final String name;
        private int count;
        private HashSet<String> sources;

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public HashSet<String> getSources() {
            return sources;
        }

        public Property(String name, String source) {
            this.name = name.replaceAll("\"", "");
            sources = new HashSet<>();
            sources.add(source);
            count = 1;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }

        public void mergeWith(Property propertyToMerge) {
            this.count += propertyToMerge.getCount();
            this.sources.addAll(propertyToMerge.getSources());
        }
    }
}
