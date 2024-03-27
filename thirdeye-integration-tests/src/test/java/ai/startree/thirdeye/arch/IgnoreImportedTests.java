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

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import java.util.regex.Pattern;

/**
 * Ignore tests classes imported as dependency.
 * For instance, ignore: 
 * Location{uri=jar:file:/Users/cyril/.m2/repository/ai/startree/thirdeye/thirdeye-persistence/1.266.0-SNAPSHOT/thirdeye-persistence-1.266.0-SNAPSHOT-tests.jar!/ai/startree/thirdeye/datalayer/ MySqlTestDatabase.class}
 * */
public class IgnoreImportedTests implements ImportOption {
  
  private final static Pattern IMPORTED_TEST_PACKAGE_PATTERN = Pattern.compile(".*-tests\\.jar.*");

  @Override
  public boolean includes(final Location location) {
    return !location.matches(IMPORTED_TEST_PACKAGE_PATTERN);
  }
}
