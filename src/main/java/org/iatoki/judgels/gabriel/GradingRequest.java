package org.iatoki.judgels.gabriel;

public interface GradingRequest {
    String getSubmissionJid();

    String getProblemJid();

    long getGradingLastUpdateTime();

    String getGradingType();

    GradingSource getGradingSource();
}
