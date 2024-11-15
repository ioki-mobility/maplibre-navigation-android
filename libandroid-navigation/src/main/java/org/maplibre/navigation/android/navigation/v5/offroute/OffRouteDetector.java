package org.maplibre.navigation.android.navigation.v5.offroute;

import android.location.Location;

import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.RingBuffer;
import org.maplibre.turf.TurfConstants;
import org.maplibre.turf.TurfMeasurement;
import org.maplibre.turf.TurfMisc;

import java.util.List;

import static org.maplibre.navigation.android.navigation.v5.utils.MeasurementUtils.userTrueDistanceFromStep;
import static org.maplibre.navigation.android.navigation.v5.utils.ToleranceUtils.dynamicRerouteDistanceTolerance;

public class OffRouteDetector extends OffRoute {

  private Point lastReroutePoint;
  private OffRouteCallback callback;
  private final RingBuffer<Integer> distancesAwayFromManeuver = new RingBuffer<>(3);
  private static final int TWO_POINTS = 2;

  /**
   * Method in charge of running a series of test based on the device current location
   * and the user progress along the route.
   * <p>
   * Test #1:
   * Distance remaining.  If the route distance remaining is 0, then return true immediately.  In the
   * route processor this will prompt the snap-to-route logic to return the raw Location.  If there isn't any
   * distance remaining, the user will always be off-route.
   * <p>
   * Test #2:
   * Valid or invalid off-route.  An off-route check can only continue if the device has received
   * at least 1 location update (for comparison) and the user has traveled passed
   * the {@link MapLibreNavigationOptions#minimumDistanceBeforeRerouting()} checked against the last re-route location.
   * <p>
   * Test #3:
   * Distance from the step. This test is checked against the max of the dynamic rerouting tolerance or the
   * accuracy based tolerance. If this test passes, this method then also checks if there have been &gt;= 3
   * location updates moving away from the maneuver point. If false, this method will return false early.
   * <p>
   * Test #4:
   * Checks if the user is close the upcoming step.  At this point, the user is considered off-route.
   * But, if the location update is within the {@link MapLibreNavigationOptions#maneuverZoneRadius()} of the
   * upcoming step, this method will return false as well as send fire {@link OffRouteCallback#onShouldIncreaseIndex()}
   * to let the <tt>NavigationEngine</tt> know that the
   * step index should be increased on the next location update.
   *
   * @return true if the users off-route, else false.
   * @since 0.2.0
   */
  @Override
  public boolean isUserOffRoute(Location location, RouteProgress routeProgress, MapLibreNavigationOptions options) {

    if (checkDistanceRemaining(routeProgress)) {
      return true;
    }

    if (!validOffRoute(location, options)) {
      return false;
    }
    Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
    boolean isOffRoute = checkOffRouteRadius(location, routeProgress, options, currentPoint);

    if (!isOffRoute) {
      return isMovingAwayFromManeuver(location, routeProgress, distancesAwayFromManeuver,
        currentPoint, options);
    }

    LegStep upComingStep = routeProgress.currentLegProgress().upComingStep();
    if (closeToUpcomingStep(options, callback, currentPoint, upComingStep)) {
      return false;
    }

    // All checks have run, return true
    updateLastReroutePoint(location);
    return true;
  }

  /**
   * Sets a callback that is fired for different off-route scenarios.
   * <p>
   * Right now, the only scenario is when the step index should be increased with
   * {@link OffRouteCallback#onShouldIncreaseIndex()}.
   *
   * @param callback to be fired
   * @since 0.11.0
   */
  public void setOffRouteCallback(OffRouteCallback callback) {
    this.callback = callback;
  }

  /**
   * Clears the {@link RingBuffer} used for tracking our recent
   * distances away from the maneuver that is being driven towards.
   *
   * @since 0.11.0
   */
  public void clearDistancesAwayFromManeuver() {
    distancesAwayFromManeuver.clear();
  }

  private boolean checkDistanceRemaining(RouteProgress routeProgress) {
    return routeProgress.distanceRemaining() == 0;
  }

  /**
   * Method to check if the user has passed either the set (in {@link MapLibreNavigationOptions})
   * minimum amount of seconds or minimum amount of meters since the last reroute.
   * <p>
   * If the user is above both thresholds, then the off-route can proceed.  Otherwise, ignore.
   *
   * @param location current location from engine
   * @param options  for second (default 3) / distance (default 50m) minimums
   * @return true if valid, false if not
   */
  private boolean validOffRoute(Location location, MapLibreNavigationOptions options) {
    // Check if minimum amount of distance has been passed since last reroute
    Point currentPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
    double distanceFromLastReroute = 0d;
    if (lastReroutePoint != null) {
      distanceFromLastReroute = TurfMeasurement.distance(lastReroutePoint,
        currentPoint, TurfConstants.UNIT_METERS);
    } else {
      // If null, this is our first update - set the last reroute point to the given location
      updateLastReroutePoint(location);
    }
    return distanceFromLastReroute > options.minimumDistanceBeforeRerouting();
  }

  private boolean checkOffRouteRadius(Location location, RouteProgress routeProgress,
                                      MapLibreNavigationOptions options, Point currentPoint) {
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double distanceFromCurrentStep = userTrueDistanceFromStep(currentPoint, currentStep);
    double offRouteRadius = createOffRouteRadius(location, routeProgress, options, currentPoint);
    return distanceFromCurrentStep > offRouteRadius;
  }

  private double createOffRouteRadius(Location location, RouteProgress routeProgress,
                                      MapLibreNavigationOptions options, Point currentPoint) {
    double dynamicTolerance = dynamicRerouteDistanceTolerance(currentPoint, routeProgress, options);
    double accuracyTolerance = location.getAccuracy() * options.deadReckoningTimeInterval();
    return Math.max(dynamicTolerance, accuracyTolerance);
  }

  private boolean isMovingAwayFromManeuver(Location location,
                                           RouteProgress routeProgress,
                                           RingBuffer<Integer> distancesAwayFromManeuver,
                                           Point currentPoint,
                                           MapLibreNavigationOptions options) {
    List<Point> stepPoints = routeProgress.currentStepPoints();
    if (movingAwayFromManeuver(routeProgress, distancesAwayFromManeuver, stepPoints, currentPoint, options)) {
      updateLastReroutePoint(location);
      return true;
    }
    return false;
  }

  /**
   * If the upcoming step is not null, detect if the current point
   * is within the maneuver radius.
   * <p>
   * If it is, fire {@link OffRouteCallback#onShouldIncreaseIndex()} to increase the step
   * index in the <tt>NavigationEngine</tt> and return true.
   *
   * @param options      for maneuver zone radius
   * @param callback     to increase step index
   * @param currentPoint for distance from upcoming step
   * @param upComingStep for distance from current point
   * @return true if close to upcoming step, false if not
   */
  private static boolean closeToUpcomingStep(MapLibreNavigationOptions options, OffRouteCallback callback,
                                             Point currentPoint, LegStep upComingStep) {
    if (callback == null) {
      return false;
    }

    boolean isCloseToUpcomingStep;
    if (upComingStep != null) {
      double distanceFromUpcomingStep = userTrueDistanceFromStep(currentPoint, upComingStep);
      double maneuverZoneRadius = options.maneuverZoneRadius();
      isCloseToUpcomingStep = distanceFromUpcomingStep < maneuverZoneRadius;
      if (isCloseToUpcomingStep) {
        // Callback to the NavigationEngine to increase the step index
        callback.onShouldIncreaseIndex();
        return true;
      }
    }
    return false;
  }

  /**
   * Checks to see if the current point is moving away from the maneuver.
   * <p>
   * Minimum three location updates and minimum of 50 meters away from the maneuver are required
   * to fire an off-route event. This parameters be considered that the user is no longer going in the right direction.
   *
   * @param routeProgress             for the upcoming step maneuver
   * @param distancesAwayFromManeuver current stack of distances away
   * @param stepPoints                current step points being traveled along
   * @param currentPoint              to determine if moving away or not
   * @return true if moving away from maneuver, false if not
   */
  private static boolean movingAwayFromManeuver(RouteProgress routeProgress,
                                                RingBuffer<Integer> distancesAwayFromManeuver,
                                                List<Point> stepPoints,
                                                Point currentPoint,
                                                MapLibreNavigationOptions options) {
    boolean invalidUpcomingStep = routeProgress.currentLegProgress().upComingStep() == null;
    boolean invalidStepPointSize = stepPoints.size() < TWO_POINTS;
    if (invalidUpcomingStep || invalidStepPointSize) {
      return false;
    }

    LineString stepLineString = LineString.fromLngLats(stepPoints);
    Point maneuverPoint = stepPoints.get(stepPoints.size() - 1);
    Point userPointOnStep = (Point) TurfMisc.nearestPointOnLine(currentPoint, stepPoints).geometry();

    if (userPointOnStep == null || maneuverPoint.equals(userPointOnStep)) {
      return false;
    }

    LineString remainingStepLineString = TurfMisc.lineSlice(userPointOnStep, maneuverPoint, stepLineString);
    int userDistanceToManeuver = (int) TurfMeasurement.length(remainingStepLineString, TurfConstants.UNIT_METERS);

    if (distancesAwayFromManeuver.isEmpty()) {
      // No move-away positions before, add the current one to history stack
      distancesAwayFromManeuver.addLast(userDistanceToManeuver);
    } else if (userDistanceToManeuver > distancesAwayFromManeuver.getLast()) {
      // If distance to maneuver increased (wrong way), add new position to history stack

      if (distancesAwayFromManeuver.size() >= 3) {
        // Replace the latest position with newest one, for keeping first position
        distancesAwayFromManeuver.removeLast();
      }
      distancesAwayFromManeuver.addLast(userDistanceToManeuver);
    } else if ((distancesAwayFromManeuver.getLast() - userDistanceToManeuver) > options.offRouteMinimumDistanceMetersBeforeRightDirection()) {
      // If distance to maneuver decreased (right way) clean history
      distancesAwayFromManeuver.clear();
    }

    // Minimum 3 position updates in the wrong way are required before an off-route can occur
    if (distancesAwayFromManeuver.size() >= 3) {
      // Check for minimum distance traveled
      return (distancesAwayFromManeuver.getLast() - distancesAwayFromManeuver.getFirst()) > options.offRouteMinimumDistanceMetersBeforeWrongDirection();
    }

    return false;
  }

  private void updateLastReroutePoint(Location location) {
    lastReroutePoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
  }
}
