package org.apache.pinot.function.jev;
import static org.junit.jupiter.api.Assertions.*;import org.junit.jupiter.api.Test;
class JevCountTest{
 @Test void semanticConditionCountsAtExplicitThreshold(){assertEquals(2,JevFunctions.countAt(new double[]{.91,.84},.8));assertEquals(1,JevFunctions.countAt(new double[]{.91,.84},.9));assertEquals(0,JevFunctions.countAt(new double[]{},.8));}
 @Test void nullAndInvalidThresholdDoNotInitializeProvider(){assertEquals(0,JevFunctions.jevCountAt(null,"[\"q\"]",.8));assertThrows(IllegalArgumentException.class,()->JevFunctions.jevCountAt("x","[\"q\"]",Double.NaN));}
 @Test void nonfiniteProbabilityCannotInflateCount(){assertThrows(IllegalArgumentException.class,()->JevFunctions.countAt(new double[]{Double.NaN},.8));}
}
