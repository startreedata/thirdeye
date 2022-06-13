/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.events;

import java.util.List;

public interface EventDataProvider<T> {

  List<T> getEvents(EventFilter eventFilter);

  String getEventType();
}
