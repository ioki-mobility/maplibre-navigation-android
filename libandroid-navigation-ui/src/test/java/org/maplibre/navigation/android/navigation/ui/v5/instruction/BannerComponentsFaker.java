package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;

class BannerComponentsFaker {
  static BannerComponents bannerComponents() {
    return bannerComponentsBuilder().build();
  }

  static BannerComponents.Builder bannerComponentsBuilder() {
    return BannerComponents.builder()
      .type("some type")
      .text("some text");
  }

  static BannerComponents bannerComponentsWithAbbreviation() {
    return bannerComponentsBuilder()
      .abbreviationPriority(1)
      .abbreviation("abbreviation text")
      .build();
  }
}
