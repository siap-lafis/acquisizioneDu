package it.almaviva.siap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

import it.almaviva.siap.wsclient.titoli.CSVReader;

@Component
public class WsClientRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(WsClientRunner.class);
	
    public void run(String... args) {
        // Do something...
    	it.almaviva.siap.wsclient.titoli.CSVReader reader = new CSVReader();
    	try {
    		reader.inviaMovimentiTitoli("bz");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		};
    }
}
