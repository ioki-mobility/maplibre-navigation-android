package org.maplibre.navigation.android.navigation.ui.v5.route;

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

public interface RouteArrowDrawer {

    void addUpcomingManeuverArrow(RouteProgress routeProgress);

    void updateVisibilityTo(boolean isVisible);
}
