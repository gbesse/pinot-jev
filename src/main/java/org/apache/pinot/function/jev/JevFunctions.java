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

  static double threshold(double value) {
    if (!Double.isFinite(value) || value < 0.5 || value > 1) throw new IllegalArgumentException("Seuil invalide / Invalid threshold / Umbral invalido");
    return value;
  }
  static int aggregateAt(double[] probabilities, double value, boolean all) {
    threshold(value);
    for (double p : probabilities) {
      if (all && p < value) return 0;
      if (!all && p >= value) return 1;
    }
    return all ? 1 : 0;
  }
  @ScalarFunction
  public static int jevAllAt(String value, String conditionsJson, double minimumProbability) {
    threshold(minimumProbability);
    if (value == null || conditionsJson == null) return 0;
    return aggregateAt(Holder.CLIENT.evaluate(value, JevClient.conditions(conditionsJson)), minimumProbability, true);
  }
  @ScalarFunction
  public static int jevAnyAt(String value, String conditionsJson, double minimumProbability) {
    threshold(minimumProbability);
    if (value == null || conditionsJson == null) return 0;
    return aggregateAt(Holder.CLIENT.evaluate(value, JevClient.conditions(conditionsJson)), minimumProbability, false);
  }

  static int countAt(double[] probabilities,double minimum){
    threshold(minimum);int count=0;
    for(double p:probabilities){if(!Double.isFinite(p)||p<0||p>1)throw new IllegalArgumentException("Probabilite invalide / Invalid probability / Probabilidad invalida");if(p>=minimum)count++;}
    return count;
  }
  @ScalarFunction
  public static int jevCountAt(String value,String conditionsJson,double minimumProbability){
    threshold(minimumProbability);if(value==null||conditionsJson==null)return 0;
    return countAt(Holder.CLIENT.evaluate(value,JevClient.conditions(conditionsJson)),minimumProbability);
  }
}
