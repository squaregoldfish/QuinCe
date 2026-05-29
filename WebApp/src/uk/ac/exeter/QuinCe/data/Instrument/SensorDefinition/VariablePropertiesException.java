package uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition;

/**
 * Exception class for issues encountered while processing
 * {@link VariableProperties}.
 */
@SuppressWarnings("serial")
public class VariablePropertiesException extends Exception {

  public VariablePropertiesException(String message) {
    super(message);
  }
}
