package org.maplibre.navigation.android.navigation.ui.v5.route;

import static com.mapbox.core.constants.Constants.PRECISION_6;

import androidx.annotation.NonNull;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.turf.TurfConstants;
import org.maplibre.turf.TurfMeasurement;
import org.maplibre.turf.TurfMisc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class MapRouteClickListener implements MapLibreMap.OnMapClickListener {

  private final List<DirectionsRoute> routes = new ArrayList<>();

  private final OnRouteSelectionChangeListener routeChangeListener;

  MapRouteClickListener(@NonNull OnRouteSelectionChangeListener routeChangeListener) {
    this.routeChangeListener = routeChangeListener;
  }

  void addRoute(DirectionsRoute route) {
    this.routes.add(route);
  }

  void addRoutes(List<DirectionsRoute> routes) {
    this.routes.addAll(routes);
  }

  void setRoutes(List<DirectionsRoute> routes) {
    this.routes.clear();
    this.routes.addAll(routes);
  }

  void clearRoutes() {
    routes.clear();
  }

  @Override
  public boolean onMapClick(@NonNull LatLng latLng) {
    return findClickedRoute(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()), routes);
  }

  private boolean findClickedRoute(@NonNull Point clickPoint, @NonNull List<DirectionsRoute> directionsRoutes) {
    HashMap<Double, DirectionsRoute> routeDistancesAwayFromClick = calculateClickDistances(clickPoint, directionsRoutes);
    List<Double> distancesAwayFromClick = new ArrayList<>(routeDistancesAwayFromClick.keySet());
    Collections.sort(distancesAwayFromClick);

    //TODO: this will handle ALL clicks as route switching.. or not? We should limit the click
    // distance to a certain threshold

    if (distancesAwayFromClick.isEmpty()) {
      return false;
    }

    DirectionsRoute clickedRoute = routeDistancesAwayFromClick.get(distancesAwayFromClick.get(0));
    routeChangeListener.onNewPrimaryRouteSelected(clickedRoute);
    return true;
  }

  private HashMap<Double, DirectionsRoute> calculateClickDistances(Point clickPoint, List<DirectionsRoute> routes) {
    HashMap<Double, DirectionsRoute> routeDistanceToClick = new HashMap<>();
    for (DirectionsRoute route : routes) {
      if (route.geometry() == null) {
        continue;
      }

      LineString routeLine = LineString.fromPolyline(route.geometry(), PRECISION_6);
      Point pointOnLine = findPointOnLine(clickPoint, routeLine);
      if (pointOnLine == null) {
        continue;
      }

      double distance = TurfMeasurement.distance(clickPoint, pointOnLine, TurfConstants.UNIT_METERS);
      routeDistanceToClick.put(distance, route);
    }

    return routeDistanceToClick;
  }

  private Point findPointOnLine(Point clickPoint, LineString lineString) {
    List<Point> linePoints = lineString.coordinates();
    Feature feature = TurfMisc.nearestPointOnLine(clickPoint, linePoints);
    return (Point) feature.geometry();
  }
}
