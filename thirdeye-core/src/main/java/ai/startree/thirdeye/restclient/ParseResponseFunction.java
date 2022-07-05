/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.restclient;

import java.io.IOException;

/**
 * A functional interface to allow passing a function to the rest client that would parse the
 * response
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
public interface ParseResponseFunction<T, R> {

  /**
   * Function that will take in a respnse and parse into an object
   *
   * @param t the t
   * @return the r
   * @throws IOException the io exception
   */
  R parse(T t) throws IOException;
}
