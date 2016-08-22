package it.almaviva.siap.wsclient.titoli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import org.springframework.xml.transform.StringSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import javax.xml.bind.JAXBElement;

import it.sian.wsdl.ISWSResponse;
import it.sian.wsdl.InputMovimentazioneTitoli;

public class TitoliClient  extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(TitoliClient.class);
	
	private final String SCHEMA = "http://cooperazione.sian.it/schema/SoapAutenticazione";
	
	public ISWSResponse inviaMovimentiTitoli(InputMovimentazioneTitoli request) {

		//MovimentazioneTitoliPAC2015
		ISWSResponse response = (ISWSResponse) getWebServiceTemplate()
				.marshalSendAndReceive(request, new WebServiceMessageCallback() {

			        public void doWithMessage(WebServiceMessage message) {
			            try {
			                SoapMessage soapMessage = (SoapMessage) message;
			                SoapHeader header = soapMessage.getSoapHeader();
			                //...
			                StringSource headerSource = new StringSource("<SOAPAutenticazione xmlns=\"http://cooperazione.sian.it/schema/SoapAutenticazione\">\n" +
			                        "<nomeServizio>MovimentazioneTitoliPAC2015</nomeServizio>\n" +
//			                        "<username>prov.aut.tre</username>\n" +
//			                        "<password>1reliancewx</password>\n" +
								"<username>pro.aut.bo</username>\n" +
								"<password>1pabcoop.01</password>\n" +
			                        "</SOAPAutenticazione>");
			                //... 
			                Transformer transformer = TransformerFactory.newInstance().newTransformer();
			                transformer.transform(headerSource, header.getResult());
			                
			            } catch (Exception e) {
			                // exception handling
			            	log.warn("Errore nell'impostazione dell'header");
			            	e.printStackTrace();
			            }
			        }
			    });

		return response;
	}
}
