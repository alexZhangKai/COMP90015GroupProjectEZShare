package EZShare;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

public class SubscriptionManager {
	private static ArrayList<Subscription> subList = new ArrayList<Subscription>();
	
	public static synchronized void addSubscription(Subscription newSub) {
		System.out.println("addSubscription");
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
	
	public static void allSubMatch() throws IOException {
		System.out.println("allSubMatch");
		for(Subscription sub : subList) {
			sub.matchResource();
		}
	}
}
