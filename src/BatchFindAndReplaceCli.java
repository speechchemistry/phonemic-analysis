package io.github.speechchemistry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.print.attribute.IntegerSyntax;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "BatchFindAndReplaceCli", version = "BatchFindAndReplaceCli 0.1", mixinStandardHelpOptions = true)
public class BatchFindAndReplaceCli implements Callable {
    @Parameters(paramLabel = "textFile", description = "Input text file")
    String textFile;
    @Parameters(paramLabel = "replaceTsvFile", description = "mapping file (in tab separated value format)")
    String replaceTsvFile;

    @Override
    public Integer call() throws IOException{
        BatchFindAndReplace p = new BatchFindAndReplace(replaceTsvFile);
        BufferedReader textReader = new BufferedReader(new FileReader(textFile));
        String thisLine = "";
        while ((thisLine = textReader.readLine()) != null) {
            System.out.println(p.processString(thisLine));
        }
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BatchFindAndReplaceCli()).execute(args); 
        System.exit(exitCode); 
    }
}