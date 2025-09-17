package uk.ac.exeter.QuinCe.web.Instrument;

import java.time.LocalDateTime;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Calibration;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Uncertainty;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.UncertaintyDB;
import uk.ac.exeter.QuinCe.jobs.Job;
import uk.ac.exeter.QuinCe.jobs.files.ExtractDataSetJob;

/**
 * Instance of the {@link CalibrationBean} for editing {@link Uncertainty}(ie)s.
 */
@ManagedBean
@SessionScoped
public class UncertaintiesBean extends CalibrationBean {

  /**
   * The navigation string for the calculation coefficients list.
   */
  private static final String NAV_LIST = "uncertainties";

  @Override
  protected String getPageNavigation() {
    return NAV_LIST;
  }

  @Override
  protected Class<? extends Job> getReprocessJobClass() {
    return ExtractDataSetJob.class;
  }

  @Override
  protected CalibrationDB getDbInstance() {
    return UncertaintyDB.getInstance();
  }

  @Override
  protected String getCalibrationType() {
    return UncertaintyDB.UNCERTAINTY_CALIBRATION_TYPE;
  }

  @Override
  public String getHumanReadableCalibrationType() {
    return "Uncertainties";
  }

  @Override
  protected Calibration initNewCalibration(long id, LocalDateTime date) {
    return new Uncertainty(getCurrentInstrument(), id, date);
  }

  @Override
  public String getTargetLabel() {
    return "Sensor";
  }

  @Override
  public String getCalibrationName() {
    return "Uncertainty";
  }

  @Override
  public String getCoefficientsLabel() {
    return "Uncertainty";
  }

  @Override
  protected boolean changeAffectsDatasetsAfterOnly() {
    return true;
  }

}
