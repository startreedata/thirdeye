package ai.startree.thirdeye.spi.api;

public interface ThirdEyeCrudApi<T extends ThirdEyeCrudApi<T>> extends ThirdEyeApi {

  Long getId();

  T setId(Long id);
}
