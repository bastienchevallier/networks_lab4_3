import java.util.HashMap;
import java.util.Map;

public class DispatchLayer implements Layer {

  private static Map<Integer, Layer> table = new HashMap<Integer, Layer>();
  private static Layer dispatcher = null;

  public static synchronized void start() {
    if (dispatcher == null)
      dispatcher = new DispatchLayer();
    GroundLayer.deliverTo(dispatcher);
  }

  @SuppressWarnings("boxing")
  public static synchronized void register(Layer layer, int sessionId) {
    if (dispatcher != null) {
      table.put(sessionId, layer);
      GroundLayer.deliverTo(dispatcher);
    } else
      GroundLayer.deliverTo(layer);
  }

  private DispatchLayer() { // singleton pattern
  }

  @Override
  public void send(String payload) {
    throw new UnsupportedOperationException("don't use this for sending");
  }

  @Override
  public void receive(String payload, String source) {
  }

  @Override
  public void deliverTo(Layer above) {
    throw new UnsupportedOperationException(
        "don't support a single Layer above");
  }

  @Override
  public void close() { // nothing
  }

}