# Consumer proguard rules for libandroid-navigation-ui

# --- Picasso ---
-dontwarn com.squareup.okhttp.**

# --- com.mapbox.services.android.navigation.v5.MapboxDirections ---
-dontwarn com.sun.xml.internal.ws.spi.db.BindingContextFactory

# --- com.amazonaws.util.json.JacksonFactory ---
-dontwarn com.fasterxml.jackson.core.**

# --- MapLibre ---
-dontwarn org.maplibre.navigation.android.ui.v5.**