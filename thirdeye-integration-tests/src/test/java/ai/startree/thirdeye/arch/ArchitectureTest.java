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

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.containAnyMethodsThat;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import ai.startree.thirdeye.datalayer.DatabaseService;
import ai.startree.thirdeye.datalayer.DatabaseTransactionService;
import ai.startree.thirdeye.resources.CrudResource;
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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
      .or(containAnyMethodsThat(annotatedWith(GET.class)));

  public static final DescribedPredicate<JavaClass> NON_SECURED_DAO_CLASSES = DescribedPredicate.or(
      assignableTo(AbstractManager.class),
      // assignableTo(GenericPojoDao.class),
      assignableTo(DatabaseService.class),
      assignableTo(DatabaseTransactionService.class));

  @Test
  public void testResourcesCannotUseDaosDirectly() {
    final JavaClasses importedClasses = new ClassFileImporter().importPackages("ai.startree");
    final ArchRule rule = noClasses().that(ARE_RESOURCE_CLASSES)
        .should().dependOnClassesThat()
        .areAssignableTo(NON_SECURED_DAO_CLASSES);

    rule.check(importedClasses);
  }

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
