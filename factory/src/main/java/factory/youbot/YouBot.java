/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/


package factory.youbot;


import factory.common.Constants;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class YouBot extends Agent implements Constants {
	private String status;
		
	private AID[] stationAgents;
	

	
	protected void setup() {
		System.out.println("Hello! youBot "+getAID().getName()+" is ready.");
		
		//get initialization arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			String argument0 = (String) args[0];
			
			// Initiate youBot Query for Work every 10seconds
			addBehaviour(new TickerBehaviour(this, 10000) {
				protected void onTick() {
					System.out.println("Querying for Work");
					
					// Update the list of seller agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("order_assembling");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Found the following stationAgents");
						stationAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							stationAgents[i] = result[i].getName();
							System.out.println(stationAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Perform the request
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		
		
		
	}
	
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("youBotAgent "+getAID().getName()+" terminating.");
	}
	
	



/**
Inner class RequestPerformer.
This is the behaviour used by youBot agents to request stationAgents
for work
*/

	private class RequestPerformer extends Behaviour {
		
		private AID commitedStation; // The Station to which the youBot commits working for 
		private int longestQueue;  // The longest received Station Queue
		private int repliesCnt = 0; // The counter of replies from station agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		
		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all stations
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < stationAgents.length; ++i) {
					cfp.addReceiver(stationAgents[i]);
				} 
				cfp.setContent("need something to do..");
				cfp.setConversationId(CONV_ID_PICKUP);
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId(CONV_ID_PICKUP),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive TransportationTask replies from Stations
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					// This is a transportation task offer because specified in mt - template
					int queue = Integer.parseInt(reply.getContent());
					if (commitedStation == null || queue < longestQueue) {
						// This is the best offer at present
						longestQueue = queue;
						commitedStation = reply.getSender();
					}
					}
					repliesCnt++;
					if (repliesCnt >= stationAgents.length) {
						// We received all replies
						step = 2; 
					}
				
				else {
					block();
				}
				break;
			case 2:
				// inform the commited Station about commitment  
				ACLMessage commitment = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				commitment.addReceiver(commitedStation);
				
				//optional todo - add content
				//commitment.setContent();
				commitment.setConversationId(CONV_ID_PICKUP);
				commitment.setReplyWith("commitment"+System.currentTimeMillis());
				myAgent.send(commitment);
				// Prepare the template to get the commitment-message reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId(CONV_ID_PICKUP),
						MessageTemplate.MatchInReplyTo(commitment.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the commitment reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// commitment reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// we can execute transportation
						//sleep 5s for virtualizing transport-time
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							Object item = reply.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println("couldn receive Station´s Object");
						}
						status = "loaded";
						
						System.out.println("successfully commited to work for agent "+reply.getSender().getName() + " starting to work now.");
						System.out.println("Queue = "+longestQueue);
						
						//myAgent.doDelete();
					}
					else {
						System.out.println("Attempt to commit to working failed.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}
		
		
		
	
	
	public boolean done() {
		if (step == 2 && commitedStation == null) {
			System.out.println("Attempt failed: no work available.");
		}
		return ((step == 2 && commitedStation == null) || step == 4);
		}
  // End of inner class RequestPerformer
	}

}