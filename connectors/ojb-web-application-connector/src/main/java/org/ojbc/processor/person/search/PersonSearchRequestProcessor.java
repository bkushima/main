package org.ojbc.processor.person.search;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ojbc.processor.RequestResponseProcessor;
import org.ojbc.util.camel.processor.MessageProcessor;
import org.ojbc.util.camel.security.saml.OJBSamlMap;
import org.ojbc.util.camel.security.saml.SAMLTokenUtils;
import org.ojbc.util.fedquery.error.MergeNotificationErrorProcessor;
import org.ojbc.web.PersonSearchInterface;
import org.ojbc.web.model.person.search.PersonSearchRequest;
import org.ojbc.web.util.RequestMessageBuilderUtilities;
import org.opensaml.xml.signature.SignatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PersonSearchRequestProcessor extends RequestResponseProcessor implements CamelContextAware, PersonSearchInterface {

	/**
	 * Camel context needed to use producer template to send messages
	 */
	protected CamelContext camelContext;
	
	private MessageProcessor personSearchMessageProcessor;
	
	private OJBSamlMap OJBSamlMap;
	
	private static final Log log = LogFactory.getLog( PersonSearchRequestProcessor.class );
	
	private boolean allowQueriesWithoutSAMLToken;
	
	public String invokePersonSearchRequest(PersonSearchRequest personSearchRequest, String federatedQueryID, Element samlToken) throws Exception
	{
		String response = "";
		try
		{
			if (allowQueriesWithoutSAMLToken)
			{	
				if (samlToken == null)
				{
					//Add SAML token to request call
					samlToken = SAMLTokenUtils.createStaticAssertionAsElement("https://idp.ojbc-local.org:9443/idp/shibboleth", SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, true, true, null);
		
				}	
			}	
			
			if (samlToken == null)
			{
				throw new Exception("No SAML token provided. Unable to perform query.");
			}	
				
			//POJO to XML Request
			Document personSearchRequestPayload = RequestMessageBuilderUtilities.createPersonSearchRequest(personSearchRequest);
			personSearchRequestPayload.normalizeDocument();
			
			//Create exchange
			Exchange senderExchange = new DefaultExchange(camelContext, ExchangePattern.InOnly);
			
			//Set exchange body to XML Request message
			senderExchange.getIn().setBody(personSearchRequestPayload);
			
			//Set reply to and WS-Addressing message ID
			senderExchange.getIn().setHeader("federatedQueryRequestGUID", federatedQueryID);
			senderExchange.getIn().setHeader("WSAddressingReplyTo", this.getReplyToAddress());
			
			//Set the token header so that CXF can retrieve this on the outbound call
			String tokenID = senderExchange.getExchangeId();
			senderExchange.getIn().setHeader("tokenID", tokenID);
	
			OJBSamlMap.putToken(tokenID, samlToken);
	
			personSearchMessageProcessor.sendResponseMessage(camelContext, senderExchange);
				
			//Put message ID and "noResponse" place holder.  
			putRequestInMap(federatedQueryID);
			
			response = pollMap(federatedQueryID);
			
			if (response.equals(NO_RESPONSE)) {
				log.debug("Endpoints timed out and no response recieved at web app, create error response");
				response = MergeNotificationErrorProcessor.returnMergeNotificationErrorMessage();
			}
			
			if (response.length() > 500)
			{	
				log.debug("Here is the response (truncated): " + response.substring(0,500));
			}
			else
			{
				log.debug("Here is the response: " + response);
			}
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw(ex);
		}
		
		//return response here
		return response;
		
	}

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;

	}

	public MessageProcessor getPersonSearchMessageProcessor() {
		return personSearchMessageProcessor;
	}

	public void setPersonSearchMessageProcessor(
			MessageProcessor personSearchMessageProcessor) {
		this.personSearchMessageProcessor = personSearchMessageProcessor;
	}
	
	public OJBSamlMap getOJBSamlMap() {
		return OJBSamlMap;
	}


	public void setOJBSamlMap(OJBSamlMap oJBSamlMap) {
		OJBSamlMap = oJBSamlMap;
	}

	public boolean isAllowQueriesWithoutSAMLToken() {
		return allowQueriesWithoutSAMLToken;
	}

	public void setAllowQueriesWithoutSAMLToken(boolean allowQueriesWithoutSAMLToken) {
		this.allowQueriesWithoutSAMLToken = allowQueriesWithoutSAMLToken;
	}
	

}