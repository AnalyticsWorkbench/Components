package eu.sisob.components.restclient;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String uri = "http://textanalytics.collide.info:8080/textanalytics-jersey-1.0-SNAPSHOT/text2network";
		Map<String,String> formParameters = new HashMap<>();
		formParameters.put("text", "Sara Smith, a Pasadena resident, went shopping. She is 30, and has lived at 3037 N. Foothill Street since 1992.\r\n" + 
				"Sara has been married to John for seven years. They have two children. Bob is five years old and Nancy is three.\r\n" + 
				"Sara owns a 1995 four-door blue Toyola. At 9 a.m., Sara got into her car and drove to Barget, a department store a mile away.\r\n" + 
				"Barget was having a holiday sale. Sara bought a four-slice toaster for $29.95 plus tax. The regular price was $39.95.\r\n" + 
				"She paid by check. On her way home, Sara stopped at MilkPlus to buy a gallon of nonfat milk. The milk was $3.50. Sara got 50 cents back in change.\r\n" + 
				"Sara arrived home at 10 a.m. John and the kids were still sleeping. She woke them up and then made a hot and nutritious breakfast for everyone.\r\n" + 
				"");
		formParameters.put("lang", "en");
		formParameters.put("window_size", "5");
		formParameters.put("doc_id", "test");
		JsonObject cmdmsg = new JsonObject();
		cmdmsg.addProperty("runid","");
		cmdmsg.addProperty("instanceid", "");
		cmdmsg.addProperty("agentid", "");
		cmdmsg.addProperty("pipes", "");
		cmdmsg.addProperty("parameters", "");
		String response = new RESTClientAgent(cmdmsg).callRestAPIPost(uri, formParameters);
		System.out.println(response);
		
	}

}
