package uk.ac.exeter.QuinCe.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

public class DoubleWithUncertaintyTest extends BaseTest {

  @Test
  public void goodConstructorTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, 2F);
    DoubleWithUncertaintyAssert.assertThat(value).matches(24D, 2F);
  }

  @Test
  public void nullValueTest() {
    assertThrows(NullPointerException.class, () -> {
      new DoubleWithUncertainty(null, 2F);
    });
  }

  @Test
  public void nanValueTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(Double.NaN, 2F);
    assertEquals(Double.NaN, value.value());
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void nanUncertaintyTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, Float.NaN);
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void nullUncertaintyTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, null);
    assertEquals(Float.NaN, value.uncertainty());
  }

  @ParameterizedTest
  @CsvSource({ "1F, true", "NaN, false" })
  public void hasUncertaintyTest(Float uncertainty, boolean hasUncertainty) {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, uncertainty);
    assertEquals(hasUncertainty, value.hasUncertainty());
  }

  @Test
  public void longConstructorTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(123L);
    DoubleWithUncertaintyAssert.assertThat(value).matches(123D, Float.NaN);
  }

  @Test
  public void integerValueConstructorTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(123);
    DoubleWithUncertaintyAssert.assertThat(value).matches(123D, Float.NaN);
  }

  @Test
  public void integerValueUncertaintyTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(123, 3);
    DoubleWithUncertaintyAssert.assertThat(value).matches(123D, 3F);
  }

  @ParameterizedTest
  @CsvSource({ "1D, false", "NaN, true" })
  public void isNaNTest(Double val, boolean isNan) {
    DoubleWithUncertainty value = new DoubleWithUncertainty(val);
    assertEquals(isNan, value.isNaN());
  }

  @Test
  public void staticNaNTest() {
    assertTrue(DoubleWithUncertainty.NaN.isNaN());
    assertEquals(Float.NaN, DoubleWithUncertainty.NaN.uncertainty());
  }

  @Test
  public void staticZeroTest() {
    DoubleWithUncertaintyAssert.assertThat(DoubleWithUncertainty.ZERO)
      .matches(0D, 0F);
  }

  @Test
  public void staticOneTest() {
    DoubleWithUncertaintyAssert.assertThat(DoubleWithUncertainty.ONE)
      .matches(1D, 0F);
  }

  @Test
  public void staticHundredTest() {
    DoubleWithUncertaintyAssert.assertThat(DoubleWithUncertainty.HUNDRED)
      .matches(100D, 0F);
  }

  private static Stream<Arguments> subtractTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0F, 0D, 0F),
      Arguments.of(1D, 0F, 1D, 0F, 0D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0.4F, 8.03D, 3.4234F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0F, 8.03D, 3.4F),
      Arguments.of(12.35D, 3.4F, 4.32D, Float.NaN, 8.03D, Float.NaN),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, 0F, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("subtractTestParams")
  public void subtractTest(Double minuendValue, Float minuendUncertainty,
    Double subtrahendValue, Float subtrahendUncertainty, Double resultValue,
    Float resultUncertainty) {

    DoubleWithUncertainty minuend = new DoubleWithUncertainty(minuendValue,
      minuendUncertainty);
    DoubleWithUncertainty subtrahend = new DoubleWithUncertainty(
      subtrahendValue, subtrahendUncertainty);

    DoubleWithUncertainty result = minuend.subtract(subtrahend);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> subtractConstantTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0D, 0F),
      Arguments.of(1D, 0F, 1D, 0D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 8.03D, 3.4F),
      Arguments.of(12.35D, 3.4F, 4.32D, 8.03D, 3.4F),
      Arguments.of(12.35D, 3.4F, 4.32D, 8.03D, 3.4F),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("subtractConstantTestParams")
  public void subtractConstantTest(Double minuendValue,
    Float minuendUncertainty, Double subtrahend, Double resultValue,
    Float resultUncertainty) {

    DoubleWithUncertainty minuend = new DoubleWithUncertainty(minuendValue,
      minuendUncertainty);

    DoubleWithUncertainty result = minuend.subtract(subtrahend);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> addTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0F, 0D, 0F),
      Arguments.of(1D, 0F, 1D, 0F, 2D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0.4F, 16.67D, 3.4234F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0F, 16.67D, 3.4F),
      Arguments.of(12.35D, 3.4F, 4.32D, Float.NaN, 16.67D, Float.NaN),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, 0F, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("addTestParams")
  public void addTest(Double augendValue, Float augendUncertainty,
    Double addendValue, Float addendUncertainty, Double resultValue,
    Float resultUncertainty) {

    DoubleWithUncertainty augend = new DoubleWithUncertainty(augendValue,
      augendUncertainty);
    DoubleWithUncertainty addend = new DoubleWithUncertainty(addendValue,
      addendUncertainty);

    DoubleWithUncertainty result = augend.add(addend);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> addConstantTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0D, 0F),
      Arguments.of(1D, 0F, 1D, 2D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 16.67D, 3.4F),
      Arguments.of(12.35D, 3.4F, 4.32D, 16.67D, 3.4F),
      Arguments.of(12.35D, 3.4F, 4.32D, 16.67D, 3.4F),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("addConstantTestParams")
  public void addConstantTest(Double augendValue, Float augendUncertainty,
    Double addend, Double resultValue, Float resultUncertainty) {

    DoubleWithUncertainty augend = new DoubleWithUncertainty(augendValue,
      augendUncertainty);

    DoubleWithUncertainty result = augend.add(addend);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> multiplyTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0F, 0D, 0F),
      Arguments.of(1D, 0F, 1D, 0F, 1D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0.4F, 53.352D, 15.4965F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0F, 53.352D, 14.688F),
      Arguments.of(12.35D, 3.4F, 4.32D, Float.NaN, 53.352D, Float.NaN),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, 0F, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("multiplyTestParams")
  public void multiplyTest(Double multiplicandValue,
    Float multiplicandUncertainty, Double multiplierValue,
    Float multiplierUncertainty, Double resultValue, Float resultUncertainty) {

    DoubleWithUncertainty multiplicand = new DoubleWithUncertainty(
      multiplicandValue, multiplicandUncertainty);
    DoubleWithUncertainty multiplier = new DoubleWithUncertainty(
      multiplierValue, multiplierUncertainty);

    DoubleWithUncertainty result = multiplicand.multiply(multiplier);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> multiplyConstantTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0D, 0F),
      Arguments.of(1D, 0F, 1D, 1D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 53.352D, 14.688F),
      Arguments.of(12.35D, 3.4F, 4.32D, 53.352D, 14.688F),
      Arguments.of(12.35D, 3.4F, 4.32D, 53.352D, 14.688F),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("multiplyConstantTestParams")
  public void multiplyConstantTest(Double multiplicandValue,
    Float multiplicandUncertainty, Double multiplier, Double resultValue,
    Float resultUncertainty) {

    DoubleWithUncertainty multiplicand = new DoubleWithUncertainty(
      multiplicandValue, multiplicandUncertainty);

    DoubleWithUncertainty result = multiplicand.multiply(multiplier);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> divideTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, 0F, Double.NaN, Float.NaN),
      Arguments.of(1D, 0F, 1D, 0F, 1D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0.4F, 2.8588D, 0.8304F),
      Arguments.of(12.35D, 3.4F, 4.32D, 0F, 2.8588D, 0.7870F),
      Arguments.of(12.35D, 3.4F, 4.32D, Float.NaN, 2.8588D, Float.NaN),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, 0F, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("divideTestParams")
  public void divideTest(Double dividendValue, Float dividendUncertainty,
    Double divisorValue, Float divisorUncertainty, Double resultValue,
    Float resultUncertainty) {

    DoubleWithUncertainty dividend = new DoubleWithUncertainty(dividendValue,
      dividendUncertainty);
    DoubleWithUncertainty divisor = new DoubleWithUncertainty(divisorValue,
      divisorUncertainty);

    DoubleWithUncertainty result = dividend.divide(divisor);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> divideConstantTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 0D, Double.NaN, Float.NaN),
      Arguments.of(1D, 0F, 1D, 1D, 0F),
      Arguments.of(12.35D, 3.4F, 4.32D, 2.8588D, 0.7870F),
      Arguments.of(12.35D, 3.4F, 4.32D, 2.8588D, 0.7870F),
      Arguments.of(12.35D, 3.4F, 4.32D, 2.8588D, 0.7870F),
      Arguments.of(Double.NaN, Float.NaN, 4.32D, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("divideConstantTestParams")
  public void divideConstantTest(Double dividendValue,
    Float dividendUncertainty, Double divisor, Double resultValue,
    Float resultUncertainty) {

    DoubleWithUncertainty dividend = new DoubleWithUncertainty(dividendValue,
      dividendUncertainty);

    DoubleWithUncertainty result = dividend.divide(divisor);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> meanTestParams() {
    return Stream.of(
      Arguments.of(List.of(12.5D, 8.3D, 1.1D), List.of(3.2F, 1.1F, 0.2F), 7.3D,
        1.1299F),
      Arguments.of(List.of(12.5D, Double.NaN, 1.1D),
        List.of(3.2F, Float.NaN, 0.2F), 6.8D, 1.6031F),
      Arguments.of(List.of(12.5D, 8.3D, 1.1D), List.of(3.2F, Float.NaN, 0.2F),
        7.3D, Float.NaN),
      Arguments.of(List.of(12.5D), List.of(3.2F), 12.5D, 3.2F), Arguments
        .of(List.of(Double.NaN), List.of(Float.NaN), Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("meanTestParams")
  public void meanTest(List<Double> values, List<Float> uncertainties,
    Double resultValue, Float resultUncertainty) {

    List<DoubleWithUncertainty> DWUs = new ArrayList<DoubleWithUncertainty>();

    // Just a check to make sure the test input is usable
    if (values.size() != uncertainties.size()) {
      throw new IllegalArgumentException(
        "Values and Uncertainties must be the same size");
    }

    for (int i = 0; i < values.size(); i++) {
      DWUs.add(new DoubleWithUncertainty(values.get(i), uncertainties.get(i)));
    }

    DoubleWithUncertaintyAssert.assertThat(DoubleWithUncertainty.mean(DWUs))
      .matches(resultValue, resultUncertainty);
  }

  @Test
  public void meanWithNullTest() {
    List<DoubleWithUncertainty> DWUs = new ArrayList<DoubleWithUncertainty>();

    DWUs.add(new DoubleWithUncertainty(12.5D, 3.2F));
    DWUs.add(null);
    DWUs.add(new DoubleWithUncertainty(1.1D, 0.2F));

    DoubleWithUncertaintyAssert.assertThat(DoubleWithUncertainty.mean(DWUs))
      .matches(6.8D, 1.6031F);
  }

  private static Stream<Arguments> logTestParams() {
    return Stream.of(Arguments.of(0D, 0F, Double.NaN, Float.NaN),
      Arguments.of(1D, 0F, 0D, 0F),
      Arguments.of(12.35D, 3.4F, 2.5137D, 0.2753F),
      Arguments.of(Double.NaN, Float.NaN, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("logTestParams")
  public void logTest(Double inputValue, Float inputUncertainty,
    Double resultValue, Float resultUncertainty) {

    DoubleWithUncertainty input = new DoubleWithUncertainty(inputValue,
      inputUncertainty);

    DoubleWithUncertainty result = input.log();

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> expTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 1D, 0F),
      Arguments.of(1D, 0F, 2.7183D, 0F),
      Arguments.of(0.1235D, 0.034F, 1.1314D, 0.0385F),
      Arguments.of(Double.NaN, Float.NaN, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("expTestParams")
  public void expTest(Double inputValue, Float inputUncertainty,
    Double resultValue, Float resultUncertainty) {

    DoubleWithUncertainty input = new DoubleWithUncertainty(inputValue,
      inputUncertainty);

    DoubleWithUncertainty result = input.exp();

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> log10TestParams() {
    return Stream.of(Arguments.of(0D, 0F, Double.NaN, Float.NaN),
      Arguments.of(1D, 0F, 0D, 0F),
      Arguments.of(12.35D, 3.4F, 1.0917D, 0.1196F),
      Arguments.of(Double.NaN, Float.NaN, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("log10TestParams")
  public void log10Test(Double inputValue, Float inputUncertainty,
    Double resultValue, Float resultUncertainty) {

    DoubleWithUncertainty input = new DoubleWithUncertainty(inputValue,
      inputUncertainty);

    DoubleWithUncertainty result = input.log10();

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> powTestParams() {
    return Stream.of(Arguments.of(0D, 0F, 2D, 0D, 0F),
      Arguments.of(1D, 0F, 2D, 1D, 0F), Arguments.of(12.35D, 3.4F, 0D, 1D, 0F),
      Arguments.of(12.35D, 3.4F, 2D, 152.5225D, 83.98F),
      Arguments.of(12.35D, 3.4F, 3D, 1883.6529D, 1555.7295F),
      Arguments.of(12.35D, 3.4F, Double.NaN, Double.NaN, Float.NaN),
      Arguments.of(Double.NaN, Float.NaN, 2D, Double.NaN, Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("powTestParams")
  public void powTest(Double inputValue, Float inputUncertainty,
    Double exponent, Double resultValue, Float resultUncertainty) {

    DoubleWithUncertainty input = new DoubleWithUncertainty(inputValue,
      inputUncertainty);

    DoubleWithUncertainty result = input.pow(exponent);

    DoubleWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> isNaNTestParams() {
    return Stream.of(Arguments.of(12.35D, 3.4F, false),
      Arguments.of(12.35D, Float.NaN, false), Arguments.of(0D, 0F, false),
      Arguments.of(Double.NaN, 0F, true));
  }

  @ParameterizedTest
  @MethodSource("isNaNTestParams")
  public void isNaNTest(Double value, Float uncertainty, boolean nan) {

    DoubleWithUncertainty object = new DoubleWithUncertainty(value,
      uncertainty);
    assertEquals(nan, object.isNaN());

  }

  @ParameterizedTest
  @MethodSource("isNaNTestParams")
  public void isNaNStaticTest(Double value, Float uncertainty, boolean nan) {

    DoubleWithUncertainty object = new DoubleWithUncertainty(value,
      uncertainty);
    assertEquals(nan, DoubleWithUncertainty.isNaN(object));

  }
}
