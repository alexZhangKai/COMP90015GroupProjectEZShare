package EZShare;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SubscriptionManager {
	private static List<Subscription> subList = new ArrayList<Subscription>();
	
	public static synchronized void addSubscription(Subscription newSub) {
		if(checkSubId(newSub.getId())) {
			subList.add(newSub);
		}
	}
	
	public static boolean checkSubId(String newId) {
		for(Subscription sub : subList) {
			if(sub.getId().equals(newId)) {
				return false;
			}
		}
		return true;
	}
	
	public static void allSubMatch(Resource newRes) throws IOException {
		for(Subscription sub : subList) {
			sub.matchNewResource(newRes);
		}
	}
	
	public static void newServerCome(JSONArray newServerList) {
		for(Subscription sub : subList) {
			sub.newRelay(newServerList);
		}
	}
	
	public static void removeSubscription(Subscription oldSub) {
		if(!subList.remove(oldSub)) {
			System.out.println("remove does not success");
		};
	}
}
