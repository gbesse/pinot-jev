package org.apache.pinot.function.jev;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class JevThresholdTest {
 @Test void explicitThresholdChangesGate(){double[] values={.91,.84};assertEquals(1,JevFunctions.aggregateAt(values,.8,true));assertEquals(0,JevFunctions.aggregateAt(values,.9,true));assertEquals(1,JevFunctions.aggregateAt(values,.9,false));}
 @Test void invalidThresholdRejectedBeforeClientInitialization(){for(double threshold:new double[]{Double.NaN,Double.POSITIVE_INFINITY,.49,1.1})assertThrows(IllegalArgumentException.class,()->JevFunctions.jevAllAt("x","[\"q\"]",threshold));}
 @Test void nullDoesNotInvokeClient(){assertEquals(0,JevFunctions.jevAnyAt(null,"[\"q\"]",.8));}
}
