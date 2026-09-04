package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.google.gson.Gson;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.TimeDataSet;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalculationCoefficient;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.BigDecimalWithUncertainty;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

public class ControsPco2Reducer extends DataReducer {

  private static final String MODE_PROPERTY = "zero_mode";

  private static final String MODE_CONTINUOUS = "Continuous";

  private static final String MODE_ZERO_BEFORE_SLEEP = "Zero before sleep";

  private static final String MODE_ZERO_AFTER_SLEEP = "Zero after sleep";

  protected static final String ZEROS_PROP = "contros.zeros";

  private static final BigDecimal T0 = new BigDecimal("273.15");

  private static final BigDecimal P0 = new BigDecimal("1013.25");

  private static List<CalculationParameter> calculationParameters = null;

  protected TreeMap<DoubleWithUncertainty, DoubleWithUncertainty> zeroS2Beams;

  private BigDecimalWithUncertainty F = null;

  private CalculationCoefficient k1Prior = null;

  private CalculationCoefficient k2Prior = null;

  private CalculationCoefficient k3Prior = null;

  private CalculationCoefficient runTimePrior = null;

  private CalculationCoefficient k1Post = null;

  private CalculationCoefficient k2Post = null;

  private CalculationCoefficient k3Post = null;

  private CalculationCoefficient runTimePost = null;

  private BigDecimalWithUncertainty k1Step = null;

  private BigDecimalWithUncertainty k2Step = null;

  private BigDecimalWithUncertainty k3Step = null;

  public ControsPco2Reducer(Variable variable,
    Map<String, Properties> properties, CalibrationSet calculationCoefficients)
    throws SensorTypeNotFoundException {
    super(variable, properties, calculationCoefficients);
  }

  @Override
  public void preprocess(Connection conn, Instrument instrument,
    DataSet dataset, List<Measurement> allMeasurements)
    throws DataReductionException {

    try {
      TimeDataSet castDataset = (TimeDataSet) dataset;

      F = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "F", castDataset.getStartTime()).getBigDecimalValue();

      calcKSteps(castDataset);
      calcZeroS2Beams(castDataset, allMeasurements);
    } catch (Exception e) {
      throw new DataReductionException(e);
    }
  }

  private void calcKSteps(TimeDataSet dataset) {
    k1Prior = CalculationCoefficient.getCoefficient(calculationCoefficients,
      variable, "k1", dataset.getStartTime());
    k2Prior = CalculationCoefficient.getCoefficient(calculationCoefficients,
      variable, "k2", dataset.getStartTime());
    k3Prior = CalculationCoefficient.getCoefficient(calculationCoefficients,
      variable, "k3", dataset.getStartTime());
    runTimePrior = CalculationCoefficient.getCoefficient(
      calculationCoefficients, variable, "Runtime", dataset.getStartTime());

    k1Post = CalculationCoefficient.getPostCoefficient(calculationCoefficients,
      variable, "k1", dataset.getEndTime());
    k2Post = CalculationCoefficient.getPostCoefficient(calculationCoefficients,
      variable, "k2", dataset.getEndTime());
    k3Post = CalculationCoefficient.getPostCoefficient(calculationCoefficients,
      variable, "k3", dataset.getEndTime());
    runTimePost = CalculationCoefficient.getPostCoefficient(
      calculationCoefficients, variable, "Runtime", dataset.getEndTime());

    if (null != k1Post && null != k2Post && null != k3Post
      && null != runTimePost) {

      BigDecimalWithUncertainty runtimePeriod = runTimePost.getBigDecimalValue()
        .subtract(runTimePrior.getBigDecimalValue());

      if (k1Post.getBigDecimalValue().equals(k1Prior.getBigDecimalValue())) {
        k1Step = BigDecimalWithUncertainty.ZERO;
      } else {
        BigDecimalWithUncertainty k1Diff = k1Post.getBigDecimalValue()
          .subtract(k1Prior.getBigDecimalValue());
        k1Step = k1Diff.divide(runtimePeriod);
      }

      if (k2Post.getBigDecimalValue().equals(k2Prior.getBigDecimalValue())) {
        k2Step = BigDecimalWithUncertainty.ZERO;
      } else {
        BigDecimalWithUncertainty k2Diff = k2Post.getBigDecimalValue()
          .subtract(k2Prior.getBigDecimalValue());
        k2Step = k2Diff.divide(runtimePeriod);
      }

      if (k3Post.getBigDecimalValue().equals(k3Prior.getBigDecimalValue())) {
        k3Step = BigDecimalWithUncertainty.ZERO;
      } else {
        BigDecimalWithUncertainty k3Diff = k3Post.getBigDecimalValue()
          .subtract(k3Prior.getBigDecimalValue());
        k3Step = k3Diff.divide(runtimePeriod);
      }
    } else {
      k1Step = BigDecimalWithUncertainty.ZERO;
      k2Step = BigDecimalWithUncertainty.ZERO;
      k3Step = BigDecimalWithUncertainty.ZERO;
    }
  }

  /**
   * Calculate zero S₂beam values
   *
   * @throws SensorTypeNotFoundException
   */
  protected void calcZeroS2Beams(DataSet dataset,
    List<Measurement> allMeasurements) throws SensorTypeNotFoundException {
    zeroS2Beams = new TreeMap<DoubleWithUncertainty, DoubleWithUncertainty>();

    // We calculate zero beams as averages within their run
    String currentRunType = "";
    ArrayList<DoubleWithUncertainty> runTimes = new ArrayList<DoubleWithUncertainty>();
    ArrayList<DoubleWithUncertainty> s2Beams = new ArrayList<DoubleWithUncertainty>();

    for (Measurement measurement : allMeasurements) {

      String runType = measurement.getRunType(variable);

      if (!runType.equals(currentRunType)) {
        if (currentRunType.equals(Measurement.INTERNAL_CALIBRATION_RUN_TYPE)) {
          if (runTimes.size() > 0) {
            zeroS2Beams.put(DoubleWithUncertainty.mean(runTimes),
              DoubleWithUncertainty.mean(s2Beams));
            runTimes = new ArrayList<DoubleWithUncertainty>();
            s2Beams = new ArrayList<DoubleWithUncertainty>();
          }
        }
        currentRunType = runType;
      }

      if (runType.equals(Measurement.INTERNAL_CALIBRATION_RUN_TYPE)) {

        DoubleWithUncertainty rawSignal = measurement
          .getMeasurementValue("Raw Detector Signal").getCalculatedValue();

        if (!rawSignal.isNaN()) {
          runTimes.add(
            measurement.getMeasurementValue("Runtime").getCalculatedValue());
          s2Beams.add(calcS2Beam(measurement));
        }
      }
    }

    if (runTimes.size() > 0) {
      zeroS2Beams.put(DoubleWithUncertainty.mean(runTimes),
        DoubleWithUncertainty.mean(s2Beams));
      runTimes = new ArrayList<DoubleWithUncertainty>();
      s2Beams = new ArrayList<DoubleWithUncertainty>();
    }

    dataset.setProperty(variable, ZEROS_PROP, new Gson().toJson(zeroS2Beams));
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws DataReductionException {

    try {
      DoubleWithUncertainty doubleRuntime = measurement
        .getMeasurementValue("Runtime").getCalculatedValue();

      // A NaN Runtime is an invalid measurement. Skip it.
      if (!doubleRuntime.isNaN()) {

        // We use BigDecimals to maintain the precision on the k parameters,
        // which are on the order of 1e-10
        BigDecimalWithUncertainty measurementRuntime = new BigDecimalWithUncertainty(
          doubleRuntime);

        DoubleWithUncertainty measurementS2Beam = calcS2Beam(measurement);

        DoubleWithUncertainty zeroS2Beam;
        DoubleWithUncertainty sProc;
        DoubleWithUncertainty xco2;
        DoubleWithUncertainty pCO2SST;
        DoubleWithUncertainty fCO2;

        if (!measurementS2Beam.isNaN()) {
          BigDecimalWithUncertainty bdMeasurementS2Beam = new BigDecimalWithUncertainty(
            measurementS2Beam);

          DoubleWithUncertainty interpZeroS2Beam = getInterpZeroS2Beam(
            measurementRuntime);

          if (null != interpZeroS2Beam) {

            try {
              BigDecimalWithUncertainty bdZeroS2Beam = new BigDecimalWithUncertainty(
                interpZeroS2Beam);

              BigDecimalWithUncertainty sDC = bdMeasurementS2Beam
                .divide(bdZeroS2Beam);

              BigDecimalWithUncertainty bdSProc = F
                .multiply(BigDecimalWithUncertainty.ONE.subtract(sDC));

              BigDecimalWithUncertainty runtimeSincePre = measurementRuntime
                .subtract(runTimePrior.getBigDecimalValue());

              BigDecimalWithUncertainty k1Interp = k1Prior.getBigDecimalValue()
                .add(k1Step.multiply(runtimeSincePre));
              BigDecimalWithUncertainty k2Interp = k2Prior.getBigDecimalValue()
                .add(k2Step.multiply(runtimeSincePre));
              BigDecimalWithUncertainty k3Interp = k3Prior.getBigDecimalValue()
                .add(k3Step.multiply(runtimeSincePre));

              BigDecimalWithUncertainty sProcCubed = bdSProc.pow(3);
              BigDecimalWithUncertainty sProcSquared = bdSProc.pow(2);

              BigDecimalWithUncertainty k3Part = k3Interp.multiply(sProcCubed);
              BigDecimalWithUncertainty k2Part = k2Interp
                .multiply(sProcSquared);
              BigDecimalWithUncertainty k1Part = k1Interp.multiply(bdSProc);

              BigDecimalWithUncertainty xco2ProcPart = k3Part.add(k2Part)
                .add(k1Part);

              // Gas temperature in Kelvin
              BigDecimalWithUncertainty gasTemperature = new BigDecimalWithUncertainty(
                measurement.getMeasurementValue("Gas Stream Temperature")
                  .getCalculatedValue())
                .add(T0);

              BigDecimalWithUncertainty gasPressure = new BigDecimalWithUncertainty(
                measurement.getMeasurementValue("Gas Stream Pressure")
                  .getCalculatedValue());

              BigDecimalWithUncertainty membranePressure = new BigDecimalWithUncertainty(
                measurement.getMeasurementValue("Membrane Pressure")
                  .getCalculatedValue());

              BigDecimalWithUncertainty pressureTimesTemp = gasTemperature
                .multiply(P0);

              BigDecimalWithUncertainty tempTimesPressure = gasPressure
                .multiply(T0);

              BigDecimalWithUncertainty xcoPresTempPart = pressureTimesTemp
                .divide(tempTimesPressure);

              BigDecimalWithUncertainty bdXCO2 = xco2ProcPart
                .multiply(xcoPresTempPart);

              BigDecimalWithUncertainty pco2PressurePart = membranePressure
                .divide(P0);

              BigDecimalWithUncertainty bdPCO2SST = bdXCO2
                .multiply(pco2PressurePart);

              DoubleWithUncertainty waterTemp = measurement
                .getMeasurementValue("Water Temperature").getCalculatedValue();

              fCO2 = Calculators.calcfCO2(bdPCO2SST.toDoubleWithUncertainty(),
                bdXCO2.toDoubleWithUncertainty(),
                membranePressure.toDoubleWithUncertainty(), waterTemp);

              // Make Double values for data reduction record
              zeroS2Beam = bdZeroS2Beam.toDoubleWithUncertainty();
              sProc = bdSProc.toDoubleWithUncertainty();
              xco2 = bdXCO2.toDoubleWithUncertainty();
              pCO2SST = bdPCO2SST.toDoubleWithUncertainty();
            } catch (NumberFormatException e) {
              /*
               * This will happen if any of the found measurement values are
               * NaN. As long as the CONTROS file isn't messed with, this
               * shouldn't happen.
               */
              zeroS2Beam = DoubleWithUncertainty.NaN;
              sProc = DoubleWithUncertainty.NaN;
              xco2 = DoubleWithUncertainty.NaN;
              pCO2SST = DoubleWithUncertainty.NaN;
              fCO2 = DoubleWithUncertainty.NaN;
            }
          } else {
            zeroS2Beam = DoubleWithUncertainty.NaN;
            sProc = DoubleWithUncertainty.NaN;
            xco2 = DoubleWithUncertainty.NaN;
            pCO2SST = DoubleWithUncertainty.NaN;
            fCO2 = DoubleWithUncertainty.NaN;
          }

          record.put("Zero S₂beam", zeroS2Beam);
          record.put("S₂beam",
            zeroS2Beam.isNaN() ? DoubleWithUncertainty.NaN : measurementS2Beam);
          record.put("Sproc", sProc);
          record.put("xCO₂", xco2);
          record.put("pCO₂ SST", pCO2SST);
          record.put("fCO₂", fCO2);
        }
      }
    } catch (DataReductionException e) {
      throw e;
    } catch (Exception e) {
      throw new DataReductionException(e);
    }
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(8);

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "Zero S₂beam", "Interpolated Zero Signal", "CONZERO2BEAM", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(1),
        "S₂beam", "Two-beam Signal", "CON2BEAM", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(2),
        "Sproc", "Drift-corrected Signal", "CONSPROC", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(3),
        "xCO₂", "xCO₂ In Water", "XCO2WBDY", "μmol/mol", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(4),
        "pCO₂ SST", "pCO₂ In Water", "PCO2TK02", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(5),
        "fCO₂", "fCO₂ In Water", "FCO2XXXX", "μatm", true));
    }

    return calculationParameters;
  }

  private DoubleWithUncertainty calcS2Beam(Measurement measurement)
    throws SensorTypeNotFoundException {
    return measurement.getMeasurementValue("Raw Detector Signal")
      .getCalculatedValue().divide(measurement
        .getMeasurementValue("Reference Signal").getCalculatedValue());
  }

  private DoubleWithUncertainty getInterpZeroS2Beam(
    BigDecimalWithUncertainty runTime) throws DataReductionException {

    Map.Entry<DoubleWithUncertainty, DoubleWithUncertainty> prior = zeroS2Beams
      .floorEntry(runTime.toDoubleWithUncertainty());
    Map.Entry<DoubleWithUncertainty, DoubleWithUncertainty> post = zeroS2Beams
      .ceilingEntry(runTime.toDoubleWithUncertainty());

    DoubleWithUncertainty result;

    switch (getStringProperty(MODE_PROPERTY)) {
    case MODE_CONTINUOUS: {

      // The Runtime has no uncertainty so we can extract it here.
      Double priorX = prior.getKey().value();
      DoubleWithUncertainty priorY = prior.getValue();
      Double postX = post.getKey().value();
      DoubleWithUncertainty postY = post.getValue();

      result = Calculators.interpolate(priorX, priorY, postX, postY,
        runTime.toDoubleWithUncertainty().value());
      break;
    }
    case MODE_ZERO_AFTER_SLEEP: {
      result = null == prior ? null : prior.getValue();
      break;
    }
    case MODE_ZERO_BEFORE_SLEEP: {
      result = null == post ? null : post.getValue();
      break;
    }
    default:
      throw new DataReductionException("Invalid zero mode");
    }

    return result;
  }
}
