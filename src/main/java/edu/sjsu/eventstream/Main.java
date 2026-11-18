package edu.sjsu.eventstream;

/** Entry point. All behavior lives in AnalyzerApplication. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = new AnalyzerApplication().run(args, System.out, System.err);
        System.exit(exitCode);
    }
}
