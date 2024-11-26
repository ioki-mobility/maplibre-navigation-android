package org.maplibre.navigation.android.navigation.ui.v5.route.impl;

import static org.maplibre.android.style.layers.Property.NONE;
import static org.maplibre.android.style.layers.Property.VISIBLE;
import static org.maplibre.android.style.layers.PropertyFactory.visibility;
import static org.maplibre.navigation.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_LAYER_ID;
import static org.maplibre.navigation.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SHIELD_LAYER_ID;
import static org.maplibre.navigation.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SOURCE_ID;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.Layer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.sources.GeoJsonOptions;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.navigation.android.navigation.ui.v5.R;
import org.maplibre.navigation.android.navigation.ui.v5.route.AlternativeRouteDrawer;
import org.maplibre.navigation.android.navigation.ui.v5.route.MapRouteLayerFactory;
import org.maplibre.navigation.android.navigation.ui.v5.utils.MapUtils;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.utils.Constants;

import java.util.ArrayList;
import java.util.List;

//TODO: check if we also need the processing task
//TODO: clean up
//TODO: fix test
public class MapLibreAlternativeRouteDrawer implements AlternativeRouteDrawer {

    protected Style mapStyle;

    @StyleRes
    private int styleResId;

    protected MapRouteLayerFactory routeLayerFactory;

    @Nullable
    private List<DirectionsRoute> routes;

//    public MapLibreAlternativeRouteDrawer(Style style, MapRouteLayerFactory routeLayerFactory) {
//        this.style = style;
//        this.routeLayerFactory = routeLayerFactory;
//    }

    public MapLibreAlternativeRouteDrawer(MapView mapView, @StyleRes int styleResId, MapRouteLayerFactory routeLayerFactory, @Nullable String belowLayerId) {
        this.styleResId = styleResId;
        this.routeLayerFactory = routeLayerFactory;
        mapView.getMapAsync(mapboxMap -> {
            mapStyle = mapboxMap.getStyle();
            //TODO: check for style availability and wait with callback for loading when not ready yet
            initStyle(mapView.getContext(), mapStyle, styleResId, belowLayerId);
        });
    }

    /**
     * @noinspection resource
     */
    protected void initStyle(Context context, Style mapStyle, @StyleRes int styleResId, @Nullable String belowLayerId) {
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(styleResId, R.styleable.NavigationMapRoute);

            float alternativeRouteScale = typedArray.getFloat(R.styleable.NavigationMapRoute_alternativeRouteScale, 1.0f);
            int alternativeRouteColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteColor,
                    ContextCompat.getColor(context, R.color.maplibre_navigation_alternative_route_color));
            int alternativeRouteShieldColor = typedArray.getColor(R.styleable.NavigationMapRoute_alternativeRouteShieldColor,
                    ContextCompat.getColor(context, R.color.maplibre_navigation_alternative_route_shield_color));

            createLayers(mapStyle, alternativeRouteScale, alternativeRouteColor, alternativeRouteShieldColor, belowLayerId);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    void createLayers(
            Style mapStyle,
            float routeScale,
                      @ColorInt int routeColor,
                      @ColorInt int routeShieldColor,
                      String belowLayerId
    ) {
        LineLayer shieldLineLayer = routeLayerFactory.createAlternativeRouteLayer(routeScale, routeShieldColor);
        MapUtils.addLayerToMap(mapStyle, shieldLineLayer, belowLayerId);

        LineLayer routeLineLayer = routeLayerFactory.createAlternativeRoutesShieldLayer(routeScale, routeColor);
        MapUtils.addLayerToMap(mapStyle, routeLineLayer, belowLayerId);
    }

    @Override
    public void setStyle(Style style) {
        this.mapStyle = style;

        if (routes != null) {
            drawRoutes(routes);
        }
    }

    @Override
    public void setRoutes(List<DirectionsRoute> routes) {
        this.routes = routes;
        drawRoutes(routes);
    }

    @Override
    public void setVisibility(boolean isVisible) {
        if (mapStyle == null || !mapStyle.isFullyLoaded()) {
            return;
        }

        Layer shieldLayer = mapStyle.getLayer(ALTERNATIVE_ROUTE_SHIELD_LAYER_ID);
        if (shieldLayer != null) {
            shieldLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }

        Layer routeLayer = mapStyle.getLayer(ALTERNATIVE_ROUTE_LAYER_ID);
        if (routeLayer != null) {
            routeLayer.setProperties(visibility(isVisible ? VISIBLE : NONE));
        }
    }

    protected void drawRoutes(List<DirectionsRoute> routes) {
        ArrayList<Feature> routeLineFeatures = new ArrayList<>();
        for (DirectionsRoute route : routes) {
            String routeGeometry = route.geometry();
            if (routeGeometry != null) {
                LineString routeLineString = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6);
                Feature lineFeature = Feature.fromGeometry(routeLineString);
                routeLineFeatures.add(lineFeature);
            }
        }

        drawFeatures(routeLineFeatures);
    }

    private void drawFeatures(List<Feature> features) {
        if (!mapStyle.isFullyLoaded()) {
            // The style is not available anymore. Skip processing.
            return;
        }

        getSource(mapStyle).setGeoJson(FeatureCollection.fromFeatures(features));

//        if (!mapStyle.isFullyLoaded()) {
//            // The style is not available anymore. Skip processing.
//            return;
//        }
//
//        ArrayList<Feature> routeLineFeatures = new ArrayList<>();
//
//        for (LineString routeLine : routeLines) {
//            Feature routeLineFeature = Feature.fromGeometry(routeLine);
//            routeLineFeatures.add(routeLineFeature);
//        }
//
//        getSource(mapStyle).setGeoJson(FeatureCollection.fromFeatures(routeLineFeatures));
    }

    private GeoJsonSource getSource(Style style) {
        GeoJsonSource primaryRouteSource = (GeoJsonSource) style.getSource(ALTERNATIVE_ROUTE_SOURCE_ID);
        if (primaryRouteSource == null) {
            primaryRouteSource = new GeoJsonSource(ALTERNATIVE_ROUTE_SOURCE_ID, new GeoJsonOptions().withMaxZoom(16));
            style.addSource(primaryRouteSource);
        }

        return primaryRouteSource;
    }
}
