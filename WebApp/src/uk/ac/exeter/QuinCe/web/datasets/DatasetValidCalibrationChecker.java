package uk.ac.exeter.QuinCe.web.datasets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.InvalidCalibrationDateException;

public abstract class DatasetValidCalibrationChecker {

  protected List<String> validationMessages = new ArrayList<String>();

  private boolean valid = true;

  protected abstract String getItemDescription();

  protected abstract CalibrationDB getCalibrationDB();

  protected void validateCalibrations(DataSource dataSource,
    Instrument instrument, LocalDateTime start, LocalDateTime end)
    throws Exception {

    valid = true;

    /*
     * Retrieve the CalibrationSet for the dataset time range.
     *
     * This automatically checks whether a calibration exists within the dataset
     * and whether that's allowed, so we can catch the exception and deal with
     * it.
     *
     * I know this invalidates the "Don't use exceptions for logic" principle,
     * but it's too hard to unpick right now and I have more important things to
     * get to.
     */
    CalibrationSet calibrations = null;
    try {
      calibrations = getCalibrationDB().getCalibrationSet(dataSource,
        instrument, start, end);
    } catch (InvalidCalibrationDateException e) {
      valid = false;
      validationMessages
        .add("Cannot change " + getItemDescription() + " within a dataset");

    }

    if (valid) {
      if (!checkPriors(calibrations)) {
        valid = false;
        validationMessages
          .add("No complete set of " + getItemDescription() + " available");
      }
    }
  }

  protected boolean checkPriors(CalibrationSet calibrations) {
    boolean valid = true;

    switch (getCalibrationDB().getCalibrationSetRequirements()) {
    case CalibrationDB.SET_COMPLETE: {
      if (!calibrations.hasCompletePrior()) {
        valid = false;
      }
      break;
    }
    case CalibrationDB.SET_COMPLETE_OR_EMPTY: {
      if (!calibrations.hasCompletePrior() && !calibrations.hasEmptyPrior()) {
        valid = false;
      }
      break;
    }
    }

    return valid;
  }

  protected boolean isValid() {
    return valid;
  }

  protected List<String> getValidationMessages() {
    return validationMessages;
  }
}
