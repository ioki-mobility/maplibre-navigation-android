package org.maplibre.navigation.android.navigation.ui.v5.route;

import android.app.Activity;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.StyleRes;

import androidx.fragment.app.Fragment;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.navigation.android.navigation.ui.v5.R;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a route using {@link NavigationMapRoute#addRoutes(List)} and a route will be drawn using
 * runtime styling. The route will automatically be placed below all labels independent of specific
 * style. If the map styles changed when a routes drawn on the map, the route will automatically be
 * redrawn onto the new map style. If during a navigation session, the user gets re-routed, the
 * route line will be redrawn to reflect the new geometry.
 * <p>
 * You are given the option when first constructing an instance of this class to pass in a style
 * resource. This allows for custom colorizing and line scaling of the route. Inside your
 * applications {@code style.xml} file, you extend {@code <style name="NavigationMapRoute">} and
 * change some or all the options currently offered. If no style files provided in the constructor,
 * the default style will be used.
 * <p>
 * You can customize the logic for route drawing by passing in your own implementations of
 * {@link PrimaryRouteDrawer}, {@link AlternativeRouteDrawer}, {@link RouteArrowDrawer}, and
 * {@link WayPointDrawer} when constructing this class. If NULL is passed in for any of these
 * classes, the default class will be used.
 *
 * @since 0.4.0
 */
public class NavigationMapRoute implements LifecycleObserver, OnRouteSelectionChangeListener {


    @StyleRes
    private final int styleRes;
    private final MapLibreMap mapLibreMap;
    private final MapView mapView;
    private final MapRouteClickListener mapRouteClickListener;
    private final MapRouteProgressChangeListener mapRouteProgressChangeListener;
    private boolean isMapClickListenerAdded = false;
    private MapView.OnDidFinishLoadingStyleListener didFinishLoadingStyleListener;
    private boolean isDidFinishLoadingStyleListenerAdded = false;
    private MapLibreNavigation navigation;
    private RouteArrowDrawer routeArrowDrawer;
    private PrimaryRouteDrawer primaryRouteDrawer;
    private AlternativeRouteDrawer alternativeRouteDrawer;
    private WayPointDrawer wayPointDrawer;
    private final List<DirectionsRoute> routes = new ArrayList<>();
    private int primaryRouteIndex = 0;
    private OnRouteSelectionChangeListener clientRouteSelectionChangeListener;

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param mapView     the MapView to apply the route to
     * @param mapLibreMap the MapLibreMap to apply route with
     * @since 0.4.0
     */
    public NavigationMapRoute(@NonNull MapView mapView, @NonNull MapLibreMap mapLibreMap) {
        this(null, mapView, mapLibreMap, R.style.NavigationMapRoute);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param mapView     the MapView to apply the route to
     * @param mapLibreMap the MapLibreMap to apply route with
     * @param belowLayer  optionally pass in a layer id to place the route line below
     * @since 0.4.0
     */
    public NavigationMapRoute(@NonNull MapView mapView, @NonNull MapLibreMap mapLibreMap,
                              @Nullable String belowLayer) {
        this(null, mapView, mapLibreMap, R.style.NavigationMapRoute, null, null, null, null, belowLayer);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation  an instance of the {@link MapLibreNavigation} object. Passing in null means
     *                    your route won't consider rerouting during a navigation session.
     * @param mapView     the MapView to apply the route to
     * @param mapLibreMap the MapLibreMap to apply route with
     * @since 0.4.0
     */
    public NavigationMapRoute(@Nullable MapLibreNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapLibreMap mapLibreMap) {
        this(navigation, mapView, mapLibreMap, R.style.NavigationMapRoute);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation  an instance of the {@link MapLibreNavigation} object. Passing in null means
     *                    your route won't consider rerouting during a navigation session.
     * @param mapView     the MapView to apply the route to
     * @param mapLibreMap the MapLibreMap to apply route with
     * @param belowLayer  optionally pass in a layer id to place the route line below
     * @since 0.4.0
     */
    public NavigationMapRoute(@Nullable MapLibreNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapLibreMap mapLibreMap, @Nullable String belowLayer) {
        this(navigation, mapView, mapLibreMap, R.style.NavigationMapRoute, null, null, null, null, belowLayer);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation  an instance of the {@link MapLibreNavigation} object. Passing in null means
     *                    your route won't consider rerouting during a navigation session.
     * @param mapView     the MapView to apply the route to
     * @param mapLibreMap the MapLibreMap to apply route with
     * @param styleRes    a style resource with custom route colors, scale, etc.
     */
    public NavigationMapRoute(@Nullable MapLibreNavigation navigation, @NonNull MapView mapView,
                              @NonNull MapLibreMap mapLibreMap, @StyleRes int styleRes) {
        this(navigation, mapView, mapLibreMap, styleRes, null, null, null, null, null);
    }

    /**
     * Construct an instance of {@link NavigationMapRoute}.
     *
     * @param navigation             an instance of the {@link MapLibreNavigation} object. Passing in null means
     *                               your route won't consider rerouting during a navigation session.
     * @param mapView                the MapView to apply the route to
     * @param mapLibreMap              the MapLibreMap to apply route with
     * @param styleRes               a style resource with custom route colors, scale, etc.
     * @param primaryRouteDrawer     a custom primary route drawer
     * @param alternativeRouteDrawer a custom alternative route drawer
     * @param routeArrowDrawer       a custom route arrow drawer
     * @param wayPointDrawer         a custom way point drawer
     * @param belowLayer             optionally pass in a layer id to place the route line below
     */
    public NavigationMapRoute(
            @Nullable MapLibreNavigation navigation,
            @NonNull MapView mapView,
            @NonNull MapLibreMap mapLibreMap,
            @StyleRes int styleRes,
            @Nullable PrimaryRouteDrawer primaryRouteDrawer,
            @Nullable AlternativeRouteDrawer alternativeRouteDrawer,
            @Nullable RouteArrowDrawer routeArrowDrawer,
            @Nullable WayPointDrawer wayPointDrawer,
            @Nullable String belowLayer
    ) {
        this.styleRes = styleRes;
        this.mapView = mapView;
        this.mapLibreMap = mapLibreMap;
        this.navigation = navigation;

        this.alternativeRouteDrawer = alternativeRouteDrawer;
        if (alternativeRouteDrawer == null) {
            this.alternativeRouteDrawer = new MapLibreCongestionAlternativeRouteDrawer(mapView, styleRes, new MapRouteLayerFactory(), findRouteBelowLayerId(belowLayer, mapLibreMap.getStyle()));
        }

        this.primaryRouteDrawer = primaryRouteDrawer;
        if (primaryRouteDrawer == null) {
            this.primaryRouteDrawer = new MapLibreCongestionPrimaryRouteDrawer(mapView, styleRes, false, new MapRouteLayerFactory(), findRouteBelowLayerId(belowLayer, mapLibreMap.getStyle()));
        }

        this.wayPointDrawer = wayPointDrawer;
        if (wayPointDrawer == null) {
            this.wayPointDrawer = new MapLibreWayPointDrawer(mapView, styleRes, new MapRouteLayerFactory(), findRouteBelowLayerId(belowLayer, mapLibreMap.getStyle()));
        }

        this.routeArrowDrawer = routeArrowDrawer;
        if (routeArrowDrawer == null) {
            this.routeArrowDrawer = new MapLibreRouteArrowDrawer(mapView, mapLibreMap, styleRes);
        }

        this.mapRouteClickListener = new MapRouteClickListener(this);
        this.mapRouteProgressChangeListener = new MapRouteProgressChangeListener(this.primaryRouteDrawer, this.routeArrowDrawer);
        initializeDidFinishLoadingStyleListener();
        addListeners();
    }

    private String findRouteBelowLayerId(String belowLayer, Style style) {
        if (belowLayer == null || belowLayer.isEmpty()) {
            List<Layer> styleLayers = style.getLayers();
            for (int i = 0; i < styleLayers.size(); i++) {
                if (!(styleLayers.get(i) instanceof SymbolLayer)
                        // Avoid placing the route on top of the user location layer
                        && !styleLayers.get(i).getId().contains(RouteConstants.MAPBOX_LOCATION_ID)) {
                    belowLayer = styleLayers.get(i).getId();
                }
            }
        }
        return belowLayer;
    }

    /**
     * Allows adding a single primary route for the user to traverse along. No alternative routes will
     * be drawn on top of the map.
     *
     * @param directionsRoute the directions route which you'd like to display on the map
     * @since 0.4.0
     */
    public void addRoute(DirectionsRoute directionsRoute) {
        List<DirectionsRoute> routes = new ArrayList<>();
        routes.add(directionsRoute);
        addRoutes(routes);
    }

    /**
     * Provide a list of {@link DirectionsRoute}s, the primary route will default to the first route
     * in the directions route list. All other routes in the list will be drawn on the map using the
     * alternative route style.
     *
     * @param directionsRoutes a list of direction routes, first one being the primary and the rest of
     *                         the routes are considered alternatives.
     * @since 0.8.0
     */
    public void addRoutes(@NonNull @Size(min = 1) List<DirectionsRoute> directionsRoutes) {
        routes.addAll(directionsRoutes);
        mapRouteClickListener.addRoutes(directionsRoutes);

        primaryRouteDrawer.setRoute(directionsRoutes.get(primaryRouteIndex));
        wayPointDrawer.setRoute(directionsRoutes.get(primaryRouteIndex));

        List<DirectionsRoute> alternativeRoutes = new ArrayList<>();
        for (DirectionsRoute route : directionsRoutes) {
            String routeIndexString = directionsRoutes.get(primaryRouteIndex).routeIndex();
            if (route.routeIndex() != null && routeIndexString != null && Integer.parseInt(route.routeIndex()) != Integer.parseInt(routeIndexString)) {
                alternativeRoutes.add(route);
            }
        }

        alternativeRouteDrawer.setRoutes(alternativeRoutes);
    }

    /**
     * Hides all routes / route arrows on the map drawn by this class.
     *
     * @deprecated you can now use a combination of {@link NavigationMapRoute#updateRouteVisibilityTo(boolean)}
     * and {@link NavigationMapRoute#updateRouteArrowVisibilityTo(boolean)} to hide the route line and arrow.
     */
    @Deprecated
    public void removeRoute() {
        updateRouteVisibilityTo(false);
        updateRouteArrowVisibilityTo(false);
    }

    /**
     * Hides all routes on the map drawn by this class.
     *
     * @param isVisible true to show routes, false to hide
     */
    public void updateRouteVisibilityTo(boolean isVisible) {
        primaryRouteDrawer.setVisibility(isVisible);
        alternativeRouteDrawer.setVisibility(isVisible);
        wayPointDrawer.setVisibility(isVisible);
    }

    /**
     * Hides the progress arrow on the map drawn by this class.
     *
     * @param isVisible true to show routes, false to hide
     */
    public void updateRouteArrowVisibilityTo(boolean isVisible) {
        routeArrowDrawer.updateVisibilityTo(isVisible);
    }

    /**
     * Add a {@link OnRouteSelectionChangeListener} to know which route the user has currently
     * selected as their primary route.
     *
     * @param onRouteSelectionChangeListener a listener which lets you know when the user has changed
     *                                       the primary route and provides the current direction
     *                                       route which the user has selected
     * @since 0.8.0
     */
    public void setOnRouteSelectionChangeListener(@Nullable OnRouteSelectionChangeListener onRouteSelectionChangeListener) {
        clientRouteSelectionChangeListener = onRouteSelectionChangeListener;
    }

    /**
     * Toggle whether or not you'd like the map to display the alternative routes. This options great
     * for when the user actually begins the navigation session and alternative routes aren't needed
     * anymore.
     *
     * @param alternativesVisible true if you'd like alternative routes to be displayed on the map,
     *                            else false
     * @since 0.8.0
     */
    public void showAlternativeRoutes(boolean alternativesVisible) {
        alternativeRouteDrawer.setVisibility(alternativesVisible);
    }

    /**
     * Toggle whether or not you'd like enable the primary route eating feature
     *
     * @param routeEatingEnabled true if you'd like to enable the primary route eating feature,
     *                           else false
     */
    public void setRouteEatingEnabled(boolean routeEatingEnabled) {
        primaryRouteDrawer.setRouteEatingEnabled(routeEatingEnabled);
    }

    /**
     * This method will allow this class to listen to new routes based on
     * the progress updates from {@link MapLibreNavigation}.
     * <p>
     * If a new route is given to {@link MapLibreNavigation#startNavigation(DirectionsRoute)}, this
     * class will automatically draw the new route.
     *
     * @param navigation to add the progress change listener
     */
    public void addProgressChangeListener(MapLibreNavigation navigation) {
        this.navigation = navigation;
        navigation.addProgressChangeListener(mapRouteProgressChangeListener);
    }

    /**
     * Should be called if {@link NavigationMapRoute#addProgressChangeListener(MapLibreNavigation)} was
     * called to prevent leaking.
     *
     * @param navigation to remove the progress change listener
     */
    public void removeProgressChangeListener(MapLibreNavigation navigation) {
        if (navigation != null) {
            navigation.removeProgressChangeListener(mapRouteProgressChangeListener);
        }
    }

    /**
     * This method should be added in your {@link Activity#onStart()} or
     * {@link Fragment#onStart()} to handle adding and removing of listeners,
     * preventing memory leaks.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        addListeners();
    }

    /**
     * This method should be added in your {@link Activity#onStop()} or {@link Fragment#onStop()}
     * to handle adding and removing of listeners, preventing memory leaks.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        removeListeners();
    }

    public PrimaryRouteDrawer getPrimaryRouteDrawer() {
        return primaryRouteDrawer;
    }

    private void initializeDidFinishLoadingStyleListener() {
        didFinishLoadingStyleListener = () -> mapLibreMap.getStyle(this::drawWithStyle);
    }

    private void addListeners() {
        if (!isMapClickListenerAdded) {
            mapLibreMap.addOnMapClickListener(mapRouteClickListener);
            isMapClickListenerAdded = true;
        }
        if (navigation != null) {
            navigation.addProgressChangeListener(mapRouteProgressChangeListener);
        }
        if (!isDidFinishLoadingStyleListenerAdded) {
            mapView.addOnDidFinishLoadingStyleListener(didFinishLoadingStyleListener);
            isDidFinishLoadingStyleListenerAdded = true;
        }
    }

    private void removeListeners() {
        if (isMapClickListenerAdded) {
            mapLibreMap.removeOnMapClickListener(mapRouteClickListener);
            isMapClickListenerAdded = false;
        }
        if (navigation != null) {
            navigation.removeProgressChangeListener(mapRouteProgressChangeListener);
        }
        if (isDidFinishLoadingStyleListenerAdded) {
            mapView.removeOnDidFinishLoadingStyleListener(didFinishLoadingStyleListener);
            isDidFinishLoadingStyleListenerAdded = false;
        }
    }

    private void drawWithStyle(Style style) {
        primaryRouteDrawer.setStyle(style);
        alternativeRouteDrawer.setStyle(style);
        wayPointDrawer.setStyle(style);

        routeArrowDrawer = new MapLibreRouteArrowDrawer(mapView, mapLibreMap, styleRes);
    }

    @Override
    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {
        if (directionsRoute.routeIndex() != null) {
            primaryRouteIndex = Integer.parseInt(directionsRoute.routeIndex());
        }
        primaryRouteDrawer.setRoute(directionsRoute);
        wayPointDrawer.setRoute(directionsRoute);

        List<DirectionsRoute> alternativeRoutes = new ArrayList<>();
        for (DirectionsRoute route : routes) {
            if (route.routeIndex() != null && Integer.parseInt(route.routeIndex()) != primaryRouteIndex) {
                alternativeRoutes.add(route);
            }
        }
        alternativeRouteDrawer.setRoutes(alternativeRoutes);

        if (clientRouteSelectionChangeListener != null) {
            clientRouteSelectionChangeListener.onNewPrimaryRouteSelected(directionsRoute);
        }
    }
}