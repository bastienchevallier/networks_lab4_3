import java.io.File;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.io.IOException;

class FileReceiver implements Layer {

  private final Layer subLayer;
  private FileWriter received_fw;
  private boolean file_received = false;

  public FileReceiver(String destinationHost, int destinationPort,
      int connectionId) {
    subLayer = new ConnectedLayer(destinationHost, destinationPort,
        connectionId);
    subLayer.deliverTo(this);
  }

  public Layer getSubLayer() {
    return subLayer;
  }

  @Override
  public void send(String payload) {
    throw new UnsupportedOperationException(
        "don't support any send from above");
  }

  @Override
  public void receive(String payload, String sender) {
	  String reg_exp = "(SEND )(.*)";
	  Pattern pattern = Pattern.compile(reg_exp);
	  Matcher matcher = pattern.matcher(payload);
	  if (matcher.find()) {
		  try {
			received_fw = new FileWriter("_received_"+matcher.group(2));
		  } catch (IOException e) {
			e.printStackTrace();
		  }
		  return;
	  }
	  if(payload.equals("**CLOSE**")) {
		  file_received=true;
		  return;
	  }
	  try {
		  PrintWriter pw = new PrintWriter(received_fw);
		  pw.println(payload);
	  
		  received_fw.close();
	  }catch(NullPointerException e ) {
		  System.err.println("Received file wasn't initialized"+e.getMessage());
	  }catch(IOException e) {
		  System.err.println(e.getMessage());
	  }
	  
  }

  @Override
  public void deliverTo(Layer above) {
    throw new UnsupportedOperationException("don't support any Layer above");
  }

  @Override
  public void close() {
	  while (!file_received) {

		try {
			Thread.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	  }
	  System.out.println("closing");
	  subLayer.close();
  }

}

public class Server_4 {

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(
          "syntax : java Server_4 myPort destinationHost destinationPort");
      return;
    }
    if (GroundLayer.start(Integer.parseInt(args[0]))) {
      // GroundLayer.RELIABILITY = 0.5;
      FileReceiver receiver = new FileReceiver(args[1],
          Integer.parseInt(args[2]), (int) (Math.random() * Integer.MAX_VALUE));
      receiver.close();
      GroundLayer.close();
    }
  }
}