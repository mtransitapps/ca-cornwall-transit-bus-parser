package org.mtransit.parser.ca_cornwall_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://maps.cornwall.ca/
// http://maps.cornwall.ca/gtfs/CornwallGTFS.zip
public class CornwallTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-cornwall-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CornwallTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Cornwall Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Cornwall Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteId())) {
			if (gRoute.getRouteLongName().startsWith("1")) {
				if ("MCCONNELL".equals(gRoute.getRouteId())) {
					return 1001l;
				} else if ("PITT".equals(gRoute.getRouteId())) {
					return 1002l;
				}
			} else if (gRoute.getRouteLongName().startsWith("2")) {
				if ("CUMBERLAND".equals(gRoute.getRouteId())) {
					return 2001l;
				} else if ("SUNRISE".equals(gRoute.getRouteId())) {
					return 2002l;
				}
			} else if (gRoute.getRouteLongName().startsWith("3")) {
				if ("BROOKDALE".equals(gRoute.getRouteId())) {
					return 3001l;
				} else if ("MONTREAL".equals(gRoute.getRouteId())) {
					return 3002l;
				}
			} else if (gRoute.getRouteLongName().startsWith("4")) {
				if ("RIVERDALE".equals(gRoute.getRouteId())) {
					return 4001l;
				}
			} else if (gRoute.getRouteLongName().startsWith("61")) {
				return 61l;
			}
			System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
			System.exit(-1);
			return -1l;
		}
		return super.getRouteId(gRoute);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (gRoute.getRouteLongName().startsWith("1")) {
			if ("MCCONNELL".equals(gRoute.getRouteId())) {
				return "1 MC";
			} else if ("PITT".equals(gRoute.getRouteId())) {
				return "1 PT";
			}
		} else if (gRoute.getRouteLongName().startsWith("2")) {
			if ("CUMBERLAND".equals(gRoute.getRouteId())) {
				return "2 CB";
			} else if ("SUNRISE".equals(gRoute.getRouteId())) {
				return "2 SR";
			}
		} else if (gRoute.getRouteLongName().startsWith("3")) {
			if ("BROOKDALE".equals(gRoute.getRouteId())) {
				return "3 BD";
			} else if ("MONTREAL".equals(gRoute.getRouteId())) {
				return "3 MT";
			}
		} else if (gRoute.getRouteLongName().startsWith("4")) {
			if ("RIVERDALE".equals(gRoute.getRouteId())) {
				return "4 RV";
			}
		} else if (gRoute.getRouteLongName().startsWith("61")) {
			return "61 CS";
		}
		System.out.printf("\nUnexpected route short name %s!\n", gRoute);
		System.exit(-1);
		return null;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^[0-9]+\\-)", Pattern.CASE_INSENSITIVE);

	@Override
	public String getRouteLongName(GRoute gRoute) {
		if (gRoute.getRouteId().startsWith("CS-") && gRoute.getRouteLongName().startsWith("61-CS-")) {
			return "Community Service";
		}
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = routeLongName.toLowerCase(Locale.ENGLISH);
		routeLongName = MCCONNELL.matcher(routeLongName).replaceAll(MCCONNELL_REPLACEMENT);
		routeLongName = STARTS_WITH_RSN.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_BLUE = "0072BC"; // BLUE (from PDF map)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		int routeId = (int) getRouteId(gRoute);
		switch (routeId) {
		// @formatter:off
		case 1001: return "8DC63F"; // 1-MCCONNELL
		case 1002: return "F78F1E"; // 1-PITT
		case 2001: return "FFD200"; // 2-CUMBERLAND
		case 2002: return "0072BC"; // 2-SUNRISE
		case 3001: return "00AEEF"; // 3-BROOKDALE
		case 3002: return "EB4498"; // 3-MONTREAL
		case 4001: return "7D4199"; // 4-RIVERDALE
		case 61: return "1C3E94"; // 61-CS
		// @formatter:on
		}
		return super.getRouteColor(gRoute);
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(1001l, new RouteTripSpec(1001l, // 1-MCCONNELL
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "417", "428", "439" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "439", "440", "401", "408", "416", "417" })) //
				.compileBothTripSort());
		map2.put(1002l, new RouteTripSpec(1002l, // 1-PITT
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "622", "633", "645" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "645", "601", "611", "622" })) //
				.compileBothTripSort());

		map2.put(2001l, new RouteTripSpec(2001l, // 2-CUMBERLAND
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "321", "332", "301" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "301", "311", "320", "321" })) //
				.compileBothTripSort());
		map2.put(2002l, new RouteTripSpec(2002l, // 2-SUNRISE
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"729", "737", "701" //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"701", "708", "718", "728", "729" //
						})) //
				.compileBothTripSort());
		map2.put(3001l, new RouteTripSpec(3001l, // 3-BROOKDALE
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "218", "222", "223", "233", "224", "201" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "201", "204", "217", "218" })) //
				.compileBothTripSort());
		map2.put(3002l, new RouteTripSpec(3002l, // 3-MONTREAL
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"518", "519", "527", "528", "529", "537", "501" //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"501", "502", "503", "504", "505", "506", "507", "508", "509", "514", "517", "518" //
						})) //
				.compileBothTripSort());
		map2.put(4001l, new RouteTripSpec(4001l, // 4-RIVERDALE
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "101", "109", "125" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "125", "133", "101" })) //
				.compileBothTripSort());
		map2.put(61l, new RouteTripSpec(61l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6101", "6102", "6114", "6124", "6126" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6126", "6133", "6137", "6101" })) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		String tripHeadsign = gTrip.getTripHeadsign();
		if (StringUtils.isEmpty(tripHeadsign)) {
			tripHeadsign = mRoute.getLongName();
		}
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	private static final Pattern MCCONNELL = Pattern.compile("((^|\\W){1}(mcconnell)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String MCCONNELL_REPLACEMENT = "$2McConnell$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		tripHeadsign = MCCONNELL.matcher(tripHeadsign).replaceAll(MCCONNELL_REPLACEMENT);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern AND = Pattern.compile("((^|\\W){1}(and)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AND_REPLACEMENT = "$2&$4";

	private static final Pattern AT_SIGN = Pattern.compile("((^|\\W){1}(@)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AT_SIGN_REPLACEMENT = "$2/$4";

	private static final Pattern APARTMENTS = Pattern.compile("((^|\\W){1}(apartments)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String APARTMENTS_REPLACEMENT = "$2Apts$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AND.matcher(gStopName).replaceAll(AND_REPLACEMENT);
		gStopName = AT_SIGN.matcher(gStopName).replaceAll(AT_SIGN_REPLACEMENT);
		gStopName = APARTMENTS.matcher(gStopName).replaceAll(APARTMENTS_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
