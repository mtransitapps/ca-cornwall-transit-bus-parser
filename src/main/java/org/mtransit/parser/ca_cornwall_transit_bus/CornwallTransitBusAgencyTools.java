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
					return 1_001L;
				} else if ("PITT".equals(gRoute.getRouteId())) {
					return 1_002L;
				}
			} else if (gRoute.getRouteLongName().startsWith("2")) {
				if ("CUMBERLAND".equals(gRoute.getRouteId())) {
					return 2_001L;
				} else if ("SUNRISE".equals(gRoute.getRouteId())) {
					return 2_002L;
				}
			} else if (gRoute.getRouteLongName().startsWith("3")) {
				if ("BROOKDALE".equals(gRoute.getRouteId())) {
					return 3_001L;
				} else if ("MONTREAL".equals(gRoute.getRouteId())) {
					return 3_002L;
				}
			} else if (gRoute.getRouteLongName().startsWith("4")) {
				if ("RIVERDALE".equals(gRoute.getRouteId())) {
					return 4_001L;
				}
			} else if (gRoute.getRouteLongName().startsWith("61")) {
				return 61L;
			} else if (gRoute.getRouteLongName().startsWith("71")) {
				return 71L;
			} else if (gRoute.getRouteLongName().startsWith("88")) {
				return 88L;
			} else if (gRoute.getRouteLongName().startsWith("99")) {
				return 99L;
			}
			System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
			System.exit(-1);
			return -1L;
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
		} else if (gRoute.getRouteLongName().startsWith("71")) {
			return "71 EX";
		} else if (gRoute.getRouteLongName().startsWith("88")) {
			return "88 CA";
		} else if (gRoute.getRouteLongName().startsWith("99")) {
			return "99 BP";
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
		map2.put(1_001L, new RouteTripSpec(1_001L, // 1-MCCONNELL
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"418", // ++
								"440", // ++
								"401", // Glengarry & Third
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"401", // Glengarry & Third
								"402", // ++
								"413", // ++
								"122", // ++
								"124", // ++
								"125", // Second & Pitt
						})) //
				.compileBothTripSort());
		map2.put(1_002L, new RouteTripSpec(1_002L, // 1-PITT
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"418", // ++
								"624", // ++
								"633", // Thirteenth & Pitt
								"601", // Ross & Cornwall Ctre Rd
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"601", // Ross & Cornwall Ctre Rd
								"611", // ++
								"614", // ++
								"118", // ++
								"124", // ++
								"125", // Second & Pitt
						})) //
				.compileBothTripSort());
		map2.put(2_001L, new RouteTripSpec(2_001L, // 2-CUMBERLAND
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"332", // ++
								"301", // Tollgate & Brookdale
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"301", // Tollgate & Brookdale
								"311", // ++
								"125", // Second & Pitt
						})) //
				.compileBothTripSort());
		map2.put(2_002L, new RouteTripSpec(2_002L, // 2-SUNRISE
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"737", // ++ 2nd & Guy
								"6141", // Leitch & Anderson
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"6141", // Leitch & Anderson
								"708", // ++ Anderson & Shearer
								"712", // Walton & Ivan
								"718", // ++ 2nd & Eastcourt Mall
								"125", // Second & Pitt
						})) //
				.compileBothTripSort());
		map2.put(3_001L, new RouteTripSpec(3_001L, // 3-BROOKDALE
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"6146", // ==
								"221", // !=
								"222", // ==
								"201", // Tollgate & Brookdale
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"201", // Tollgate & Brookdale
								"217", //
								"125", // Second & Pitt
						})) //
				.compileBothTripSort());
		map2.put(3_002L, new RouteTripSpec(3_002L, // 3-MONTREAL
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"527", // ++
								"501", // Nav Centre (Main Entrance)
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"501", // Nav Centre (Main Entrance)
								"507", // ++
								"125", // Second & Pitt
						})) //
				.compileBothTripSort());
		map2.put(4_001L, new RouteTripSpec(4_001L, // 4-RIVERDALE
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"101", // Joyce & Riverdale
								"109", // ++
								"125", // Second & Pitt
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"125", // Second & Pitt
								"128", // ==
								"6148", // !=
								"129", // ==
								"141", // Joyce & Pescod
						})) //
				.compileBothTripSort());
		map2.put(61L, new RouteTripSpec(61L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"6101", //
								"6102", //
								"6114", //
								"6124", //
								"6126", //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"6126", //
								"6133", //
								"6137", //
								"6101", //
						})) //
				.compileBothTripSort());
		map2.put(71L, new RouteTripSpec(71L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // LOOP
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // LOOP
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"125", // <> xx Pitt & Second
								"219", // !=
								"329", // !=
								"241", // xx Walmart
								"225", // != Brookdale Centre
								"201", // Tollgate & Brookdale #NORTH
								"207", // !=
								"241", // xx Walmart
								"234", // !=
								"217", // !=
								"125", // <> xx Pitt & Second
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"125", // <> xx Pitt & Second
								"126", // !=
								"527", // !=
								"528", // xx Saint Lawrence College
								"529", // !=
								"534", // Montreal Rd. & Anthony #SOUTH
								"718", // Eastcourt Mall
								"508", // !=
								"528", // xx Saint Lawrence College
								"509", // !=
								"517", // !=
								"125", // <> xx Pitt & Second
						})) //
				.compileBothTripSort());
		map2.put(99L, new RouteTripSpec(99L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), // LOOP
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) // LOOP
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"6134", // Second & Seymour <=
								"240", //
								"9900", // xx Pitt & Second (Industrial Stop) <=
								"733", // !=
								"734", // <>
								"9909", // <> ==
								"9919", // <> !=
								"9914", // <>
								"9916", // <> !=
								"9918", // <> !=
								"9920", // <> ==
								"9917", // !=
								"9902", // !=
								"9910", // ==
								"9912", // xx
								"9913", // xx
								"9905", // !=
								"9903", // ==
								"9912", // xx
								"9913", // xx
								"716", // ==
								"727", // !=
								"418", // != <>
								"9900", // xx Pitt & Second (Industrial Stop) =>
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"6134", // Second & Seymour <=
								"240", // ==
								"323", // !=
								"124", // !=
								"9900", // xx Pitt & Second (Industrial Stop)
								"418", // != <>
								"624", // !=
								"628", // !=
								"734", // <>
								"9909", // <> ==
								"9919", // !=
								"9914", // <>
								"9918", // <> !=
								"9916", // <> !=
								"9920", // <> !=
								"9900", // xx Pitt & Second (Industrial Stop) =>
						})) //
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
