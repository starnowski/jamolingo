public class OlingoKinds {
  public static void main(String[] args) {
    for (org.apache.olingo.server.api.uri.queryoption.ApplyItem.Kind k :
        org.apache.olingo.server.api.uri.queryoption.ApplyItem.Kind.values()) {
      System.out.println(k.name());
    }
  }
}
