/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
