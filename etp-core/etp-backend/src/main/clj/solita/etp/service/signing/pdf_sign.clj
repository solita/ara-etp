(ns solita.etp.service.signing.pdf-sign
  (:require
    [solita.etp.service.sign :as sign-service]
    [solita.etp.config :as config])
  (:import (eu.europa.esig.dss.enumerations CommitmentType SignatureLevel DigestAlgorithm SignatureAlgorithm SignaturePackaging)
           (eu.europa.esig.dss.model BLevelParameters DSSDocument DSSMessageDigest Digest FileDocument ToBeSigned SignatureValue)
           (eu.europa.esig.dss.cades.signature CMSSignedDocument)
           (eu.europa.esig.dss.model.x509 CertificateToken)
           (eu.europa.esig.dss.pades PAdESSignatureParameters PAdESUtils SignatureFieldParameters SignatureImageParameters SignatureImageTextParameters)
           (eu.europa.esig.dss.pades.signature ExternalCMSService PAdESService PAdESWithExternalCMSService)
           (eu.europa.esig.dss.pdf.pdfbox PdfBoxNativeObjectFactory)
           (eu.europa.esig.dss.service.ocsp OnlineOCSPSource)
           (eu.europa.esig.dss.spi DSSMessageDigestCalculator DSSUtils)
           (eu.europa.esig.dss.spi.validation CertificateVerifier CommonCertificateVerifier)
           (java.util ArrayList Collection Date List)))

(defn sign-pdf [aws-kms-client unsigned-document]
  (let [^PAdESWithExternalCMSService service (PAdESWithExternalCMSService.)
        ;;TODO: Card vs system
        _ (println "isPDF: " (PAdESUtils/isPDFDocument unsigned-document))
        ^CertificateToken signing-cert-token (-> config/system-signature-certificate-leaf
                                                 (DSSUtils/convertToDER)
                                                 (DSSUtils/loadCertificate))
        ^List cert-chain (ArrayList. ^Collection (->> [config/system-signature-certificate-leaf
                                                       config/system-signature-certificate-intermediate
                                                       config/system-signature-certificate-root]
                                                      (mapv #(-> %
                                                                 (DSSUtils/convertToDER)
                                                                 (DSSUtils/loadCertificate)))))
        ^PAdESSignatureParameters signature-parameters (doto (PAdESSignatureParameters.)
                                                         #_(-> % (.bLevel) (.setSigningDate (Date.)))
                                                         (.setSignatureLevel SignatureLevel/PAdES_BASELINE_B)
                                                         (.setCertificateChain cert-chain)
                                                         #_(.setReason "DSS testing") ;; This is seen in the signature.
                                                         (.setSigningCertificate signing-cert-token)
                                                         #_(.setSignaturePackaging SignaturePackaging/ENVELOPING)
                                                         (.setDigestAlgorithm DigestAlgorithm/SHA256))

        ^DSSMessageDigest message-digest (.getMessageDigest service unsigned-document signature-parameters)
        ;;certificate-verifier
        #_(doto (CommonCertificateVerifier.)
            (.setOcspSource (OnlineOCSPSource.)))

        _ (println "DIGEST: " (.toString message-digest))

        ;;;;;;;;;;;;;;;;;;;;;;;;;;;
        certificate-verifier (CommonCertificateVerifier.)

        ^ExternalCMSService padesCMSGeneratorService (ExternalCMSService. certificate-verifier)

        ^ToBeSigned data-to-sign (-> padesCMSGeneratorService (.getDataToSign message-digest signature-parameters))


        _ (doto signature-parameters
            (.setSignedData (.getBytes data-to-sign)))

        ^DSSMessageDigestCalculator dig-calc (doto (DSSMessageDigestCalculator. DigestAlgorithm/SHA256)
                                               (.update (.getBytes data-to-sign))
                                               )

        ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 (.readAllBytes (sign-service/sign aws-kms-client (.getValue (.getMessageDigest dig-calc)))))


        ^CMSSignedDocument cms-signature (-> padesCMSGeneratorService (.signMessageDigest message-digest signature-parameters signature-value))
        ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

        ^DSSDocument signedDocument (.signDocument service unsigned-document signature-parameters cms-signature)]


    (assert (not (nil? message-digest)))
    (assert (not (nil? cms-signature)))
    (assert (.isValidCMSSignedData service message-digest cms-signature))
    (assert (.isValidPAdESBaselineCMSSignedData service message-digest cms-signature))
    signedDocument))

(defn wtf [aws-kms-client pdf-document]
  (let [^PAdESService service (doto (PAdESService. (CommonCertificateVerifier.))
                                (.setPdfObjFactory (PdfBoxNativeObjectFactory.)))

        ^CertificateToken signing-cert-token (-> config/system-signature-certificate-leaf
                                                 (DSSUtils/convertToDER)
                                                 (DSSUtils/loadCertificate))
        ^List cert-chain (ArrayList. ^Collection (->> [config/system-signature-certificate-leaf
                                                       config/system-signature-certificate-intermediate
                                                       config/system-signature-certificate-root]
                                                      (mapv #(-> %
                                                                 (DSSUtils/convertToDER)
                                                                 (DSSUtils/loadCertificate)))))
        ^BLevelParameters b-level-params (doto (BLevelParameters.)
                                           (.setSigningDate (Date.))
                                           (.setClaimedSignerRoles (ArrayList. ["Laatija"]))
                                           (.setSignedAssertions (ArrayList. ["SignedAssertion?"]))
                                           #_(.setCommitmentTypeIndications (ArrayList. ))
                                           )

        ^SignatureImageTextParameters txt-params (doto (SignatureImageTextParameters.)
                                            (.setText "TTTT LAAAA \n 12:12:12 12.12.12")

                                            )

        ^SignatureFieldParameters sig-field-params (doto (SignatureFieldParameters.)
                                                     (.setPage 1)
                                                     (.setOriginX 10)
                                                     (.setOriginY 10)
                                                     (.setWidth 100)
                                                     (.setHeight 125)


                                                     )

        ^SignatureImageParameters sig-img (doto (SignatureImageParameters.)
                                            (.setFieldParameters sig-field-params)
                                            (.setTextParameters txt-params)

                                            )

        ^PAdESSignatureParameters signature-parameters (doto (PAdESSignatureParameters.)
                                                         (.setBLevelParams b-level-params)
                                                         (.setSignatureLevel SignatureLevel/PAdES_BASELINE_B)
                                                         (.setCertificateChain cert-chain)
                                                         (.setReason "DSS testing") ;; This is seen in the signature.
                                                         (.setSigningCertificate signing-cert-token)
                                                         (.setSignaturePackaging SignaturePackaging/ENVELOPED)
                                                         (.setDigestAlgorithm DigestAlgorithm/SHA256)
                                                         (.setSignerName "Testaaja Laatija")
                                                         (.setIncludeVRIDictionary true)
                                                         (.setImageParameters sig-img))

        _ (println (.toString signature-parameters))

        ^ToBeSigned data-to-sign (-> service (.getDataToSign pdf-document signature-parameters))
        ^Digest digest (Digest.
                         (-> signature-parameters .getDigestAlgorithm)
                         (DSSUtils/digest (-> signature-parameters .getDigestAlgorithm)
                                          (-> data-to-sign .getBytes)))

        ^SignatureValue signature-value (SignatureValue. SignatureAlgorithm/RSA_SHA256 (.readAllBytes (sign-service/sign aws-kms-client (-> data-to-sign .getBytes))))

        ;_ (assert (-> service (.isValidSignatureValue data-to-sign signature-value signing-cert-token)))

        ^DSSDocument signed-document (-> service (.signDocument pdf-document signature-parameters signature-value))

        ]
    signed-document
    )

  )