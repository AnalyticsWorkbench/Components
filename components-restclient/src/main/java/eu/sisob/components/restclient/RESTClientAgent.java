package eu.sisob.components.restclient;

import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import com.google.gson.JsonObject;
import eu.sisob.components.framework.Agent;

/**
 * @author Taskin
 */

public class RESTClientAgent extends Agent {
	
	public static final String REQUESTMEDIATYPE = MediaType.APPLICATION_FORM_URLENCODED;
	public static final String RESPONSEMEDIATYPE = MediaType.APPLICATION_JSON;

	public RESTClientAgent(JsonObject commandMsg) {
		super(commandMsg);
	}
	

	@Override
	public void executeAgent(JsonObject dataMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeAgent(List<JsonObject> dataMessages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void uploadResults() {
		// TODO Auto-generated method stub
		
	}
	
	
	public String callRestAPIPost(String uri, Map<String, String> formParameters){
		//JaxRS Code
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(uri);
		//add all the parameters for the request to the web target
		Form form = new Form();
		for(String key : formParameters.keySet()){
			String value = formParameters.get(key);
			form.param(key, value);
		}
		Invocation.Builder invocationBuilder =
		        webTarget.request(REQUESTMEDIATYPE);
		
		invocationBuilder.header("Accept", RESPONSEMEDIATYPE);
		
		String resp = invocationBuilder.post(Entity.form(form), String.class);
		
		client.close();
		
		return resp;
	}

}
