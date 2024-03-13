package com.mapbox.services.android.navigation.ui.v5.route;

import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.product;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.switchCase;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconPitchAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ALTERNATIVE_ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.CONGESTION_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.DESTINATION_MARKER_NAME;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.HEAVY_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.MODERATE_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ORIGIN_MARKER_NAME;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_CONGESTION_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_CONGESTION_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.PRIMARY_ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ROUTE_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ROUTE_SHIELD_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.ROUTE_SOURCE_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.SEVERE_CONGESTION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_DESTINATION_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_LAYER_ID;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_ORIGIN_VALUE;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_PROPERTY_KEY;
import static com.mapbox.services.android.navigation.ui.v5.route.RouteConstants.WAYPOINT_SOURCE_ID;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.services.android.navigation.ui.v5.utils.MapImageUtils;

//TODO: public to be able to change logic?!
//TODO: add as interface?!
public class MapRouteLayerFactory {

    /**
     * Create a line layer for primary route drawings. This layer will draw the shield, inner line of
     * primary route. If route eating is activated this layer will also draw this shield and inner line.
     * This method only create a layer, nothing is added to the map yet.
     *
     * @return a line layer for primary route drawings
     */
    public LineLayer createPrimaryRouteLayer(float routeScale, int routeColor, int drivenRouteColor) {
        return new LineLayer(PRIMARY_ROUTE_LAYER_ID, PRIMARY_ROUTE_SOURCE_ID)
                .withProperties(
                        lineJoin(
                                Property.LINE_JOIN_ROUND
                        ),
                        lineCap(
                                Property.LINE_CAP_ROUND
                        ),
                        lineWidth(
                                interpolate(
                                        exponential(1.5f), zoom(),
                                        stop(4f, product(literal(3f), literal(routeScale))),
                                        stop(10f, product(literal(4f), literal(routeScale))),
                                        stop(13f, product(literal(6f), literal(routeScale))),
                                        stop(16f, product(literal(10f), literal(routeScale))),
                                        stop(19f, product(literal(14f), literal(routeScale))),
                                        stop(22f, product(literal(18f), literal(routeScale)))
                                )
                        ),
                        lineColor(
                                switchCase(
                                        get(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY), color(drivenRouteColor),
                                        color(routeColor)
                                )
                        )
                );
    }

    public LineLayer createPrimaryRouteShieldLayer(float routeScale, int routeShieldColor, int drivenRouteShieldColor) {
        return new LineLayer(PRIMARY_ROUTE_SHIELD_LAYER_ID, PRIMARY_ROUTE_SOURCE_ID)
                .withProperties(
                        lineJoin(
                                Property.LINE_JOIN_ROUND
                        ),
                        lineCap(
                                Property.LINE_CAP_ROUND
                        ),
                        lineWidth(
                                interpolate(
                                        exponential(1.5f), zoom(),
                                        stop(10f, 7f),
                                        stop(14f, product(literal(10.5f), literal(routeScale))),
                                        stop(16.5f, product(literal(15.5f), literal(routeScale))),
                                        stop(19f, product(literal(24f), literal(routeScale))),
                                        stop(22f, product(literal(29f), literal(routeScale)))
                                )
                        ),
                        lineColor(
                                switchCase(
                                        get(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY), color(drivenRouteShieldColor),
                                        color(routeShieldColor)
                                )
                        )
                );
    }

    public LineLayer createPrimaryRouteCongestion(float routeScale,
                                                  int routeColor,
                                                  int routeModerateColor,
                                                  int routeHeavyColor,
                                                  int routeSevereColor,
                                                  int drivenRouteColor,
                                                  int drivenRouteModerateColor,
                                                  int drivenRouteHeavyColor,
                                                  int drivenRouteSevereColor) {
        return new LineLayer(PRIMARY_ROUTE_CONGESTION_LAYER_ID, PRIMARY_ROUTE_CONGESTION_SOURCE_ID)
                .withProperties(
                        lineJoin(
                                Property.LINE_JOIN_ROUND
                        ),
                        lineCap(
                                Property.LINE_CAP_BUTT
                        ),
                        lineWidth(
                                interpolate(
                                        exponential(1.5f), zoom(),
                                        stop(4f, product(literal(3f), literal(routeScale))),
                                        stop(10f, product(literal(4f), literal(routeScale))),
                                        stop(13f, product(literal(6f), literal(routeScale))),
                                        stop(16f, product(literal(10f), literal(routeScale))),
                                        stop(19f, product(literal(14f), literal(routeScale))),
                                        stop(22f, product(literal(18f), literal(routeScale)))
                                )
                        ),
                        lineColor(
                                switchCase(
                                        get(PRIMARY_DRIVEN_ROUTE_PROPERTY_KEY), match(
                                                Expression.toString(get(CONGESTION_KEY)),
                                                color(drivenRouteColor),
                                                stop("low", color(drivenRouteModerateColor)), //TODO: remove or add low color
                                                stop(MODERATE_CONGESTION_VALUE, color(drivenRouteModerateColor)),
                                                stop(HEAVY_CONGESTION_VALUE, color(drivenRouteHeavyColor)),
                                                stop(SEVERE_CONGESTION_VALUE, color(drivenRouteSevereColor))
                                        ),
                                        match(
                                                Expression.toString(get(CONGESTION_KEY)),
                                                color(routeColor),
                                                stop("low", color(routeModerateColor)), //TODO: remove or add low color
                                                stop(MODERATE_CONGESTION_VALUE, color(routeModerateColor)),
                                                stop(HEAVY_CONGESTION_VALUE, color(routeHeavyColor)),
                                                stop(SEVERE_CONGESTION_VALUE, color(routeSevereColor))
                                        )
                                )
                        )
                );
    }

    public LineLayer createAlternativeRouteLayer(float alternativeRouteScale, int alternativeRouteColor) {
        return new LineLayer(ALTERNATIVE_ROUTE_LAYER_ID, ALTERNATIVE_ROUTE_SOURCE_ID)
                .withProperties(
                        lineJoin(
                                Property.LINE_JOIN_ROUND
                        ),
                        lineCap(
                                Property.LINE_CAP_ROUND
                        ),
                        lineWidth(
                                interpolate(
                                        exponential(1.5f), zoom(),
                                        stop(4f, product(literal(3f), literal(alternativeRouteScale))),
                                        stop(10f, product(literal(4f), literal(alternativeRouteScale))),
                                        stop(13f, product(literal(6f), literal(alternativeRouteScale))),
                                        stop(16f, product(literal(10f), literal(alternativeRouteScale))),
                                        stop(19f, product(literal(14f), literal(alternativeRouteScale))),
                                        stop(22f, product(literal(18f), literal(alternativeRouteScale)))
                                )
                        ),
                        lineColor(
                                color(alternativeRouteColor)
                        )
                );
    }

    public LineLayer createAlternativeRoutesShieldLayer(float alternativeRouteScale, int alternativeRouteShieldColor) {
        return new LineLayer(ALTERNATIVE_ROUTE_SHIELD_LAYER_ID, ALTERNATIVE_ROUTE_SOURCE_ID)
                .withProperties(
                        lineJoin(
                                Property.LINE_JOIN_ROUND
                        ),
                        lineCap(
                                Property.LINE_CAP_ROUND
                        ),
                        lineWidth(
                                interpolate(
                                        exponential(1.5f), zoom(),
                                        stop(10f, 7f),
                                        stop(14f, product(literal(10.5f), literal(alternativeRouteScale))),
                                        stop(16.5f, product(literal(15.5f), literal(alternativeRouteScale))),
                                        stop(19f, product(literal(24f), literal(alternativeRouteScale))),
                                        stop(22f, product(literal(29f), literal(alternativeRouteScale)))
                                )
                        ),
                        lineColor(
                                color(alternativeRouteShieldColor)
                        )
                );
    }

    public SymbolLayer createWayPointLayer(Style style, Drawable originIcon, Drawable destinationIcon) {
        Bitmap bitmap = MapImageUtils.getBitmapFromDrawable(originIcon);
        style.addImage(ORIGIN_MARKER_NAME, bitmap);
        bitmap = MapImageUtils.getBitmapFromDrawable(destinationIcon);
        style.addImage(DESTINATION_MARKER_NAME, bitmap);

        return new SymbolLayer(WAYPOINT_LAYER_ID, WAYPOINT_SOURCE_ID).withProperties(
                iconImage(
                        match(
                                Expression.toString(get(WAYPOINT_PROPERTY_KEY)), literal(ORIGIN_MARKER_NAME),
                                stop(WAYPOINT_ORIGIN_VALUE, literal(ORIGIN_MARKER_NAME)),
                                stop(WAYPOINT_DESTINATION_VALUE, literal(DESTINATION_MARKER_NAME))
                        )),
                iconSize(
                        interpolate(
                                exponential(1.5f), zoom(),
                                stop(0f, 0.6f),
                                stop(10f, 0.8f),
                                stop(12f, 1.3f),
                                stop(22f, 2.8f)
                        )
                ),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
    }
}