package org.apache.pinot.thirdeye.detection.v2.plan;

import static org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory.V2_DETECTION_PLAN_PACKAGE_NAME;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Modifier;
import java.util.Set;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.reflections.Reflections;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlanNodeFactoryTest {

  @Test
  public void testNoDuplicatedTypeKeyRegistration() {
    PlanNodeFactory planNodeFactory = new PlanNodeFactory(
        mock(DataSourceCache.class));
    int numPlanNodes = planNodeFactory.getAllPlanNodes().size();
    Reflections reflections = new Reflections(V2_DETECTION_PLAN_PACKAGE_NAME);
    Set<Class<? extends PlanNode>> planNodeClasses = reflections.getSubTypesOf(PlanNode.class);
    int expectPlanNodeClassesNum = planNodeClasses.size();
    for (Class<? extends PlanNode> planNodeClass : planNodeClasses) {
      if (Modifier.isAbstract(planNodeClass.getModifiers())) {
        expectPlanNodeClassesNum--;
      }
    }
    Assert.assertEquals(numPlanNodes, expectPlanNodeClassesNum);
  }
}
