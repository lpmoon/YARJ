package com.lpmoon.agent.reporter;

public class SummaryFactory {

    public static final String CODAHALE = "Codahale";
    public static final String SIMPLE = "Simple";


    public static Summary getSummary(String summaryName) {
        Summary summary = null;
        if (CODAHALE.equals(summaryName)) {
            summary = CodahaleSummary.getInstance();
        }

        if (!summary.isStarted()) {
            summary.start();
        }

        return summary;
    }
}
