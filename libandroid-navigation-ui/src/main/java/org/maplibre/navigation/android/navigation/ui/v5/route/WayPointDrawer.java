package org.maplibre.navigation.android.navigation.ui.v5.route;


import org.maplibre.android.maps.Style;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;

public interface WayPointDrawer {

    void setRoute(DirectionsRoute route);

    void setStyle(Style style);

    void setVisibility(boolean isVisible);
}
