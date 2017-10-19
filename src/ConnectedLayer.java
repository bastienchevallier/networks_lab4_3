
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
	int last_transmitted=0;
	Object Monitor = new Object();
	
	
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
		TIMER.schedule(SendingTask,0,250);
		synchronized(Monitor) {
			try {
				Monitor.wait();
				pckt_number++;
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());;
			}
		}
		SendingTask.cancel();
		
	}

	@Override
	public void receive(String payload, String source) {
		String[] parsed_payload = payload.split(";",3);
		int Connect_Id = Integer.parseInt(parsed_payload[0]);
		int pckt_numb = Integer.parseInt(parsed_payload[1]);
		String rcvd_payload = parsed_payload[2];

		if (rcvd_payload.equals("--ACK--") && Connect_Id==ConnectionId){
			synchronized(Monitor) {
				Monitor.notify();
			}
		}
		
		if(rcvd_payload.equals("--HELLO--") && Connect_Id==ConnectionId) {
			String ack = Connect_Id + ";" +pckt_numb+ ";"+"--ACK--";
			GroundLayer.send(ack, destinationHost, destinationPort);
			
		}else if (!rcvd_payload.equals("--ACK--")) {
			String ack = Connect_Id+ ";" +pckt_numb+ ";"+"--ACK--";
			if (last_transmitted!=pckt_numb) {
				LayerAbove.receive(rcvd_payload, source);
				last_transmitted = pckt_numb;
			}
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
