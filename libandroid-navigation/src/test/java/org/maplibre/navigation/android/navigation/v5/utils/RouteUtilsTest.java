package org.maplibre.navigation.android.navigation.v5.utils;

import androidx.annotation.NonNull;

import org.maplibre.navigation.android.navigation.v5.BaseTest;
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions;
import org.maplibre.navigation.android.navigation.v5.models.BannerText;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.navigation.android.navigation.v5.models.VoiceInstructions;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.android.navigation.v5.milestone.BannerInstructionMilestone;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteLegProgress;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouteUtilsTest extends BaseTest {

  @Test
  public void isNewRoute_returnsTrueWhenPreviousGeometriesNull() throws Exception {
    RouteProgress defaultRouteProgress = buildDefaultTestRouteProgress();
    RouteUtils routeUtils = new RouteUtils();

    boolean isNewRoute = routeUtils.isNewRoute(null, defaultRouteProgress);

    assertTrue(isNewRoute);
  }

  @Test
  public void isNewRoute_returnsFalseWhenGeometriesEqualEachOther() throws Exception {
    RouteProgress previousRouteProgress = buildDefaultTestRouteProgress();
    RouteUtils routeUtils = new RouteUtils();

    boolean isNewRoute = routeUtils.isNewRoute(previousRouteProgress, previousRouteProgress);

    assertFalse(isNewRoute);
  }

  @Test
  public void isNewRoute_returnsTrueWhenGeometriesDoNotEqual() throws Exception {
    DirectionsRoute aRoute = buildTestDirectionsRoute();
    RouteProgress defaultRouteProgress = buildDefaultTestRouteProgress();
    RouteProgress previousRouteProgress = defaultRouteProgress.toBuilder()
      .directionsRoute(aRoute.toBuilder().geometry("vfejnqiv").build())
      .build();
    RouteUtils routeUtils = new RouteUtils();

    boolean isNewRoute = routeUtils.isNewRoute(previousRouteProgress, defaultRouteProgress);

    assertTrue(isNewRoute);
  }

  @Test
  public void isArrivalEvent_returnsTrueWhenManeuverTypeIsArrival_andIsLastInstruction() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    int first = 0;
    int lastInstruction = 1;
    RouteLeg routeLeg = route.legs().get(first);
    List<LegStep> routeSteps = routeLeg.steps();
    int currentStepIndex = routeSteps.size() - 2;
    int upcomingStepIndex = routeSteps.size() - 1;
    LegStep currentStep = routeSteps.get(currentStepIndex);
    LegStep upcomingStep = routeSteps.get(upcomingStepIndex);
    RouteProgress routeProgress = buildRouteProgress(first, route, currentStep, upcomingStep);
    BannerInstructionMilestone bannerInstructionMilestone = mock(BannerInstructionMilestone.class);
    List<BannerInstructions> currentStepBannerInstructions = currentStep.bannerInstructions();
    buildBannerInstruction(lastInstruction, bannerInstructionMilestone, currentStepBannerInstructions);

    RouteUtils routeUtils = new RouteUtils();

    boolean isArrivalEvent = routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone);

    assertTrue(isArrivalEvent);
  }

  @Test
  public void isArrivalEvent_returnsFalseWhenManeuverTypeIsArrival_andIsNotLastInstruction() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    int first = 0;
    RouteLeg routeLeg = route.legs().get(first);
    List<LegStep> routeSteps = routeLeg.steps();
    int currentStepIndex = routeSteps.size() - 2;
    int upcomingStepIndex = routeSteps.size() - 1;
    LegStep currentStep = routeSteps.get(currentStepIndex);
    LegStep upcomingStep = routeSteps.get(upcomingStepIndex);
    RouteProgress routeProgress = buildRouteProgress(first, route, currentStep, upcomingStep);
    BannerInstructionMilestone bannerInstructionMilestone = mock(BannerInstructionMilestone.class);
    List<BannerInstructions> currentStepBannerInstructions = currentStep.bannerInstructions();
    buildBannerInstruction(first, bannerInstructionMilestone, currentStepBannerInstructions);

    RouteUtils routeUtils = new RouteUtils();

    boolean isArrivalEvent = routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone);

    assertFalse(isArrivalEvent);
  }

  @Test
  public void isArrivalEvent_returnsFalseWhenManeuverTypeIsNotArrival() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    int first = 0;
    RouteLeg routeLeg = route.legs().get(first);
    List<LegStep> routeSteps = routeLeg.steps();
    LegStep currentStep = routeSteps.get(first);
    LegStep upcomingStep = routeSteps.get(first + 1);
    RouteProgress routeProgress = buildRouteProgress(first, route, currentStep, upcomingStep);
    BannerInstructionMilestone bannerInstructionMilestone = mock(BannerInstructionMilestone.class);
    List<BannerInstructions> currentStepBannerInstructions = currentStep.bannerInstructions();
    buildBannerInstruction(first, bannerInstructionMilestone, currentStepBannerInstructions);

    RouteUtils routeUtils = new RouteUtils();

    boolean isArrivalEvent = routeUtils.isArrivalEvent(routeProgress, bannerInstructionMilestone);

    assertFalse(isArrivalEvent);
  }

  @Test
  public void isValidRouteProfile_returnsTrueWithDrivingTrafficProfile() throws Exception {
    String routeProfileDrivingTraffic = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC;
    RouteUtils routeUtils = new RouteUtils();

    boolean isValidProfile = routeUtils.isValidRouteProfile(routeProfileDrivingTraffic);

    assertTrue(isValidProfile);
  }

  @Test
  public void isValidRouteProfile_returnsTrueWithDrivingProfile() throws Exception {
    String routeProfileDriving = DirectionsCriteria.PROFILE_DRIVING;
    RouteUtils routeUtils = new RouteUtils();

    boolean isValidProfile = routeUtils.isValidRouteProfile(routeProfileDriving);

    assertTrue(isValidProfile);
  }

  @Test
  public void isValidRouteProfile_returnsTrueWithCyclingProfile() throws Exception {
    String routeProfileCycling = DirectionsCriteria.PROFILE_CYCLING;
    RouteUtils routeUtils = new RouteUtils();

    boolean isValidProfile = routeUtils.isValidRouteProfile(routeProfileCycling);

    assertTrue(isValidProfile);
  }

  @Test
  public void isValidRouteProfile_returnsTrueWithWalkingProfile() throws Exception {
    String routeProfileWalking = DirectionsCriteria.PROFILE_WALKING;
    RouteUtils routeUtils = new RouteUtils();

    boolean isValidProfile = routeUtils.isValidRouteProfile(routeProfileWalking);

    assertTrue(isValidProfile);
  }

  @Test
  public void isValidRouteProfile_returnsFalseWithInvalidProfile() throws Exception {
    String invalidProfile = "invalid_profile";
    RouteUtils routeUtils = new RouteUtils();

    boolean isValidProfile = routeUtils.isValidRouteProfile(invalidProfile);

    assertFalse(isValidProfile);
  }

  @Test
  public void isValidRouteProfile_returnsFalseWithNullProfile() throws Exception {
    String nullProfile = null;
    RouteUtils routeUtils = new RouteUtils();

    boolean isValidProfile = routeUtils.isValidRouteProfile(nullProfile);

    assertFalse(isValidProfile);
  }

  @Test
  public void findCurrentBannerInstructions_returnsNullWithNullCurrentStep() throws Exception {
    LegStep currentStep = null;
    double stepDistanceRemaining = 0;
    RouteUtils routeUtils = new RouteUtils();

    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
      currentStep, stepDistanceRemaining
    );

    assertNull(currentBannerInstructions);
  }

  @Test
  public void findCurrentBannerInstructions_returnsNullWithCurrentStepEmptyInstructions() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    List<BannerInstructions> currentInstructions = currentStep.bannerInstructions();
    currentInstructions.clear();
    RouteUtils routeUtils = new RouteUtils();

    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
      currentStep, stepDistanceRemaining
    );

    assertNull(currentBannerInstructions);
  }

  @Test
  public void findCurrentBannerInstructions_returnsCorrectCurrentInstruction() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
      currentStep, stepDistanceRemaining
    );

    assertEquals(currentStep.bannerInstructions().get(0), currentBannerInstructions);
  }

  @Test
  public void findCurrentBannerInstructions_adjustedDistanceRemainingReturnsCorrectInstruction() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepIndex(1)
      .stepDistanceRemaining(50)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
      currentStep, stepDistanceRemaining
    );

    assertEquals(currentStep.bannerInstructions().get(1), currentBannerInstructions);
  }

  @Test
  public void findCurrentBannerInstructions_adjustedDistanceRemainingRemovesCorrectInstructions() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepIndex(1)
      .stepDistanceRemaining(500)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    BannerInstructions currentBannerInstructions = routeUtils.findCurrentBannerInstructions(
      currentStep, stepDistanceRemaining
    );

    assertEquals(currentStep.bannerInstructions().get(0), currentBannerInstructions);
  }

  @Test
  public void findCurrentBannerText_returnsCorrectPrimaryBannerText() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepIndex(1)
      .stepDistanceRemaining(50)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    BannerText currentBannerText = routeUtils.findCurrentBannerText(
      currentStep, stepDistanceRemaining, true
    );

    assertEquals(currentStep.bannerInstructions().get(1).primary(), currentBannerText);
  }

  @Test
  public void findCurrentBannerText_returnsCorrectSecondaryBannerText() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepIndex(1)
      .stepDistanceRemaining(50)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    BannerText currentBannerText = routeUtils.findCurrentBannerText(
      currentStep, stepDistanceRemaining, false
    );

    assertEquals(currentStep.bannerInstructions().get(1).secondary(), currentBannerText);
  }

  @Test
  public void findCurrentBannerText_returnsNullWithNullCurrentStep() throws Exception {
    LegStep currentStep = null;
    double stepDistanceRemaining = 0;
    RouteUtils routeUtils = new RouteUtils();

    BannerText currentBannerText = routeUtils.findCurrentBannerText(
      currentStep, stepDistanceRemaining, false
    );

    assertNull(currentBannerText);
  }

  @Test
  public void findCurrentVoiceInstructions_returnsNullWithNullCurrentStep() throws Exception {
    LegStep currentStep = null;
    double stepDistanceRemaining = 0;
    RouteUtils routeUtils = new RouteUtils();

    VoiceInstructions currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
      currentStep, stepDistanceRemaining
    );

    assertNull(currentVoiceInstructions);
  }

  @Test
  public void findCurrentVoiceInstructions_returnsNullWithCurrentStepEmptyInstructions() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    List<VoiceInstructions> currentInstructions = currentStep.voiceInstructions();
    currentInstructions.clear();
    RouteUtils routeUtils = new RouteUtils();

    VoiceInstructions voiceInstructions = routeUtils.findCurrentVoiceInstructions(
      currentStep, stepDistanceRemaining
    );

    assertNull(voiceInstructions);
  }

  @Test
  public void findCurrentVoiceInstructions_returnsCorrectInstructionsBeginningOfStepDistanceRemaining() throws
    Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepIndex(1)
      .stepDistanceRemaining(300)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    VoiceInstructions currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
      currentStep, stepDistanceRemaining
    );

    assertEquals(currentStep.voiceInstructions().get(1), currentVoiceInstructions);
  }

  @Test
  public void findCurrentVoiceInstructions_returnsCorrectInstructionsNoDistanceTraveled() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepDistanceRemaining(routeProgress.currentLegProgress().currentStep().distance())
      .stepIndex(0)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    VoiceInstructions currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
      currentStep, stepDistanceRemaining
    );

    assertEquals(currentStep.voiceInstructions().get(0), currentVoiceInstructions);
  }

  @Test
  public void findCurrentVoiceInstructions_returnsCorrectInstructionsEndOfStepDistanceRemaining() throws Exception {
    RouteProgress routeProgress = buildDefaultTestRouteProgress();
    routeProgress = routeProgress.toBuilder()
      .stepIndex(1)
      .stepDistanceRemaining(50)
      .build();
    LegStep currentStep = routeProgress.currentLegProgress().currentStep();
    double stepDistanceRemaining = routeProgress.currentLegProgress().currentStepProgress().distanceRemaining();
    RouteUtils routeUtils = new RouteUtils();

    VoiceInstructions currentVoiceInstructions = routeUtils.findCurrentVoiceInstructions(
      currentStep, stepDistanceRemaining
    );

    assertEquals(currentStep.voiceInstructions().get(2), currentVoiceInstructions);
  }

  @Test
  public void calculateRemainingWaypoints() {
    DirectionsRoute route = mock(DirectionsRoute.class);
    RouteOptions routeOptions = mock(RouteOptions.class);
    when(routeOptions.coordinates()).thenReturn(buildCoordinateList());
    when(route.routeOptions()).thenReturn(routeOptions);
    RouteProgress routeProgress = mock(RouteProgress.class);
    when(routeProgress.remainingWaypoints()).thenReturn(2);
    when(routeProgress.directionsRoute()).thenReturn(route);
    RouteUtils routeUtils = new RouteUtils();

    List<Point> remainingWaypoints = routeUtils.calculateRemainingWaypoints(routeProgress);

    assertEquals(2, remainingWaypoints.size());
    assertEquals(Point.fromLngLat(7.890, 1.234), remainingWaypoints.get(0));
    assertEquals(Point.fromLngLat(5.678, 9.012), remainingWaypoints.get(1));
  }

  @Test
  public void calculateRemainingWaypoints_handlesNullOptions() {
    DirectionsRoute route = mock(DirectionsRoute.class);
    when(route.routeOptions()).thenReturn(null);
    RouteProgress routeProgress = mock(RouteProgress.class);
    when(routeProgress.remainingWaypoints()).thenReturn(2);
    when(routeProgress.directionsRoute()).thenReturn(route);
    RouteUtils routeUtils = new RouteUtils();

    List<Point> remainingWaypoints = routeUtils.calculateRemainingWaypoints(routeProgress);

    assertNull(remainingWaypoints);
  }

  @Test
  public void calculateRemainingWaypointNames() {
    DirectionsRoute route = mock(DirectionsRoute.class);
    RouteOptions routeOptions = mock(RouteOptions.class);
    when(routeOptions.coordinates()).thenReturn(buildCoordinateList());
    when(routeOptions.waypointNames()).thenReturn("first;second;third;fourth");
    when(route.routeOptions()).thenReturn(routeOptions);
    RouteProgress routeProgress = mock(RouteProgress.class);
    when(routeProgress.remainingWaypoints()).thenReturn(2);
    when(routeProgress.directionsRoute()).thenReturn(route);
    RouteUtils routeUtils = new RouteUtils();

    String[] remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(routeProgress);

    assertEquals(3, remainingWaypointNames.length);
    assertEquals("first", remainingWaypointNames[0]);
    assertEquals("third", remainingWaypointNames[1]);
    assertEquals("fourth", remainingWaypointNames[2]);
  }

  @Test
  public void calculateRemainingWaypointNames_handlesNullOptions() {
    DirectionsRoute route = mock(DirectionsRoute.class);
    when(route.routeOptions()).thenReturn(null);
    RouteProgress routeProgress = mock(RouteProgress.class);
    when(routeProgress.remainingWaypoints()).thenReturn(2);
    when(routeProgress.directionsRoute()).thenReturn(route);
    RouteUtils routeUtils = new RouteUtils();

    String[] remainingWaypointNames = routeUtils.calculateRemainingWaypointNames(routeProgress);

    assertNull(remainingWaypointNames);
  }

  @NonNull
  private RouteProgress buildRouteProgress(int first, DirectionsRoute route, LegStep currentStep,
                                           LegStep upcomingStep) {
    RouteProgress routeProgress = mock(RouteProgress.class);
    RouteLegProgress legProgress = mock(RouteLegProgress.class);
    when(legProgress.currentStep()).thenReturn(currentStep);
    when(legProgress.upComingStep()).thenReturn(upcomingStep);
    when(routeProgress.currentLegProgress()).thenReturn(legProgress);
    when(routeProgress.directionsRoute()).thenReturn(route);
    when(routeProgress.currentLeg()).thenReturn(route.legs().get(first));
    return routeProgress;
  }

  private void buildBannerInstruction(int first, BannerInstructionMilestone bannerInstructionMilestone,
                                      List<BannerInstructions> currentStepBannerInstructions) {
    BannerInstructions bannerInstructions = currentStepBannerInstructions.get(first);
    when(bannerInstructionMilestone.getBannerInstructions()).thenReturn(bannerInstructions);
  }

  private List<Point> buildCoordinateList() {
    List<Point> coordinates = new ArrayList<>();
    coordinates.add(Point.fromLngLat(1.234, 5.678));
    coordinates.add(Point.fromLngLat(9.012, 3.456));
    coordinates.add(Point.fromLngLat(7.890, 1.234));
    coordinates.add(Point.fromLngLat(5.678, 9.012));
    return coordinates;
  }
}