package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.nevec.rjm.BigDecimalMath;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetMeasurements;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.HaganGenXMeasurementLocator;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementLocatorException;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValueCollector;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValueCollectorFactory;
import uk.ac.exeter.QuinCe.data.Dataset.TimeCoordinate;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.MutableBigDecimal;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Reducer for Hagan GenX water measurements.
 *
 * <p>
 * The calculation code is translated from the official processing software,
 * with adjustments to fit how QuinCe processes data files.
 * </p>
 */
public class HaganGenXEqReducer extends DataReducer {

	protected SensorType tempSensorType;
	protected SensorType pressureSensorType;
	protected SensorType co2Raw1SensorType;
	protected SensorType co2Raw2SensorType;
	protected SensorType rhSensorType;
	protected SensorType rhTempSensorType;
	protected SensorType calKSensorType;
	protected SensorType spanRefSensorType;
	protected SensorType spanSlopeSensorType;

	private static final BigDecimal p0 = new BigDecimal(99D);

	// Pressure correction Coefficients
	private static final BigDecimal b1 = new BigDecimal(1.101583D);
	private static final BigDecimal b2 = new BigDecimal(-0.006121779D);
	private static final BigDecimal b3 = new BigDecimal(-0.2662779D);
	private static final BigDecimal b4 = new BigDecimal(3.698951D);
	private static final BigDecimal b5 = new BigDecimal(0.496099382D);

	/*
	 * Mole Fraction coefficients
	 *
	 * these have been divided (To(50) + 273.15) from original and offset the term
	 * in the final temp correction
	 */
	private static final BigDecimal a1 = new BigDecimal(0.39899740D);
	private static final BigDecimal a2 = new BigDecimal(18.24935912D);
	private static final BigDecimal a3 = new BigDecimal(0.097101982D);
	private static final BigDecimal a4 = new BigDecimal(1.845891413D);

	// These are calculated in the static block
	private static final BigDecimal A;
	private static final BigDecimal B;
	private static final BigDecimal D;

	static {
		A = a2.subtract(a4);

		// B = 2 * A * ((a1* a4) - (a2* a3))
		BigDecimal a1xa4 = a1.multiply(a4);
		BigDecimal a2xa3 = a2.multiply(a3);
		BigDecimal aPart = a1xa4.subtract(a2xa3);
		BigDecimal ax2 = A.multiply(new BigDecimal(2D));
		B = ax2.multiply(aPart);

		// D = (a3* a2) + (a1* a4)
		D = a2xa3.add(a1xa4);
	}

	private static List<CalculationParameter> calculationParameters = null;

	private TreeMap<TimeCoordinate, DataReductionElement> zeroCalKs = new TreeMap<TimeCoordinate, DataReductionElement>();

	private TreeMap<TimeCoordinate, DataReductionElement> spanCalKs = new TreeMap<TimeCoordinate, DataReductionElement>();

	private TreeMap<TimeCoordinate, MeasurementValue> spanRhs = new TreeMap<TimeCoordinate, MeasurementValue>();

	private TreeMap<TimeCoordinate, MeasurementValue> spanRhTemps = new TreeMap<TimeCoordinate, MeasurementValue>();

	public HaganGenXEqReducer(Variable variable, Map<String, Properties> properties,
			CalibrationSet calculationCoefficients) {
		super(variable, properties, calculationCoefficients);
	}

	public void preprocess(Connection conn, Instrument instrument, DataSet dataset, DatasetSensorValues allSensorValues,
			DatasetMeasurements allMeasurements) throws DataReductionException {

		try {
			tempSensorType = ResourceManager.getInstance().getSensorsConfiguration().getSensorType("GenX Temperature");
			pressureSensorType = ResourceManager.getInstance().getSensorsConfiguration().getSensorType("GenX Pressure");
			co2Raw1SensorType = ResourceManager.getInstance().getSensorsConfiguration().getSensorType("GenX CO₂ Raw 1");
			co2Raw2SensorType = ResourceManager.getInstance().getSensorsConfiguration().getSensorType("GenX CO₂ Raw 2");
			rhSensorType = ResourceManager.getInstance().getSensorsConfiguration()
					.getSensorType("GenX Relative Humidity");
			rhTempSensorType = ResourceManager.getInstance().getSensorsConfiguration()
					.getSensorType("GenX Relative Humidity Temperature");
			calKSensorType = ResourceManager.getInstance().getSensorsConfiguration().getSensorType("GenX CALK");
			spanRefSensorType = ResourceManager.getInstance().getSensorsConfiguration().getSensorType("GenX Span Ref");
			spanSlopeSensorType = ResourceManager.getInstance().getSensorsConfiguration()
					.getSensorType("GenX Span Slope");

			MeasurementValueCollector measurementValueCollector = MeasurementValueCollectorFactory
					.getCollector(variable);

			// Process Zero measurements
			for (Measurement measurement : allMeasurements.getOrderedMeasurements()) {
				if (HaganGenXMeasurementLocator.getRunType(measurement).equals("zero")) {

					Collection<MeasurementValue> measurementValues = measurementValueCollector.collectMeasurementValues(
							instrument, dataset, variable, allMeasurements, allSensorValues, conn, measurement);

					MeasurementValue co2Raw1 = getMeasurementValue(measurementValues, co2Raw1SensorType);
					MeasurementValue co2Raw2 = getMeasurementValue(measurementValues, co2Raw2SensorType);

					DataReductionElement zeroCalK = new DataReductionElement();

					BigDecimal bdco2Raw2 = new BigDecimal(co2Raw2.getCalculatedValue());
					BigDecimal bdco2Raw1 = new BigDecimal(co2Raw1.getCalculatedValue());
					zeroCalK.setValue(bdco2Raw2.divide(bdco2Raw1, MathContext.DECIMAL128));
					zeroCalK.addSensorValueIDs(co2Raw1);
					zeroCalK.addSensorValueIDs(co2Raw2);

					zeroCalKs.put((TimeCoordinate) measurement.getCoordinate(), zeroCalK);
				}
			}

			// Process Span measurements
			for (Measurement measurement : allMeasurements.getOrderedMeasurements()) {
				if (HaganGenXMeasurementLocator.getRunType(measurement).equals("span")) {

					Collection<MeasurementValue> measurementValues = measurementValueCollector.collectMeasurementValues(
							instrument, dataset, variable, allMeasurements, allSensorValues, conn, measurement);

					MeasurementValue temp = getMeasurementValue(measurementValues, tempSensorType);
					MeasurementValue pressure = getMeasurementValue(measurementValues, pressureSensorType);
					MeasurementValue co2Raw1 = getMeasurementValue(measurementValues, co2Raw1SensorType);
					MeasurementValue co2Raw2 = getMeasurementValue(measurementValues, co2Raw2SensorType);
					MeasurementValue rh = getMeasurementValue(measurementValues, rhSensorType);
					MeasurementValue rhTemp = getMeasurementValue(measurementValues, rhTempSensorType);

					DataReductionElement zeroCalK = DataReductionElement
							.getInterpolatedElement((TimeCoordinate) measurement.getCoordinate(), zeroCalKs);

					MeasurementValue measurementCalK = getMeasurementValue(measurementValues, calKSensorType);

					MeasurementValue spanRef = getMeasurementValue(measurementValues, spanRefSensorType);

					MeasurementValue spanSlope = getMeasurementValue(measurementValues, spanSlopeSensorType);

					BigDecimal spanCalK = convergeSpanCalK(new BigDecimal(temp.getCalculatedValue()),
							new BigDecimal(pressure.getCalculatedValue()), new BigDecimal(co2Raw1.getCalculatedValue()),
							new BigDecimal(co2Raw2.getCalculatedValue()), new BigDecimal(rh.getCalculatedValue()),
							new BigDecimal(rhTemp.getCalculatedValue()), zeroCalK.getValue(),
							new BigDecimal(measurementCalK.getCalculatedValue()),
							new BigDecimal(spanRef.getCalculatedValue()),
							new BigDecimal(spanSlope.getCalculatedValue()));

					DataReductionElement spanCalKElement = new DataReductionElement();
					spanCalKElement.setValue(spanCalK);
					spanCalKElement.addSensorValueIDs(temp, pressure, co2Raw1, co2Raw2, rh, rhTemp, measurementCalK,
							spanRef, spanSlope);
					spanCalKElement.addSensorValueIDs(zeroCalK);

					TimeCoordinate coord = (TimeCoordinate) measurement.getCoordinate();

					spanCalKs.put(coord, spanCalKElement);
					spanRhs.put(coord, rh);
					spanRhTemps.put(coord, rhTemp);
				}
			}

		} catch (MeasurementLocatorException e) {
			throw new DataReductionException(e.getMessage());
		} catch (Exception e) {
			throw new DataReductionException(e);
		}
	}

	@Override
	public void doCalculation(Instrument instrument, Measurement measurement, DataReductionRecord record,
			Connection conn) throws DataReductionException {

		BigDecimal zeroCalK = getZeroCalK(measurement);
		record.put("ZeroCalK", zeroCalK.doubleValue());

		// Get the spanCalK at the measurement time
		BigDecimal spanCalK = getSpanCalK(measurement);
		record.put("SpanCalK", spanCalK.doubleValue());

		BigDecimal temp = new BigDecimal(measurement.getMeasurementValue(tempSensorType).getCalculatedValue());
		BigDecimal pressure = new BigDecimal(measurement.getMeasurementValue(pressureSensorType).getCalculatedValue());
		BigDecimal co2Raw1 = new BigDecimal(measurement.getMeasurementValue(co2Raw1SensorType).getCalculatedValue());
		BigDecimal co2Raw2 = new BigDecimal(measurement.getMeasurementValue(co2Raw2SensorType).getCalculatedValue());
		BigDecimal rh = new BigDecimal(measurement.getMeasurementValue(rhSensorType).getCalculatedValue());
		BigDecimal rhTemp = new BigDecimal(measurement.getMeasurementValue(rhTempSensorType).getCalculatedValue());
		BigDecimal spanSlope = new BigDecimal(
				measurement.getMeasurementValue(spanSlopeSensorType).getCalculatedValue());

		// Can these persist across measurements?
		MutableBigDecimal r_absp = new MutableBigDecimal(BigDecimal.ZERO);
		MutableBigDecimal s_absp = new MutableBigDecimal(BigDecimal.ZERO);

		BigDecimal xCO2Wet = calculatedCO2(temp, pressure, co2Raw1, co2Raw2, rh, rhTemp, zeroCalK, spanCalK, r_absp,
				s_absp, spanSlope);

		record.put("xCO2Wet", xCO2Wet.doubleValue());

		BigDecimal spanRh = getSpanRh(measurement);
		BigDecimal spanRhTemp = getSpanRhTemp(measurement);
		record.put("spanRh", spanRh.doubleValue());
		record.put("spanRhTemp", spanRhTemp.doubleValue());

		BigDecimal vpSat = calcVpSat(spanRhTemp);
		BigDecimal co2VPrh = calcCo2VPrh(rh, spanRh, vpSat);
		BigDecimal xCO2Dry = calcXCO2Dry(xCO2Wet, pressure, co2VPrh);

		record.put("co2VPrh", co2VPrh.doubleValue());
		record.put("xCO2Dry", xCO2Dry.doubleValue());
	}

	protected BigDecimal calcVpSat(BigDecimal spanRhTemp) {

		/*
		 * Original code:
		 * 
		 * vpSat = 0.61365484 * Math.Exp(17.502 * spanPumpOnRhTemp / (240.97 +
		 * spanPumpOnRhTemp));
		 */

		BigDecimal expPart1 = new BigDecimal(17.502D).multiply(spanRhTemp);
		BigDecimal expPart2 = new BigDecimal(240.97D).add(spanRhTemp);
		BigDecimal exp = BigDecimalMath.exp(expPart1.divide(expPart2, MathContext.DECIMAL128));

		return new BigDecimal(0.61365484D).multiply(exp);
	}

	protected BigDecimal calcCo2VPrh(BigDecimal rh, BigDecimal spanRh, BigDecimal vpSat) {

		/*
		 * Original code:
		 * 
		 * co2VPrh = ((rh - spanRh) * vpSat) / 100;
		 */

		BigDecimal subractionPart = rh.subtract(spanRh);
		BigDecimal topPart = subractionPart.multiply(vpSat);
		return topPart.divide(new BigDecimal(100D), MathContext.DECIMAL128);
	}

	protected BigDecimal calcXCO2Dry(BigDecimal xCO2Wet, BigDecimal pressure, BigDecimal co2VPrh) {

		/*
		 * Original code:
		 * 
		 * xCO2Dry = xCO2Wet * pressure / (pressure - co2VPrh);
		 */

		BigDecimal top = xCO2Wet.multiply(pressure);
		BigDecimal bottom = pressure.subtract(co2VPrh);
		return top.divide(bottom, MathContext.DECIMAL128);
	}

	protected BigDecimal getZeroCalK(Measurement measurement) {
		// Get the zeroCalK at the measurement time
		Map.Entry<TimeCoordinate, DataReductionElement> priorZeroCalK = zeroCalKs
				.floorEntry((TimeCoordinate) measurement.getCoordinate());

		Map.Entry<TimeCoordinate, DataReductionElement> postZeroCalK = zeroCalKs
				.ceilingEntry((TimeCoordinate) measurement.getCoordinate());

		return Calculators.interpolateTimeAndDataReductionElement(priorZeroCalK, postZeroCalK,
				(TimeCoordinate) measurement.getCoordinate());
	}

	protected BigDecimal getSpanCalK(Measurement measurement) {
		Map.Entry<TimeCoordinate, DataReductionElement> priorSpanCalK = spanCalKs
				.floorEntry((TimeCoordinate) measurement.getCoordinate());

		Map.Entry<TimeCoordinate, DataReductionElement> postSpanCalK = spanCalKs
				.ceilingEntry((TimeCoordinate) measurement.getCoordinate());

		return Calculators.interpolateTimeAndDataReductionElement(priorSpanCalK, postSpanCalK,
				(TimeCoordinate) measurement.getCoordinate());
	}

	protected BigDecimal getSpanRh(Measurement measurement) {
		Map.Entry<TimeCoordinate, MeasurementValue> priorRh = spanRhs
				.floorEntry((TimeCoordinate) measurement.getCoordinate());

		Map.Entry<TimeCoordinate, MeasurementValue> postRh = spanRhs
				.ceilingEntry((TimeCoordinate) measurement.getCoordinate());

		return new BigDecimal(Calculators.interpolateTimeAndMeasurementValue(priorRh, postRh,
				(TimeCoordinate) measurement.getCoordinate()));
	}

	protected BigDecimal getSpanRhTemp(Measurement measurement) {
		Map.Entry<TimeCoordinate, MeasurementValue> priorRhTemp = spanRhTemps
				.floorEntry((TimeCoordinate) measurement.getCoordinate());

		Map.Entry<TimeCoordinate, MeasurementValue> postRhTemp = spanRhTemps
				.ceilingEntry((TimeCoordinate) measurement.getCoordinate());

		return new BigDecimal(Calculators.interpolateTimeAndMeasurementValue(priorRhTemp, postRhTemp,
				(TimeCoordinate) measurement.getCoordinate()));
	}

	@Override
	public List<CalculationParameter> getCalculationParameters() {
		if (null == calculationParameters) {
			calculationParameters = new ArrayList<CalculationParameter>(1);

			calculationParameters
					.add(new CalculationParameter(makeParameterId(0), "ZeroCalK", "ZeroCalK", "ZeroCalK", "", false));

			calculationParameters
					.add(new CalculationParameter(makeParameterId(1), "SpanCalK", "SpanCalK", "SpanCalK", "", false));

			calculationParameters
					.add(new CalculationParameter(makeParameterId(2), "xCO2Wet", "xCO2Wet", "xCO2Wet", "", true));

			calculationParameters
					.add(new CalculationParameter(makeParameterId(3), "spanRh", "spanRh", "spanRh", "", false));

			calculationParameters.add(
					new CalculationParameter(makeParameterId(4), "spanRhTemp", "spanRhTemp", "spanRhTemp", "", false));

			calculationParameters
					.add(new CalculationParameter(makeParameterId(5), "co2VPrh", "co2VPrh", "co2VPrh", "", false));

			calculationParameters
					.add(new CalculationParameter(makeParameterId(6), "xCO2Dry", "xCO2Dry", "xCO2Dry", "", true));
		}

		return calculationParameters;
	}

	private MeasurementValue getMeasurementValue(Collection<MeasurementValue> measurementValues, SensorType sensorType)
			throws DataReductionException {
		return measurementValues.stream().filter(mv -> mv.getSensorType().equals(sensorType)).findAny()
				.orElseThrow(() -> new DataReductionException("Missing " + sensorType.getShortName()));

	}

	protected static BigDecimal convergeSpanCalK(BigDecimal temp, BigDecimal pressure, BigDecimal raw1, BigDecimal raw2,
			BigDecimal rh, BigDecimal rhTemp, BigDecimal zeroCalK, BigDecimal spanCalKSeed, BigDecimal spanRef,
			BigDecimal spanSlope) {

		BigDecimal stepDecimal = new BigDecimal(0.01D);
		BigDecimal calcCO2;
		BigDecimal previousCalcCO2;
		BigDecimal calcSpanCalK = spanCalKSeed;

		MutableBigDecimal r_absp = new MutableBigDecimal(0D);
		MutableBigDecimal s_absp = new MutableBigDecimal(0D);

		if (temp.compareTo(BigDecimal.ZERO) == 0 || pressure.compareTo(BigDecimal.ZERO) == 0
				|| raw1.compareTo(BigDecimal.ZERO) == 0 || raw2.compareTo(BigDecimal.ZERO) == 0
				|| zeroCalK.compareTo(BigDecimal.ZERO) == 0 || spanCalKSeed.compareTo(BigDecimal.ZERO) == 0
				|| spanRef.compareTo(BigDecimal.ZERO) == 0)
			return (BigDecimal.ZERO);

		calcCO2 = calculatedCO2(temp, pressure, raw1, raw2, rh, rhTemp, zeroCalK, calcSpanCalK, r_absp, s_absp,
				spanSlope);
		while (stepDecimal.compareTo(new BigDecimal(0.00000001D)) == 1) {

			if (calcCO2.compareTo(spanRef) == 1) {
				calcSpanCalK = calcSpanCalK.subtract(stepDecimal);
			} else {
				calcSpanCalK = calcSpanCalK.add(stepDecimal);
			}

			previousCalcCO2 = calcCO2;
			calcCO2 = calculatedCO2(temp, pressure, raw1, raw2, rh, rhTemp, zeroCalK, calcSpanCalK, r_absp, s_absp,
					spanSlope);

			if (calcCO2.compareTo(spanRef) == 1 ^ previousCalcCO2.compareTo(spanRef) == 1) {

				if (previousCalcCO2.compareTo(spanRef) == 1) {
					calcSpanCalK = calcSpanCalK.add(stepDecimal);
				} else {
					calcSpanCalK = calcSpanCalK.subtract(stepDecimal);
				}

				calcCO2 = previousCalcCO2;
				stepDecimal = stepDecimal.divide(BigDecimal.TEN, MathContext.DECIMAL128);
			}
		}

		return (calcSpanCalK);
	}

	protected static BigDecimal calculatedCO2(BigDecimal temp, BigDecimal pressure, BigDecimal raw1, BigDecimal raw2,
			BigDecimal rh, BigDecimal rhTemp, BigDecimal zeroCalK, BigDecimal spanCalK, MutableBigDecimal r_absp,
			MutableBigDecimal s_absp, BigDecimal spanSlope) {

		BigDecimal p_absp;
		BigDecimal P;
		BigDecimal a_1;
		BigDecimal b_1;
		BigDecimal x;
		BigDecimal g;
		BigDecimal c_1;
		BigDecimal c_2;
		BigDecimal c_3;
		BigDecimal C;
		BigDecimal wc;

		/*
		 * Initialise r_absp
		 * 
		 * r_absp = 1 - ((raw1 / raw2) / zeroCalK)
		 */
		BigDecimal rabspRawDiv = raw1.divide(raw2, MathContext.DECIMAL128);
		BigDecimal rabspSubtrahend = rabspRawDiv.multiply(zeroCalK);
		r_absp.setValue(BigDecimal.ONE.subtract(rabspSubtrahend));

		// po is std pressure, po = 99
		// P is the ratio of the std pressure and measured press
		g = BigDecimal.ONE;

		if (pressure.compareTo(p0) != 0) {
			if (pressure.compareTo(p0) == -1) {
				P = p0.divide(pressure, MathContext.DECIMAL128);
			} else if (pressure.compareTo(p0) == 1) {
				P = pressure.divide(p0, MathContext.DECIMAL128);
			} else {
				P = BigDecimal.ONE;
			}

			// g is the empirical correction function and is a function of absorptance
			// and pressure

			// a_1 = 1 / (b1 * (P - 1));
			BigDecimal a1PMinusOne = P.subtract(BigDecimal.ONE);
			BigDecimal a1Divisor = b1.multiply(a1PMinusOne);
			a_1 = BigDecimal.ONE.divide(a1Divisor, MathContext.DECIMAL128);

			// b_1 = (1 / (b5 - r_absp)) - (1 / b5);
			BigDecimal b1MinuendDivisor = b5.subtract(r_absp.getValue());
			BigDecimal b1Minuend = BigDecimal.ONE.divide(b1MinuendDivisor, MathContext.DECIMAL128);
			BigDecimal b1Subtrahend = BigDecimal.ONE.divide(b5, MathContext.DECIMAL128);
			b_1 = b1Minuend.subtract(b1Subtrahend);

			// c_1 = (1 / (b2 + (b3 * P))) + b4;
			BigDecimal c1LeftDivisor = b2.add(b3.multiply(P));
			BigDecimal c1Left = BigDecimal.ONE.divide(c1LeftDivisor, MathContext.DECIMAL128);
			c_1 = c1Left.add(b4);

			// x = 1 + (1 / (a_1 + (b_1 / c_1)));
			BigDecimal xDivisor = a_1.add(b_1.divide(c_1, MathContext.DECIMAL128));
			BigDecimal xDivision = BigDecimal.ONE.divide(xDivisor, MathContext.DECIMAL128);
			x = BigDecimal.ONE.add(xDivision);

			if (pressure.compareTo(p0) == -1) {
				g = x;
			} else if (pressure.compareTo(p0) == 1) {
				g = BigDecimal.ONE.divide(x, MathContext.DECIMAL128);
			}
		}

		// the order of the next 2 lines was under discussion but if span slope is
		// used, this is the correct order for both Li820 and Li830

		// s_absp = r_absp * (spanCalK + (r_absp * spanSlope))

		BigDecimal sabspMultiplier = spanCalK.add(r_absp.getValue().multiply(spanSlope));
		s_absp.setValue(r_absp.getValue().multiply(sabspMultiplier));

		p_absp = s_absp.getValue().multiply(g);

		// Computing CO2 Mole Fraction
		// c_1 = D - ((a2 + a4) * p_absp);
		BigDecimal c1Subtrahend = a2.add(a4).multiply(p_absp);
		c_1 = D.subtract(c1Subtrahend);

		// c_2 = ((A * A) * (p_absp * p_absp)) + (B * p_absp) + (D * D);
		BigDecimal c2_partOne = A.pow(2).multiply(p_absp.pow(2));
		BigDecimal c2_partTwo = B.multiply(p_absp);
		c_2 = c2_partOne.add(c2_partTwo).add(D.pow(2));

		// c_3 = p_absp - a1 - a3;
		c_3 = p_absp.subtract(a1).subtract(a3);

		// C = ((c_1 - Math.sqrt(c_2)) / (2 * c_3)) * (temp + 273.15);
		BigDecimal CDividend = c_1.subtract(c_2.sqrt(MathContext.DECIMAL128));
		BigDecimal CDivisor = new BigDecimal(2D).multiply(c_3);
		C = CDividend.divide(CDivisor, MathContext.DECIMAL128).multiply(Calculators.kelvin(temp));

		// moisture compensation - band broadening effect
		// h=1.45
		// wc = 1 + (h-1) * xH2O(umol/mol) * 1E-6
		// xH2O(umol/mol) =
		// 1260*216.7*(RH/100.0*6.112*exp(17.62*T/(243.12+T))/(273.15+T)) where
		// RH(%) & T(C)

		// wc = 1 + 0.007509747 * rh * Math.exp(17.62 * rhTemp / (243.12 + rhTemp))
		// / (273.15 + rhTemp);

		BigDecimal wcExpTop = new BigDecimal(17.62D).multiply(rhTemp);
		BigDecimal wcExpBottom = new BigDecimal(243.12D).add(rhTemp);
		BigDecimal wcExp = BigDecimalMath.exp(wcExpTop.divide(wcExpBottom, MathContext.DECIMAL128));

		BigDecimal wcTop = new BigDecimal(0.007509747D).multiply(rh).multiply(wcExp);
		BigDecimal wcAddend = wcTop.divide(Calculators.kelvin(rhTemp), MathContext.DECIMAL128);
		wc = BigDecimal.ONE.add(wcAddend);

		C = C.multiply(wc);

		return C;
	}

}
