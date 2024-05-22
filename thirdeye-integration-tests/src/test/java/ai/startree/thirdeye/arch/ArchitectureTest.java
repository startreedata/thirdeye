/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.arch;

import static com.tngtech.archunit.base.DescribedPredicate.doNot;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.containAnyMethodsThat;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import ai.startree.thirdeye.PluginLoader;
import ai.startree.thirdeye.ThirdEyeServerModule;
import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.alert.EvaluationContextProcessor;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.NamespaceResolver;
import ai.startree.thirdeye.datalayer.DataSourceBuilder;
import ai.startree.thirdeye.datalayer.DatabaseClient;
import ai.startree.thirdeye.datalayer.DatabaseOrm;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datalayer.bao.AlertManagerImpl;
import ai.startree.thirdeye.datalayer.bao.TaskManagerImpl;
import ai.startree.thirdeye.datalayer.core.EnumerationItemDeleter;
import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.datalayer.dao.TaskDao;
import ai.startree.thirdeye.datasource.DataSourceOnboarder;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.detectionpipeline.components.EventDataFetcher;
import ai.startree.thirdeye.detectionpipeline.components.GenericDataFetcher;
import ai.startree.thirdeye.detectionpipeline.persistence.CachedDatasetConfigManager;
import ai.startree.thirdeye.events.HolidayEventProvider;
import ai.startree.thirdeye.healthcheck.DatabaseHealthCheck;
import ai.startree.thirdeye.notification.NotificationReportBuilder;
import ai.startree.thirdeye.notification.NotificationTaskFilter;
import ai.startree.thirdeye.notification.NotificationTaskPostProcessor;
import ai.startree.thirdeye.plugins.postprocessor.AnomalyMergerPostProcessor;
import ai.startree.thirdeye.plugins.postprocessor.ColdStartPostProcessor;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.resources.CrudResource;
import ai.startree.thirdeye.scheduler.DetectionCronScheduler;
import ai.startree.thirdeye.scheduler.SchedulerService;
import ai.startree.thirdeye.scheduler.SubscriptionCronScheduler;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoader;
import ai.startree.thirdeye.scheduler.events.MockEventsLoader;
import ai.startree.thirdeye.scheduler.job.DetectionPipelineJob;
import ai.startree.thirdeye.service.CrudService;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer.ThirdEyeAuthorizerFactory;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.detection.health.DetectionHealth;
import ai.startree.thirdeye.worker.task.TaskDriver;
import ai.startree.thirdeye.worker.task.TaskDriverRunnable;
import ai.startree.thirdeye.worker.task.runner.DetectionPipelineTaskRunner;
import ai.startree.thirdeye.worker.task.runner.NotificationTaskRunner;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.elements.MethodsShouldConjunction;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import javax.sql.DataSource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ArchitectureTest {

  public static final DescribedPredicate<JavaClass> ARE_RESOURCE_CLASSES = are(
      assignableTo(CrudResource.class)).or(resideInAnyPackage("ai.startree.thirdeye.resources"))
      .or(are(annotatedWith(Path.class)))
      .or(annotatedWith(Produces.class))
      .or(annotatedWith(SecurityScheme.class))
      .or(annotatedWith(OpenAPIDefinition.class))
      .or(containAnyMethodsThat(annotatedWith(DELETE.class)))
      .or(containAnyMethodsThat(annotatedWith(POST.class)))
      .or(containAnyMethodsThat(annotatedWith(PUT.class)))
      .or(containAnyMethodsThat(annotatedWith(PATCH.class)))
      .or(containAnyMethodsThat(annotatedWith(OPTIONS.class)))
      .or(containAnyMethodsThat(annotatedWith(HEAD.class)))
      .or(containAnyMethodsThat(annotatedWith(GET.class)));

  public static final DescribedPredicate<JavaClass> ARE_SERVICE_CLASSES = are(
      assignableTo(CrudService.class)).or(resideInAnyPackage("ai.startree.thirdeye.service.."));

  // any class that can perform db writes and does not apply the authorization layer
  public static final DescribedPredicate<JavaClass> ARE_NON_SECURED_DB_LAYER_CLASSES = or(
      assignableTo(AbstractManager.class),
      assignableTo(GenericPojoDao.class),
      assignableTo(TaskDao.class),
      assignableTo(DatabaseOrm.class),
      assignableTo(DatabaseClient.class));

  // no other classes should use 
  public static final DescribedPredicate<JavaClass> ARE_ALLOWED_DATASOURCE_USER = belongToAnyOf(
      // legitimate user
      DatabaseClient.class,
      // legitimate initialization user 
      DataSourceBuilder.class,
      // uses a connection to init EntityMappingHolder - TODO CYRIL - should be refactored to be removed - should go away when ORM is rewritten 
      ThirdEyePersistenceModule.class
  );

  public JavaClasses thirdeyeClasses;

  @BeforeClass
  public void loadClasses() {
    thirdeyeClasses = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new IgnoreImportedTests())
        .importPackages("ai.startree");
  }

  @Test
  public void testNoUnknownUserOfDatasource() {
    final ArchRule rule = noClasses()
        .that(doNot(ARE_ALLOWED_DATASOURCE_USER))
        .should()
        .accessClassesThat()
        .areAssignableTo(DataSource.class);
    rule.check(thirdeyeClasses);
  }

  @Test
  public void testNoUnknownUserOfNonSecuredDbLayerClasses() {
    // FIXME CYRIL  apart from services - these are the class allowed to use non secured db layers
    // review, and enforce namespace isolation where necessary
    //  or all methods should require an identity
    final Class[] NON_SECURED_DB_LAYER_USERS_WHITELIST = {NotificationTaskRunner.class,
        DetectionPipelineTaskRunner.class,
        TaskDriverRunnable.class,
        TaskDriver.class,
        DetectionHealth.class,
        DetectionPipelineJob.class,
        MockEventsLoader.class,
        HolidayEventsLoader.class,
        SubscriptionCronScheduler.class,
        SchedulerService.class,
        DetectionCronScheduler.class,
        RcaInfoFetcher.class,
        ColdStartPostProcessor.class,
        AnomalyMergerPostProcessor.class,
        NotificationTaskPostProcessor.class,
        NotificationTaskFilter.class,
        NotificationReportBuilder.class,
        DatabaseHealthCheck.class,
        HolidayEventProvider.class,
        CachedDatasetConfigManager.class,
        GenericDataFetcher.class,
        EventDataFetcher.class,
        PlanExecutor.class,
        DataSourceCache.class,
        EnumerationItemMaintainer.class,
        EnumerationItemDeleter.class,
        TaskManagerImpl.class,
        AlertManagerImpl.class,
        DataSourceOnboarder.class, // OK - REVIEWED ON APRIL 12 2024
        NamespaceResolver.class,
        EvaluationContextProcessor.class,
        AlertTemplateRenderer.class,
        AuthorizationManager.class // OK - REVIEW ON MAY 6 2024
    };
    final ArchRule rule = noClasses().that(
            doNot(
                ARE_NON_SECURED_DB_LAYER_CLASSES
                    .or(ARE_SERVICE_CLASSES)
                    .or(belongToAnyOf(NON_SECURED_DB_LAYER_USERS_WHITELIST))
                    // operators in enterprise distribution may use non secured db layer classes
                    .or(assignableTo(Operator.class))
            ))
        .should()
        .accessClassesThat(ARE_NON_SECURED_DB_LAYER_CLASSES);
    rule.check(thirdeyeClasses);
  }

  // FIXME CONSIDER LOOKING AT ALL USERS OF ARE_NON_SECURED_DB_LAYER_CLASSES INSTEAD ? 
  @Test
  public void testAllPublicMethodsOfServicesRequireAnIdentity() {
    final MethodsShouldConjunction rule = methods().that()
        .areDeclaredInClassesThat(ARE_SERVICE_CLASSES.and(IsTopLevelClass.IS_TOP_LEVEL_CLASS))
        .and()
        .arePublic()
        .and()
        .areNotStatic()
        .should(HavePrincipalAsFirstParam.HAVE_PRINCIPAL_AS_FIRST_PARAM);
    rule.check(thirdeyeClasses);
  }

  @Test
  public void testAllServicesDependOnAndCallTheAuthorizer() {
    final ArchRule rule = classes().that(
            ARE_SERVICE_CLASSES.and(IsTopLevelClass.IS_TOP_LEVEL_CLASS))
        .should()
        .dependOnClassesThat(assignableTo(AuthorizationManager.class));
    rule.check(thirdeyeClasses);
  }

  @Test
  public void testResourcesCannotUseNonSecuredDBLayers() {
    final ArchRule rule = noClasses().that(ARE_RESOURCE_CLASSES)
        .should()
        .dependOnClassesThat(ARE_NON_SECURED_DB_LAYER_CLASSES);
    rule.check(thirdeyeClasses);
  }

  @Test
  public void testResourcesDoNotPerformAuthorization() {
    // no resources should perform authorization - they are only responsible for authentication
    final ArchRule rule = noClasses().that(ARE_RESOURCE_CLASSES)
        .should()
        .dependOnClassesThat()
        .areAssignableTo(AuthorizationManager.class);
    rule.check(thirdeyeClasses);
  }

  @Test
  public void testNoUnknownUserOfThirdEyeAuthorizer() {
    // services that want to perform authorization should use the AuthorizationManager and never user the lower level ThirdEyeAuthorizer
    // here we ensure there are no unknown users of the lower levels
    final ArchRule rule = noClasses().that()
        .doNotImplement(ThirdEyeAuthorizer.class)
        .and()
        .doNotImplement(ThirdEyeAuthorizerFactory.class)
        .and()
        .doNotBelongToAnyOf(AuthorizationManager.class, ThirdEyeServerModule.class,
            PluginLoader.class)
        .should()
        .accessClassesThat()
        .implement(ThirdEyeAuthorizer.class);

    rule.check(thirdeyeClasses);
  }

  private static class HavePrincipalAsFirstParam extends ArchCondition<JavaMethod> {

    private static final HavePrincipalAsFirstParam HAVE_PRINCIPAL_AS_FIRST_PARAM = new HavePrincipalAsFirstParam();

    private HavePrincipalAsFirstParam() {
      super(String.format("Ensure a method has a %s as first parameter", ThirdEyePrincipal.class));
    }

    @Override
    public void check(final JavaMethod item, final ConditionEvents events) {
      if (item.getParameters().isEmpty()) {
        events.add(SimpleConditionEvent.violated(item, String.format(
            "The method %s has no parameter. " + "It should have at least one parameter of type %s",
            item.getFullName(), ThirdEyePrincipal.class)));
      } else {
        final boolean firstParamIsPrincipal = item.getParameters()
            .get(0)
            .getRawType()
            .isAssignableTo(ThirdEyePrincipal.class);
        if (!firstParamIsPrincipal) {
          events.add(SimpleConditionEvent.violated(item,
              String.format("First parameter of the public method %s is not of type %s",
                  item.getFullName(), ThirdEyePrincipal.class.getSimpleName())));
        }
      }
    }
  }

  private static class IsTopLevelClass extends DescribedPredicate<JavaClass> {

    private static final IsTopLevelClass IS_TOP_LEVEL_CLASS = new IsTopLevelClass();

    private IsTopLevelClass() {
      super("Is top level class");
    }

    @Override
    public boolean test(final JavaClass javaClass) {
      return javaClass.isTopLevelClass();
    }
  }
  
  // FIXME CYRIL - for testResourcesCannotUseDaosDirectly: consider a stricter design: 
  //  enforce resources only use Service classes as fields
  //  then Service classes can be checked easily for properties
  //  (Service being an empty interface or an annotation)
  //    final ArchRule rule1 = constructors().that()
  //        .areDeclaredInClassesThat(ARE_RESOURCE_CLASSES)
  //            .should()
  //            .haveRawParameterTypes(DescribedPredicate.allElements(
  //                are(assignableTo(Service.class)
  //                .or(ARE_RESOURCE_CLASSES))));
}
