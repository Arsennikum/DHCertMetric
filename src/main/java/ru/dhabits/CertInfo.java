package ru.dhabits;

import java.util.Date;

public class CertInfo {
    private final String issuerCN;
    private final String subjectCN;
    private final Date date;

    public CertInfo(String issuerCN, String subjectCN, Date date) {
        this.issuerCN = issuerCN;
        this.subjectCN = subjectCN;
        this.date = date;
    }

    public String getIssuerCN() {
        return issuerCN;
    }

    public String getSubjectCN() {
        return subjectCN;
    }

    public Date getDate() {
        return date;
    }
}
