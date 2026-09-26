package org.apache.pinot.function.jev;

import java.util.Collections;
import java.util.List;
import org.apache.pinot.spi.annotations.ScalarFunction;

/** Pinot scalar functions. Several predicates over one row share one Jev request. */
public final class JevFunctions {
  private JevFunctions() { }

  private static final class Holder {
    private static final JevClient CLIENT = JevClient.fromEnvironment();
  }

  @ScalarFunction
  public static int jevAll(String value, String conditionsJson) {
    if (value == null || conditionsJson == null) return 0;
    List<String> conditions = JevClient.conditions(conditionsJson);
    for (double probability : Holder.CLIENT.evaluate(value, conditions)) {
      if (probability < 0.5) return 0;
    }
    return 1;
  }

  @ScalarFunction
  public static int jevAny(String value, String conditionsJson) {
    if (value == null || conditionsJson == null) return 0;
    List<String> conditions = JevClient.conditions(conditionsJson);
    for (double probability : Holder.CLIENT.evaluate(value, conditions)) {
      if (probability >= 0.5) return 1;
    }
    return 0;
  }

  @ScalarFunction
  public static double jevProbability(String value, String condition) {
    if (value == null || condition == null) return 0;
    return Holder.CLIENT.evaluate(value, Collections.singletonList(condition))[0];
  }
}
