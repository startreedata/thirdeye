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

import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.containAnyMethodsThat;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import ai.startree.thirdeye.PluginLoader;
import ai.startree.thirdeye.ThirdEyeServerModule;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.datalayer.DatabaseAdministratorClient;
import ai.startree.thirdeye.datalayer.DatabaseClient;
import ai.startree.thirdeye.datalayer.DatabaseTransactionClient;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.resources.CrudResource;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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

  // any class that can perform db writes and does not apply the authorization layer
  public static final DescribedPredicate<JavaClass> ARE_NON_SECURED_DB_LAYER_CLASSES = or(
      assignableTo(AbstractManager.class), assignableTo(GenericPojoDao.class),
      assignableTo(DatabaseClient.class), assignableTo(DatabaseTransactionClient.class),
      assignableTo(DatabaseAdministratorClient.class));

  public JavaClasses thirdeyeClasses;

  @BeforeClass
  public void loadClasses() {
    thirdeyeClasses = new ClassFileImporter().importPackages("ai.startree");
  }

  @Test
  public void testResourcesCannotUseNonSecuredDaosDirectly() {
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
    final ArchRule rule = noClasses().that(
        )
        .doNotImplement(ThirdEyeAuthorizer.class)
        .and()
        .doNotBelongToAnyOf(AuthorizationManager.class, ThirdEyeServerModule.class,
            PluginLoader.class)
        .should()
        .accessClassesThat()
        .implement(ThirdEyeAuthorizer.class);
    
    rule.check(thirdeyeClasses);
  }

  // TODO CYRIL next PR - 
  //  ensure no unknown users of generic PojoDAO and managers
  //  ensure Services do their job of using the authorization manager 
  //  and taking the principal as input of all public methods

  // todo add test - all service public methods should ask for principle 
  //  how is a public method of service supposed to check auth 
  //  if it does not require a principal ?

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
