package ru.dhabits;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@Service
public class CertInfoContainer {
    private static final Logger log = Logger.getLogger(CertInfoContainer.class.getName());
    private final ResourceLoader resourceLoader;
    private final List<CertInfo> certsInfo;
    private final Pattern cnRegex = Pattern.compile("CN=(.*?)(,|$)");

    CertInfoContainer(Environment env, ResourceLoader resourceLoader) throws Exception {
        this.resourceLoader = resourceLoader;

        var javaHome = System.getProperty("java.home");
        var defaultStorePath = Optional.ofNullable(System.getProperty("javax.net.ssl.trustStore"))
                .orElse(javaHome + "/lib/security/cacerts");
        var defaultStorePass = Optional.ofNullable(System.getProperty("javax.net.ssl.trustStorePassword"))
                .orElse("changeit");

        certsInfo = certExpirationFromStore("file:" + defaultStorePath, defaultStorePass);
    }
    private List<CertInfo> certExpirationFromStore(String storePath, String storePass) throws Exception {
        if (storePath == null || storePass == null) return emptyList();
        var store = resourceLoader.getResource(storePath);
        if (!store.exists()) {
            log.warning("Cert store path " + storePath + " was found, but doesn't exists");
            return emptyList();
        }

        var keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(store.getInputStream(), storePass.toCharArray());

        ArrayList<String> list = Collections.list(keystore.aliases());
        return list.stream()
            .flatMap(alias -> {
                        try {
                            var certChain = keystore.getCertificateChain(alias);
                            if (certChain != null) return Stream.of(certChain);
                            return Stream.of(keystore.getCertificate(alias));
                        } catch (KeyStoreException e) {
                            throw new RuntimeException(e);
                        }
            }).filter(Objects::nonNull)

            .map( cert -> {
            if (!"X.509".equals(cert.getType())) return null;

            var x509Certificate = (X509Certificate) cert;
            return new CertInfo(
                    getCn(x509Certificate.getIssuerX500Principal().getName()),
                    getCn(x509Certificate.getSubjectX500Principal().getName()),
                    x509Certificate.getNotAfter()
            );
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String getCn(String principal) {
        return cnRegex.matcher(principal).results().findFirst().map(it -> it.group(1)).orElse(principal);
    }

    public List<CertInfo> getCertsInfo() {
        return certsInfo;
    }
}
