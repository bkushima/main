/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2015 Open Justice Broker Consortium
 */
package org.apache.commons.httpclient.contrib.ssl;

import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.commons.ssl.TrustMaterial;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;


/**
 * YC: Added this class and modified line 195 so it wouldn't try to read private key from Truststore.
 * http://old.nabble.com/No-private-keys-found-in-keystore-td18165598.html
 * This class requires the 'Not Yet Commons library' maven dependency
 * 
 * <p/>
 * AuthSSLProtocolSocketFactory can be used to validate the identity of the HTTPS
 * server against a list of trusted certificates and to authenticate to the HTTPS
 * server using a private key.
 * </p>
 * <p/>
 * <p/>
 * AuthSSLProtocolSocketFactory will enable server authentication when supplied with
 * a {@link java.security.KeyStore truststore} file containg one or several trusted certificates.
 * The client secure socket will reject the connection during the SSL session handshake
 * if the target HTTPS server attempts to authenticate itself with a non-trusted
 * certificate.
 * </p>
 * <p/>
 * <p/>
 * Use JDK keytool utility to import a trusted certificate and generate a truststore file:
 * <pre>
 *     keytool -import -alias "my server cert" -file server.crt -keystore my.truststore
 *    </pre>
 * </p>
 * <p/>
 * <p/>
 * AuthSSLProtocolSocketFactory will enable client authentication when supplied with
 * a {@link java.security.KeyStore keystore} file containg a private key/public certificate pair.
 * The client secure socket will use the private key to authenticate itself to the target
 * HTTPS server during the SSL session handshake if requested to do so by the server.
 * The target HTTPS server will in its turn verify the certificate presented by the client
 * in order to establish client's authenticity
 * </p>
 * <p/>
 * <p/>
 * Use the following sequence of actions to generate a keystore file
 * </p>
 * <ul>
 * <li>
 * <p/>
 * Use JDK keytool utility to generate a new key
 * <pre>keytool -genkey -v -alias "my client key" -validity 365 -keystore my.keystore</pre>
 * For simplicity use the same password for the key as that of the keystore
 * </p>
 * </li>
 * <li>
 * <p/>
 * Issue a certificate signing request (CSR)
 * <pre>keytool -certreq -alias "my client key" -file mycertreq.csr -keystore my.keystore</pre>
 * </p>
 * </li>
 * <li>
 * <p/>
 * Send the certificate request to the trusted Certificate Authority for signature.
 * One may choose to act as her own CA and sign the certificate request using a PKI
 * tool, such as OpenSSL.
 * </p>
 * </li>
 * <li>
 * <p/>
 * Import the trusted CA root certificate
 * <pre>keytool -import -alias "my trusted ca" -file caroot.crt -keystore my.keystore</pre>
 * </p>
 * </li>
 * <li>
 * <p/>
 * Import the PKCS#7 file containg the complete certificate chain
 * <pre>keytool -import -alias "my client key" -file mycert.p7 -keystore my.keystore</pre>
 * </p>
 * </li>
 * <li>
 * <p/>
 * Verify the content the resultant keystore file
 * <pre>keytool -list -v -keystore my.keystore</pre>
 * </p>
 * </li>
 * </ul>
 * <p/>
 * Example of using custom protocol socket factory for a specific host:
 * <pre>
 *     Protocol authhttps = new Protocol("https",
 *          new AuthSSLProtocolSocketFactory(
 *              new URL("file:my.keystore"), "mypassword",
 *              new URL("file:my.truststore"), "mypassword"), 443);
 * <p/>
 *     HttpClient client = new HttpClient();
 *     client.getHostConfiguration().setHost("localhost", 443, authhttps);
 *     // use relative url only
 *     GetMethod httpget = new GetMethod("/");
 *     client.executeMethod(httpget);
 *     </pre>
 * </p>
 * <p/>
 * Example of using custom protocol socket factory per default instead of the standard one:
 * <pre>
 *     Protocol authhttps = new Protocol("https",
 *          new AuthSSLProtocolSocketFactory(
 *              new URL("file:my.keystore"), "mypassword",
 *              new URL("file:my.truststore"), "mypassword"), 443);
 *     Protocol.registerProtocol("https", authhttps);
 * <p/>
 *     HttpClient client = new HttpClient();
 *     GetMethod httpget = new GetMethod("https://localhost/");
 *     client.executeMethod(httpget);
 *     </pre>
 * </p>
 *
 * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski</a>
 *         <p/>
 *         <p/>
 *         DISCLAIMER: HttpClient developers DO NOT actively support this component.
 *         The component is provided as a reference material, which may be inappropriate
 *         for use without additional customization.
 *         </p>
 */

public class AuthSSLProtocolSocketFactory extends HttpSecureProtocol {

    /**
     * Constructor for AuthSSLProtocolSocketFactory. Either a keystore or truststore file
     * must be given. Otherwise SSL context initialization error will result.
     *
     * @param keystoreUrl        URL of the keystore file. May be <tt>null</tt> if HTTPS client
     *                           authentication is not to be used.
     * @param keystorePassword   Password to unlock the keystore. IMPORTANT: this implementation
     *                           assumes that the same password is used to protect the key and the keystore itself.
     * @param truststoreUrl      URL of the truststore file. May be <tt>null</tt> if HTTPS server
     *                           authentication is not to be used.
     * @param truststorePassword Password to unlock the truststore.
     */
    public AuthSSLProtocolSocketFactory(final URL keystoreUrl,
                                        final String keystorePassword,
                                        final URL truststoreUrl,
                                        final String truststorePassword)
        throws GeneralSecurityException, IOException {

        super();

        // prepare key material
        if (keystoreUrl != null) {
            char[] ksPass = null;
            if (keystorePassword != null) {
                ksPass = keystorePassword.toCharArray();
            }
            KeyMaterial km = new KeyMaterial(keystoreUrl, ksPass);
            super.setKeyMaterial(km);
        }

        // prepare trust material1
        if (truststoreUrl != null) {
            char[] tsPass = null;
            if (truststorePassword != null) {
                tsPass = truststorePassword.toCharArray();
            }
            
            //YC: CHANGED THIS LINE BELOW TO LOAD TRUSTMATERIAL RATHER THAN KEYMATERIAL
            TrustMaterial tm = new TrustMaterial(truststoreUrl, tsPass);
            super.setTrustMaterial(tm);
        }
    }

}