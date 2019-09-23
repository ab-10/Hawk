package cliDemo;

import analysis.DictionaryClassifiers;
import org.apache.commons.cli.*;

public class CliDemo {
    private static final String SCRIPT_NAME = "EDAM Demo";
    public static void main(String args[]){
        Options cliOptions = new Options();

        Option tripleOption = new Option("t", "triple", true,
                "Discriminative attribute triple, that should be evaluated.\n" +
                        "Given in the form: pivot,comparison,feature");
        tripleOption.setRequired(true);

        Option indexLocationOption = new Option("i", "indexLocation", true,
                "Path to parent folder containing ConceptNet, VGAttributes, VGRelationships, and WN index folders.");
        indexLocationOption.setRequired(true);

        cliOptions.addOption(tripleOption);
        cliOptions.addOption(indexLocationOption);

        cliOptions.addOption(indexLocationOption);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e){
            System.err.println(e.getMessage());
            helpFormatter.printHelp(SCRIPT_NAME, cliOptions);

            return;
        }

        String triple = cmd.getOptionValue("triple");
        String indexParentFolder = cmd.getOptionValue("indexLocation");

        if(indexParentFolder.charAt(indexParentFolder.length() -1) != '/'){
            indexParentFolder += "/";
        }

        System.out.println("Query: " + triple);

        String[] tripleArr = triple.split(",");

        if(tripleArr.length != 3){
            System.err.println("Unexpected triple argument\n" +
                    "Expecting a triple of form: pivot,feature,comparison.");
        }

        int wnVote = DictionaryClassifiers.wordNetVote(tripleArr[0], tripleArr[1], tripleArr[2],
                indexParentFolder + "LabeledWNWithHypSupertype", true);
        System.out.println("Received vote from WordNet graph: " + wnVote);

        int vgVote = DictionaryClassifiers.visualGenomeVote(tripleArr[0], tripleArr[1], tripleArr[2],
                indexParentFolder + "explainableVG", true);
        System.out.printf("Received vote from VG: %d\n", vgVote);

    }
}
