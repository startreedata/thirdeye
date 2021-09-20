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

package org.apache.pinot.thirdeye.detection.anomaly.utils;

import com.google.api.client.util.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailUtils {

  private static final Logger LOG = LoggerFactory.getLogger(EmailUtils.class);

  /**
   * Check if given email is a valid email
   */
  public static boolean isValidEmailAddress(String email) {
    try {
      Validate.notEmpty(email);
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Parse and return a set of valid email addresses
   *
   * @param emails comma separated list of emails
   */
  public static Set<String> getValidEmailAddresses(String emails) {
    Set<String> validEmailAddresses = new HashSet<>();
    List<String> invalidEmailAddresses = new ArrayList<>();
    if (StringUtils.isBlank(emails)) {
      return Sets.newHashSet();
    } else {
      String[] emailArr = emails.split(",");
      for (String email : emailArr) {
        email = email.trim();
        if (isValidEmailAddress(email)) {
          validEmailAddresses.add(email);
        } else {
          invalidEmailAddresses.add(email);
        }
      }
    }
    if (invalidEmailAddresses.size() > 0) {
      LOG.warn("Found invalid email addresses, please verify the email addresses: {}",
          invalidEmailAddresses);
    }
    return validEmailAddresses;
  }
}
