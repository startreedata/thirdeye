package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class EmailSchemeApi {

  private String from;
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

  public String getFrom() {
    return from;
  }

  public EmailSchemeApi setFrom(final String from) {
    this.from = from;
    return this;
  }

  public List<String> getTo() {
    return to;
  }

  public EmailSchemeApi setTo(final List<String> to) {
    this.to = to;
    return this;
  }

  public List<String> getCc() {
    return cc;
  }

  public EmailSchemeApi setCc(final List<String> cc) {
    this.cc = cc;
    return this;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public EmailSchemeApi setBcc(final List<String> bcc) {
    this.bcc = bcc;
    return this;
  }
}
