package bac.peers;

import bac.peers.Peer;
import bac.helper.Helper;
import bac.settings.Settings;
import bac.cron.Cron;

import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


public final class Peers {		

   public static String MyAnnouncedAddress ="127.0.0.1:8080";

   public static final int PEER_STATE_DISCONNECTED = 0;	
	public static final int PEER_STATE_CONNECTED = 1;
	

   public static HashMap<String, Peer> peers = new HashMap<>();
   public static int PeersCounter;

	public static void init(){ 
       PeersCounter=0;
       Helper.logMessage("Reset PeersCounter");
       MyAnnouncedAddress=Settings.APIhost+":"+Settings.APIport;
       Helper.logMessage("Announced Address:"+MyAnnouncedAddress);
		 for (String SeedNode : Settings.SeedNodes.split(";")) {
			 SeedNode = SeedNode.trim();
			 Peer.AddPeer(SeedNode);
       }
       Cron.AddCronThread( ConnectToPeers, 5 );
       Cron.AddCronThread( FindNewPeers, 7 );	
	}
	
	
	public static Runnable ConnectToPeers = new Runnable() { 
     public void run() {
     	
       try {     	
           Peer peer = Peer.GetRandomPeer(PEER_STATE_DISCONNECTED);
			  if (peer != null) {								
				 peer.PeerConnect();								
			  }
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (ConnectToPeers) error.");		 
		 }	  
     }
   };
   
   
	public static Runnable FindNewPeers = new Runnable() { 
     public void run() {
       try {     	
           Peer peer = Peer.GetRandomPeer(PEER_STATE_CONNECTED);
			  if (peer != null) {								
				 JSONObject request = new JSONObject();
			    request.put("requestType", "GetPeers");						
			    request.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");
			    JSONObject response = peer.SendJsonQueryToPeer(request);
			    Helper.logMessage("Find peers answer:"+response.toString());								
             JSONArray PeersList = (JSONArray)response.get("PeersList");
				 for (int i = 0; i < PeersList.size(); i++) {								
					 String announcedAddress = ((String)PeersList.get(i)).trim(); 
					 if (announcedAddress.length() > 0) {										
						 Peer.AddPeer(announcedAddress);										
					 }									
				 }
			  }
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (FindNewPeers) error.");		 
		 }	  

     }
   };


}