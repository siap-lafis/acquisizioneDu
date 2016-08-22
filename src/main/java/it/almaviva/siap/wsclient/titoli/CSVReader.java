package it.almaviva.siap.wsclient.titoli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;

import it.sian.wsdl.ObjectFactory;
import it.sian.wsdl.InputMovimentazioneTitoli;
import it.sian.wsdl.CUAA;
import it.sian.wsdl.Intestatario;
import it.sian.wsdl.Cedente;
import it.sian.wsdl.ArrayOfTitoliMovimentati;
import it.sian.wsdl.TitoliMovimentati;
import it.sian.wsdl.ISWSResponse;

import it.almaviva.siap.config.AcquisizioneDUConfiguration;


public class CSVReader {

    private final Logger log = LoggerFactory.getLogger(CSVReader.class);
    
    private final String wsdlUrl = "http://cooperazione.sian.it/wspdd/services/RiformaTitoli?wsdl";
    
    private ObjectFactory objectFactory = new ObjectFactory();

    private final SimpleDateFormat sdf_in = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat sdf_out = new SimpleDateFormat("yyyyMMyy");
    
    private int cntTit = 0;

	/*
	 * Legge i file CSV restituisce l'oggetto domanda da passare in input al WS
	 */
	public void inviaMovimentiTitoli (String prov) throws Exception
	{
		int cnt = 0, numErrori = 0;
		
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AcquisizioneDUConfiguration.class);
		TitoliClient client = context.getBean(TitoliClient.class);
		client.setDefaultUri(wsdlUrl);
    	Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("it.sian.wsdl");
		
    	List<InputMovimentazioneTitoli> listaMov = this.readMovimenti(prov, "trasf-titoli-bz-02082016.csv");
    	for (InputMovimentazioneTitoli mov : listaMov) {
        	//debugging
    		try {
        		ISWSResponse response = client.inviaMovimentiTitoli(mov);
    			if (response != null && !response.getCodRet().equals("012")) {
    	    		StringResult result = new StringResult();
    	    		marshaller.marshal(mov, result);
    	    		log.debug("mov: " + result.toString());
    	    		log.info("Documento = " + mov.getIdDocumento());
    	    		log.info("Cod.ret. = " + response.getCodRet() + " - " + response.getSegnalazione());
    	    		numErrori++;
    			}
    		}
    		catch (Exception ex) {
    			ex.printStackTrace();
    			numErrori++;
			}
			cnt++;
    	}
		log.info("Movimenti inviati: " + cnt 
				+ " (num. titoli: " + cntTit
				+ ") di cui con esito negativo: " + numErrori);
	}
	
	/*
	 * Legge il file CSV relativo all'anagrafica e restituisce l'oggetto 
	 * da passare in input al WS
	 */
	private List<InputMovimentazioneTitoli> readMovimenti (String prov, String fileName) 
	{
        String csvFile = prov + "/" + fileName;
        String line = "";
        String cvsSplitBy = ";";
        DecimalFormat df = new DecimalFormat("000000000000");
        		
		int cnt = 0;
		
        List<InputMovimentazioneTitoli> lista = new ArrayList<InputMovimentazioneTitoli>();
        
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(csvFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) 
        {
            String keySup = null;
        	InputMovimentazioneTitoli mov = null;
        	ArrayOfTitoliMovimentati titoliMov = null;
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] fields = line.split(cvsSplitBy);
                StringBuilder sbk = new StringBuilder();
                //...
            	if (!fields[0].equalsIgnoreCase("INTESTATARIO") 
            			&& !fields[0].equalsIgnoreCase("CUAA acquirente")
        			) {
            		
                	for (int i = 0; i < fields.length; i++) {
                		log.debug("field[" + i + "] = " + fields[i]);
                		if (i < 7) {
                    		sbk.append(fields[i]);
                		}
                	}
                	
                	if (keySup != null && !keySup.equals(sbk.toString())) {
                    	mov.setTitoliMovimentatiInput(titoliMov);
                    	lista.add(mov);
                    	mov = objectFactory.createInputMovimentazioneTitoli();
                    	titoliMov = objectFactory.createArrayOfTitoliMovimentati();
                	}
                	else {
                		if (keySup == null) {
                        	mov = objectFactory.createInputMovimentazioneTitoli();
                        	titoliMov = objectFactory.createArrayOfTitoliMovimentati();
                		}
                	}

                	keySup = sbk.toString();                	
                	
                	CUAA cuaaIntestatario = objectFactory.createCUAA();
                	Intestatario intestatario = objectFactory.createIntestatario();
                	
                	if (fields[0].trim().length() == 16) {
                		cuaaIntestatario.setCodiceFiscalePersonaFisica(fields[0].trim());
                	}
                	else {
                		cuaaIntestatario.setCodiceFiscalePersonaGiuridica(fields[0].trim());
                	}
                	intestatario.setCUAA(cuaaIntestatario);
                	mov.setIntestatario(intestatario);
                	mov.setIdDocumento(fields[1]);
                	mov.setCampagna(fields[2]);
                	mov.setFattispecie(fields[3]);
                	if (!fields[4].isEmpty()) {
                    	CUAA cuaaCedente = objectFactory.createCUAA();
                    	Cedente cedente = objectFactory.createCedente();
                    	if (fields[4].trim().length() == 16) {
                    		cuaaCedente.setCodiceFiscalePersonaFisica(fields[4].trim());
                    	}
                    	else {
                    		cuaaCedente.setCodiceFiscalePersonaGiuridica(fields[4].trim());
                    	}
                    	cedente.setCUAA(cuaaCedente);
                    	mov.setCedente(cedente);
                	}
                	if (!fields[5].isEmpty()) {
                    	mov.setConsensoCedente(fields[5]);
                	}
                	if (!fields[6].isEmpty()) {
                		// BZ
                		Date dt = sdf_in.parse(fields[6]);
                    	mov.setDataConsenso(sdf_out.format(dt));
                		// TN
                    	//mov.setDataConsenso(fields[6]);
                	}
                	/*
                	 * aggiunta titolo movimentato
                	 */
                	TitoliMovimentati titMov = objectFactory.createTitoliMovimentati();
                	titMov.setPrimoIdentificativo(df.format(Long.parseLong(fields[7])));
                	if (fields.length > 8 && !fields[8].isEmpty()) {
                		// BZ
                		Date dt = sdf_in.parse(fields[8]);
                    	titMov.setDataScadenzaAffitto(sdf_out.format(dt));
                		// TN
                		//titMov.setDataScadenzaAffitto(fields[8]);
                	}
                	if (fields.length > 9 && !fields[9].isEmpty()) {
                		java.math.BigDecimal bd = new java.math.BigDecimal(new Double(fields[9]).doubleValue())
                				.setScale(6, java.math.BigDecimal.ROUND_HALF_UP);
                    	titMov.setSuperficieUBA(bd);
                	}
                	titoliMov.getTitoliMovimentati().add(titMov);
                	cntTit++;
            	}
            }
        	mov.setTitoliMovimentatiInput(titoliMov);
        	lista.add(mov);
        	cnt++;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }        
		log.debug("Movimenti#: " + lista.size());
        return lista;
	}
}