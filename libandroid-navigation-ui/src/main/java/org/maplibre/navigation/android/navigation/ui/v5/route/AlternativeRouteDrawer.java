package org.maplibre.navigation.android.navigation.ui.v5.route;

import org.maplibre.android.maps.Style;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;

import java.util.List;

public interface AlternativeRouteDrawer {

    void setRoutes(List<DirectionsRoute> routes);

    void setStyle(Style style);

    void setVisibility(boolean isVisible);
}
