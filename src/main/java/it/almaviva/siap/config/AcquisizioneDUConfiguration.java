package it.almaviva.siap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import it.almaviva.siap.wsclient.titoli.*;

@Configuration
public class AcquisizioneDUConfiguration {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("it.sian.wsdl");
		return marshaller;
	}

	@Bean
	public TitoliClient acquisizioneDUClient(Jaxb2Marshaller marshaller) {
		TitoliClient client = new TitoliClient();
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
}