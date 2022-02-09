package ai.startree.thirdeye.events;

import java.util.Objects;

public class HolidayEvent {

  /**
   * The Name.
   */
  String name;
  /**
   * The Event type.
   */
  String eventType;

  /**
   * The Start time.
   */
  long startTime;
  /**
   * The End time.
   */
  long endTime;

  /**
   * Instantiates a new Holiday event.
   *
   * @param name the name
   * @param eventType the event type
   * @param startTime the start time
   * @param endTime the end time
   */
  HolidayEvent(String name, String eventType, long startTime, long endTime) {
    this.name = name;
    this.eventType = eventType;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets start time.
   *
   * @return the start time
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Sets start time.
   *
   * @param startTime the start time
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  /**
   * Gets end time.
   *
   * @return the end time
   */
  public long getEndTime() {
    return endTime;
  }

  /**
   * Sets end time.
   *
   * @param endTime the end time
   */
  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  /**
   * Gets event type.
   *
   * @return the event type
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * Sets event type.
   *
   * @param eventType the event type
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getEventType(), getStartTime(), getEndTime());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HolidayEvent)) {
      return false;
    }
    HolidayEvent holidayEvent = (HolidayEvent) obj;
    return Objects.equals(getName(), holidayEvent.getName()) && Objects.equals(getStartTime(),
        holidayEvent.getStartTime()) && Objects.equals(getEndTime(), holidayEvent.getEndTime())
        && Objects.equals(
        getEventType(), holidayEvent.getEventType());
  }
}
