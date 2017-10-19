import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Connected Layer exercice 3
 
 * @author Bastien Chevallier
 * @author Jules Yates
 */

public class ConnectedLayer implements Layer {
	String destinationHost;
	int ConnectionId;
	int destinationPort;
	int pckt_number;
	Layer LayerAbove;
	Timer TIMER;
	
	public ConnectedLayer(String destinationHost, int destinationPort, int ConnectionId ) {
		this.destinationHost = destinationHost;
		this.destinationPort = destinationPort;
		this.pckt_number = 0;
		this.ConnectionId = ConnectionId;
		this.TIMER = new Timer("ConnecedLayerTimer",true);
		//The connected layer is the layer above the GroundLayer
		GroundLayer.deliverTo(this);
		//First message
		this.send("--HELLO--");
	}
	
	@Override
	public void send(String payload) {
		String wrapped_payload = Integer.toString(ConnectionId) + ";" + Integer.toString(pckt_number) + ";" + payload;
		TimerTask SendingTask = new TimerTask(){			
			@Override
			public void run(){
				GroundLayer.send(wrapped_payload, destinationHost, destinationPort);

			}
		};
		TIMER.schedule(SendingTask,0,10);
		SendingTask.cancel();
		pckt_number++;
	}

	@Override
	public void receive(String payload, String source) {
		String[] parsed_payload = payload.split(";",3);
		int Connect_Id = Integer.parseInt(parsed_payload[0]);
		int pckt_numb = Integer.parseInt(parsed_payload[1]);
		String rcvd_payload = parsed_payload[2];

		if (rcvd_payload.equals("--ACK--")){
			synchronized(this) {
				notify();
			}
		}
		if(rcvd_payload.equals("--HELLO--")) {
			//TODO Problem with the verification on connectionID
			String ack = Connect_Id + ";" +pckt_numb+ ";"+"--ACK--";
			GroundLayer.send(ack, destinationHost, destinationPort);
		}else if (!rcvd_payload.equals("--ACK--")) {
			String ack = Connect_Id+ ";" +pckt_numb+ ";"+"--ACK--";
			LayerAbove.receive(rcvd_payload, source);
			GroundLayer.send(ack, destinationHost, destinationPort);
		} 
	}

	@Override
	public void deliverTo(Layer above) {
		LayerAbove = above;
	}
	
	@Override
	public void close() {
	}
}
