package quark.model;

public class ParseException extends Exception {
  private static final long serialVersionUID = 1L;

  public ParseException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
