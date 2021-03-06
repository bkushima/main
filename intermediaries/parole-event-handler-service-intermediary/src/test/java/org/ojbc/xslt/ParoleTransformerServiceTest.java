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
package org.ojbc.xslt;

//import static org.hamcrest.Matchers.is;
//import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ParoleTransformerServiceTest {

	XsltTransformerService unit;

	@Before
	public void setup() {
		unit = new XsltTransformerService();
	}
	
	@After
	public void tearDown() {
		unit = null;
	}	

	@Test
	public void paroleSubscribeTransform() throws Exception{
		String xml = FileUtils.readFileToString(new File( "src/test/resources/xmlInstances/parole/paroleCaseInitiation.xml"));
		String xslt = FileUtils.readFileToString(new File("src/main/resources/xslt/paroleDocumentToSubscription.xsl"));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("topicExpression","topics:person/arrest");
		
		transformAndValidate(xslt, xml,"output/subscription.xml", params);
	}
	
	@Test
	public void paroleUnsubscribeTransform() throws Exception{
		String xml = FileUtils.readFileToString(new File( "src/test/resources/xmlInstances/parole/paroleCaseTermination.xml"));
		String xslt = FileUtils.readFileToString(new File("src/main/resources/xslt/paroleDocumentToUnsubscription.xsl"));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("topicExpression","topics:person/arrest");
		
		transformAndValidate(xslt, xml,"output/unsubscription.xml", params);
	}

	@SuppressWarnings("unchecked")
	private void transformAndValidate(String xslPath, String inputXmlPath, String expectedHtmlPath, Map<String,Object> params) throws Exception {
		String expectedXml = FileUtils.readFileToString(new File("src/test/resources/xmlInstances/"+expectedHtmlPath));
		
		String convertResult = unit.transform(createSource(inputXmlPath), createSource(xslPath), params);
		
		System.out.println("Converted result: " + convertResult);
		
		Diff diff = new Diff(expectedXml, convertResult);
		assertTrue("XML should be identical", diff.identical());
	}

	private SAXSource createSource(String xml) {
		InputSource inputSource = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		inputSource.setEncoding(org.apache.commons.lang.CharEncoding.UTF_8);
		return new SAXSource(inputSource);
	}
	
}
