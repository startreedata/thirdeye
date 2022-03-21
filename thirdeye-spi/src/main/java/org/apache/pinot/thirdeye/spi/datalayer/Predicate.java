/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.spi.datalayer;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class Predicate {

  private final OPER oper;

  private String lhs;
  private Object rhs;
  private Predicate[] childPredicates;
  public Predicate(String lhs, OPER oper, Object rhs) {
    this.lhs = lhs;
    this.oper = oper;
    this.rhs = rhs;
  }

  private Predicate(OPER oper, Predicate[] childPredicates) {
    this.childPredicates = childPredicates;
    this.oper = oper;
  }

  public static Predicate EQ(String columnName, Object value) {
    return new Predicate(columnName, OPER.EQ, value);
  }

  public static Predicate NEQ(String columnName, Object value) {
    return new Predicate(columnName, OPER.NEQ, value);
  }

  public static Predicate LT(String columnName, Object value) {
    return new Predicate(columnName, OPER.LT, value);
  }

  public static Predicate GT(String columnName, Object value) {
    return new Predicate(columnName, OPER.GT, value);
  }

  public static Predicate LE(String columnName, Object value) {
    return new Predicate(columnName, OPER.LE, value);
  }

  public static Predicate GE(String columnName, Object value) {
    return new Predicate(columnName, OPER.GE, value);
  }

  public static Predicate IN(String columnName, Object[] values) {
    return new Predicate(columnName, OPER.IN, values);
  }

  public static Predicate AND(Predicate... childPredicates) {
    return new Predicate(OPER.AND, childPredicates);
  }

  public static Predicate OR(Predicate... childPredicates) {
    return new Predicate(OPER.OR, childPredicates);
  }

  public static Predicate BETWEEN(String columnName, Object startValue, Object endValue) {
    return new Predicate(columnName, OPER.BETWEEN,
        new ImmutablePair<Object, Object>(startValue, endValue));
  }

  public static Predicate LIKE(String columnName, Object value) {
    return new Predicate(columnName, OPER.LIKE, value);
  }

  public String getLhs() {
    return lhs;
  }

  public OPER getOper() {
    return oper;
  }

  public Object getRhs() {
    return rhs;
  }

  public Predicate[] getChildPredicates() {
    return childPredicates;
  }

  public enum OPER {
    AND("AND"),
    OR("OR"),
    EQ("="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    NEQ("!="),
    IN("IN"),
    BETWEEN("BETWEEN"),
    LIKE("LIKE");

    private final String sign;

    OPER(String sign) {
      this.sign = sign;
    }

    @Override
    public String toString() {
      return sign;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Predicate predicate = (Predicate) o;
    return oper == predicate.oper &&
        Objects.equals(lhs, predicate.lhs) &&
        Objects.equals(rhs, predicate.rhs) &&
        Arrays.equals(childPredicates, predicate.childPredicates);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(oper, lhs, rhs);
    result = 31 * result + Arrays.hashCode(childPredicates);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("oper", oper)
        .add("lhs", lhs)
        .add("rhs", rhs)
        .add("childPredicates", childPredicates)
        .toString();
  }
}
