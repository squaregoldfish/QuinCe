package uk.ac.exeter.QuinCe.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

public class BigDecimalWithUncertaintyTest extends BaseTest {

  @Test
  public void goodConstructorTest() {
    BigDecimalWithUncertainty value = new BigDecimalWithUncertainty(
      new BigDecimal(24D), 2F);
    BigDecimalWithUncertaintyAssert.assertThat(value)
      .matches(new BigDecimal(24D), 2F);
  }

  @Test
  public void nullValueTest() {
    assertThrows(NullPointerException.class, () -> {
      new BigDecimalWithUncertainty(null, 2F);
    });
  }

  @Test
  public void nanUncertaintyTest() {
    BigDecimalWithUncertainty value = new BigDecimalWithUncertainty(
      new BigDecimal(24D), Float.NaN);
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void nullUncertaintyTest() {
    BigDecimalWithUncertainty value = new BigDecimalWithUncertainty(
      new BigDecimal(24D), null);
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void noUncertaintyTest() {
    BigDecimalWithUncertainty value = new BigDecimalWithUncertainty(
      new BigDecimal(24D));
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void fromDoubleWithUncertaintyTest() {
    DoubleWithUncertainty dwu = new DoubleWithUncertainty(12.35D, 3.4F);
    BigDecimalWithUncertainty bdwu = new BigDecimalWithUncertainty(dwu);
    BigDecimalWithUncertaintyAssert.assertThat(bdwu)
      .matches(new BigDecimal(12.35D), 3.4F);
  }

  @ParameterizedTest
  @CsvSource({ "1F, true", "NaN, false" })
  public void hasUncertaintyTest(Float uncertainty, boolean hasUncertainty) {
    BigDecimalWithUncertainty value = new BigDecimalWithUncertainty(
      new BigDecimal(24D), uncertainty);
    assertEquals(hasUncertainty, value.hasUncertainty());
  }

  @Test
  public void integerValueConstructorTest() {
    BigDecimalWithUncertainty value = new BigDecimalWithUncertainty(123);
    BigDecimalWithUncertaintyAssert.assertThat(value)
      .matches(new BigDecimal(123D), Float.NaN);
  }

  @Test
  public void staticZeroTest() {
    BigDecimalWithUncertaintyAssert.assertThat(BigDecimalWithUncertainty.ZERO)
      .matches(new BigDecimal(0D), 0F);
  }

  @Test
  public void staticOneTest() {
    BigDecimalWithUncertaintyAssert.assertThat(BigDecimalWithUncertainty.ONE)
      .matches(new BigDecimal(1D), 0F);
  }

  private static Stream<Arguments> subtractTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(0D), 0F,
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D), 0F,
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0.4F,
        new BigDecimal(8.03D), 3.4234F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0F,
        new BigDecimal(8.03D), 3.4F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        Float.NaN, new BigDecimal(8.03D), Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("subtractTestParams")
  public void subtractTest(BigDecimal minuendValue, Float minuendUncertainty,
    BigDecimal subtrahendValue, Float subtrahendUncertainty,
    BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty minuend = new BigDecimalWithUncertainty(
      minuendValue, minuendUncertainty);
    BigDecimalWithUncertainty subtrahend = new BigDecimalWithUncertainty(
      subtrahendValue, subtrahendUncertainty);

    BigDecimalWithUncertainty result = minuend.subtract(subtrahend);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> subtractConstantTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(0D),
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D),
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(8.03D), 3.4F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(8.03D), 3.4F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(8.03D), 3.4F));
  }

  @ParameterizedTest
  @MethodSource("subtractConstantTestParams")
  public void subtractConstantTest(BigDecimal minuendValue,
    Float minuendUncertainty, BigDecimal subtrahend, BigDecimal resultValue,
    Float resultUncertainty) {

    BigDecimalWithUncertainty minuend = new BigDecimalWithUncertainty(
      minuendValue, minuendUncertainty);

    BigDecimalWithUncertainty result = minuend.subtract(subtrahend);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> addTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(0D), 0F,
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D), 0F,
        new BigDecimal(2D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0.4F,
        new BigDecimal(16.67D), 3.4234F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0F,
        new BigDecimal(16.67D), 3.4F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        Float.NaN, new BigDecimal(16.67D), Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("addTestParams")
  public void addTest(BigDecimal augendValue, Float augendUncertainty,
    BigDecimal addendValue, Float addendUncertainty, BigDecimal resultValue,
    Float resultUncertainty) {

    BigDecimalWithUncertainty augend = new BigDecimalWithUncertainty(
      augendValue, augendUncertainty);
    BigDecimalWithUncertainty addend = new BigDecimalWithUncertainty(
      addendValue, addendUncertainty);

    BigDecimalWithUncertainty result = augend.add(addend);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> addConstantTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(0D),
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D),
        new BigDecimal(2D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(16.67D), 3.4F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(16.67D), 3.4F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(16.67D), 3.4F));
  }

  @ParameterizedTest
  @MethodSource("addConstantTestParams")
  public void addConstantTest(BigDecimal augendValue, Float augendUncertainty,
    BigDecimal addend, BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty augend = new BigDecimalWithUncertainty(
      augendValue, augendUncertainty);

    BigDecimalWithUncertainty result = augend.add(addend);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> multiplyTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(0D), 0F,
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D), 0F,
        new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0.4F,
        new BigDecimal(53.352D), 15.4965F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0F,
        new BigDecimal(53.352D), 14.688F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        Float.NaN, new BigDecimal(53.352D), Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("multiplyTestParams")
  public void multiplyTest(BigDecimal multiplicandValue,
    Float multiplicandUncertainty, BigDecimal multiplierValue,
    Float multiplierUncertainty, BigDecimal resultValue,
    Float resultUncertainty) {

    BigDecimalWithUncertainty multiplicand = new BigDecimalWithUncertainty(
      multiplicandValue, multiplicandUncertainty);
    BigDecimalWithUncertainty multiplier = new BigDecimalWithUncertainty(
      multiplierValue, multiplierUncertainty);

    BigDecimalWithUncertainty result = multiplicand.multiply(multiplier);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> multiplyConstantTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(0D),
        new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D),
        new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(53.352D), 14.688F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(53.352D), 14.688F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(53.352D), 14.688F));
  }

  @ParameterizedTest
  @MethodSource("multiplyConstantTestParams")
  public void multiplyConstantTest(BigDecimal multiplicandValue,
    Float multiplicandUncertainty, BigDecimal multiplier,
    BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty multiplicand = new BigDecimalWithUncertainty(
      multiplicandValue, multiplicandUncertainty);

    BigDecimalWithUncertainty result = multiplicand.multiply(multiplier);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  @Test
  public void divideByZeroTest() {
    BigDecimalWithUncertainty dividend = new BigDecimalWithUncertainty(1D, 0F);

    assertThrows(ArithmeticException.class, () -> {
      dividend.divide(BigDecimalWithUncertainty.ZERO);
    });
  }

  private static Stream<Arguments> divideTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D), 0F,
        new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0.4F,
        new BigDecimal(2.8588D), 0.8304F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D), 0F,
        new BigDecimal(2.8588D), 0.7870F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        Float.NaN, new BigDecimal(2.8588D), Float.NaN));
  }

  @ParameterizedTest
  @MethodSource("divideTestParams")
  public void divideTest(BigDecimal dividendValue, Float dividendUncertainty,
    BigDecimal divisorValue, Float divisorUncertainty, BigDecimal resultValue,
    Float resultUncertainty) {

    BigDecimalWithUncertainty dividend = new BigDecimalWithUncertainty(
      dividendValue, dividendUncertainty);
    BigDecimalWithUncertainty divisor = new BigDecimalWithUncertainty(
      divisorValue, divisorUncertainty);

    BigDecimalWithUncertainty result = dividend.divide(divisor);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> divideConstantTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(1D),
        new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(2.8588D), 0.7870F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(2.8588D), 0.7870F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, new BigDecimal(4.32D),
        new BigDecimal(2.8588D), 0.7870F));
  }

  @ParameterizedTest
  @MethodSource("divideConstantTestParams")
  public void divideConstantTest(BigDecimal dividendValue,
    Float dividendUncertainty, BigDecimal divisor, BigDecimal resultValue,
    Float resultUncertainty) {

    BigDecimalWithUncertainty dividend = new BigDecimalWithUncertainty(
      dividendValue, dividendUncertainty);

    BigDecimalWithUncertainty result = dividend.divide(divisor);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> meanTestParams() {
    return Stream.of(
      Arguments.of(
        List.of(new BigDecimal(12.5D), new BigDecimal(8.3D),
          new BigDecimal(1.1D)),
        List.of(3.2F, 1.1F, 0.2F), new BigDecimal(7.3D), 1.1299F),
      Arguments.of(
        List.of(new BigDecimal(12.5D), new BigDecimal(8.3D),
          new BigDecimal(1.1D)),
        List.of(3.2F, Float.NaN, 0.2F), new BigDecimal(7.3D), Float.NaN),
      Arguments.of(List.of(new BigDecimal(12.5D)), List.of(3.2F),
        new BigDecimal(12.5D), 3.2F));
  }

  @ParameterizedTest
  @MethodSource("meanTestParams")
  public void meanTest(List<BigDecimal> values, List<Float> uncertainties,
    BigDecimal resultValue, Float resultUncertainty) {

    List<BigDecimalWithUncertainty> DWUs = new ArrayList<BigDecimalWithUncertainty>();

    // Just a check to make sure the test input is usable
    if (values.size() != uncertainties.size()) {
      throw new IllegalArgumentException(
        "Values and Uncertainties must be the same size");
    }

    for (int i = 0; i < values.size(); i++) {
      DWUs.add(
        new BigDecimalWithUncertainty(values.get(i), uncertainties.get(i)));
    }

    BigDecimalWithUncertaintyAssert
      .assertThat(BigDecimalWithUncertainty.mean(DWUs))
      .matches(resultValue, resultUncertainty);
  }

  @Test
  public void meanWithNullTest() {
    List<BigDecimalWithUncertainty> DWUs = new ArrayList<BigDecimalWithUncertainty>();

    DWUs.add(new BigDecimalWithUncertainty(new BigDecimal(12.5D), 3.2F));
    DWUs.add(null);
    DWUs.add(new BigDecimalWithUncertainty(new BigDecimal(1D), 0.2F));

    BigDecimalWithUncertaintyAssert
      .assertThat(BigDecimalWithUncertainty.mean(DWUs))
      .matches(new BigDecimal(6.75D), 1.6031F);
  }

  @Test
  public void meanWithOnlyNullTest() {
    List<BigDecimalWithUncertainty> DWUs = new ArrayList<BigDecimalWithUncertainty>();

    DWUs.add(null);

    assertThrows(ArithmeticException.class, () -> {
      BigDecimalWithUncertainty.mean(DWUs);
    });
  }

  private static Stream<Arguments> logTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(0D), 0F), Arguments
        .of(new BigDecimal(12.35D), 3.4F, new BigDecimal(2.5137D), 0.2753F));
  }

  @ParameterizedTest
  @MethodSource("logTestParams")
  public void logTest(BigDecimal inputValue, Float inputUncertainty,
    BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty input = new BigDecimalWithUncertainty(inputValue,
      inputUncertainty);

    BigDecimalWithUncertainty result = input.log();

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> expTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(2.7183D), 0F),
      Arguments.of(new BigDecimal(0.1235D), 0.034F, new BigDecimal(1.1314D),
        0.0385F));
  }

  @ParameterizedTest
  @MethodSource("expTestParams")
  public void expTest(BigDecimal inputValue, Float inputUncertainty,
    BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty input = new BigDecimalWithUncertainty(inputValue,
      inputUncertainty);

    BigDecimalWithUncertainty result = input.exp();

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> log10TestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(1D), 0F, new BigDecimal(0D), 0F), Arguments
        .of(new BigDecimal(12.35D), 3.4F, new BigDecimal(1.0917D), 0.1196F));
  }

  @ParameterizedTest
  @MethodSource("log10TestParams")
  public void log10Test(BigDecimal inputValue, Float inputUncertainty,
    BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty input = new BigDecimalWithUncertainty(inputValue,
      inputUncertainty);

    BigDecimalWithUncertainty result = input.log10();

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }

  private static Stream<Arguments> powTestParams() {
    return Stream.of(
      Arguments.of(new BigDecimal(0D), 0F, 2, new BigDecimal(0D), 0F),
      Arguments.of(new BigDecimal(1D), 0F, 2, new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, 0, new BigDecimal(1D), 0F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, 2, new BigDecimal(152.5225D),
        83.98F),
      Arguments.of(new BigDecimal(12.35D), 3.4F, 3, new BigDecimal(1883.6529D),
        1555.7295F));
  }

  @ParameterizedTest
  @MethodSource("powTestParams")
  public void powTest(BigDecimal inputValue, Float inputUncertainty,
    int exponent, BigDecimal resultValue, Float resultUncertainty) {

    BigDecimalWithUncertainty input = new BigDecimalWithUncertainty(inputValue,
      inputUncertainty);

    BigDecimalWithUncertainty result = input.pow(exponent);

    BigDecimalWithUncertaintyAssert.assertThat(result).matches(resultValue,
      resultUncertainty);
  }
}
