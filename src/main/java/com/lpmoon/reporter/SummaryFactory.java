package com.lpmoon.reporter;

/**
 * Created by zblacker on 2017/6/29.
 */
public class SummaryFactory {

    public static final String CODAHALE = "Codahale";
    public static final String SIMPLE = "Simple";


    public static Summary getSummary(String summaryName) {
        Summary summary;
        if (CODAHALE.equals(summaryName)) {
            summary = CodahaleSummary.getInstance();
        } else {
            summary = SimpleSummary.getInstance();
        }

        if (!summary.isStarted()) {
            summary.start();
        }

        return summary;
    }
}
