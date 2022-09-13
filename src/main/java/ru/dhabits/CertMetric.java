package ru.dhabits;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@Service
public class CertMetric {

    CertMetric(MeterRegistry registry, CertInfoContainer certInfoContainer) {
        certInfoContainer.getCertsInfo()
                .forEach(certInfo -> Gauge
                        .builder("certificates.ttl.days", certInfo.getDate(),
                                it -> (double) (it.getTime() - new Date().getTime()) / 1000 / 60 / 60 / 24)
                        .tag("issuerCN", certInfo.getIssuerCN())
                        .tag("subjectCN", certInfo.getSubjectCN())
                        .tag("date", new SimpleDateFormat("yyyy-MM-dd").format(certInfo.getDate()))
                        .register(registry)
                );
    }

}

