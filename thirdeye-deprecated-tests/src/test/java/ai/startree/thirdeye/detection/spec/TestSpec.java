/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spec;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import java.util.Map;

public class TestSpec extends AbstractSpec {

  private int a = 123;
  private double b = 456.7;
  private String c = "default";
  private Map<String, String> configuration;
  private double threshold = 0.1;
  private double upThreshold;
  private double downThreshold;

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }

  public int getA() {
    return a;
  }

  public void setA(int a) {
    this.a = a;
  }

  public double getB() {
    return b;
  }

  public void setB(double b) {
    this.b = b;
  }

  public String getC() {
    return c;
  }

  public void setC(String c) {
    this.c = c;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public double getUpThreshold() {
    return upThreshold;
  }

  public void setUpThreshold(double upThreshold) {
    this.upThreshold = upThreshold;
  }

  public double getDownThreshold() {
    return downThreshold;
  }

  public void setDownThreshold(double downThreshold) {
    this.downThreshold = downThreshold;
  }
}

