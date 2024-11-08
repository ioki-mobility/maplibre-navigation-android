package org.maplibre.navigation.android.navigation.v5.milestone;

import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

public interface MilestoneEventListener {

  void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone);

}
