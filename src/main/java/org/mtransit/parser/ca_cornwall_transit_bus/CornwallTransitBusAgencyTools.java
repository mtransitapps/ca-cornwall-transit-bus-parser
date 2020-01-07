package org.mtransit.parser.ca_cornwall_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
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
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// http://metrolinx.tmix.se/gtfs/gtfs-cornwall.zip
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
		MTLog.log("Generating Cornwall Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		MTLog.log("Generating Cornwall Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				switch (rsn) {
				case 1:
					if ("MCCONNELL".equals(gRoute.getRouteId())) {
						return 1_001L;
					} else if ("PITT".equals(gRoute.getRouteId())) {
						return 1_002L;
					}
				case 2:
					if ("CUMBERLAND".equals(gRoute.getRouteId())) {
						return 2_001L;
					} else if ("SUNRISE".equals(gRoute.getRouteId())) {
						return 2_002L;
					}
				case 3:
					if ("BROOKDALE".equals(gRoute.getRouteId())) {
						return 3_001L;
					} else if ("MONTREAL".equals(gRoute.getRouteId())) {
						return 3_002L;
					}
				case 4:
					if ("RIVERDALE".equals(gRoute.getRouteId())) {
						return 4_001L;
					}
				case 17:
					return 17L;
				case 19:
					return 19L;
				case 61:
					if ("CS-EAST".equals(gRoute.getRouteId())) {
						return 61_001L;
					} else if ("CS-WEST".equals(gRoute.getRouteId())) {
						return 61_002L;
					}
				case 71:
					if ("EXPRESS EAST".equals(gRoute.getRouteId())) {
						return 71_001L;
					} else if ("EXPRESS WEST".equals(gRoute.getRouteId())) {
						return 71_002L;
					}
				case 88:
					return 88L;
				case 99:
					return 99L;
				}
			}
			MTLog.logFatal("Unexpected route ID for %s!", gRoute);
			return -1L;

		}
		return super.getRouteId(gRoute);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			case 1:
				if ("MCCONNELL".equals(gRoute.getRouteId())) {
					return "1 MC";
				} else if ("PITT".equals(gRoute.getRouteId())) {
					return "1 PT";
				}
			case 2:
				if ("CUMBERLAND".equals(gRoute.getRouteId())) {
					return "2 CB";
				} else if ("SUNRISE".equals(gRoute.getRouteId())) {
					return "2 SR";
				}
			case 3:
				if ("BROOKDALE".equals(gRoute.getRouteId())) {
					return "3 BD";
				} else if ("MONTREAL".equals(gRoute.getRouteId())) {
					return "3 MT";
				}
			case 4:
				if ("RIVERDALE".equals(gRoute.getRouteId())) {
					return "4 RV";
				}
			case 17:
				return "17 S3";
			case 19:
				return "19 BP";
			case 61:
				if ("CS-EAST".equals(gRoute.getRouteId())) {
					return "61 E";
				} else if ("CS-WEST".equals(gRoute.getRouteId())) {
					return "61 W";
				}
			case 71:
				if ("EXPRESS EAST".equals(gRoute.getRouteId())) {
					return "71 E";
				} else if ("EXPRESS WEST".equals(gRoute.getRouteId())) {
					return "71 W";
				}
			case 88:
				return "88 CA";
			case 99:
				return "99 BP";
			}
		}
		MTLog.logFatal("Unexpected route short name %s!", gRoute);
		return null;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^[0-9]+-)", Pattern.CASE_INSENSITIVE);

	@Override
	public String getRouteLongName(GRoute gRoute) {
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

	// https://www.cornwall.ca/en/live-here/transit-routes.aspx
	// https://www.cornwall.ca/en/live-here/resources/Transit/Transit-MAP-Nov-2019.pdf
	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
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
		}
		return super.getRouteColor(gRoute);
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		//noinspection UnnecessaryLocalVariable
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
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
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop
			gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
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
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), directionId);
	}

	private static final Pattern STARTS_WITH_CORNWALL = Pattern.compile("(^Cornwall )", Pattern.CASE_INSENSITIVE);

	private static final Pattern COMMUNITY_SERVICE_ = CleanUtils.cleanWords("community service");
	private static final String COMMUNITY_SERVICE_REPLACEMENT = CleanUtils.cleanWordsReplacement("CS");

	private static final Pattern MCCONNELL = CleanUtils.cleanWords("mcconnell");
	private static final String MCCONNELL_REPLACEMENT = CleanUtils.cleanWordsReplacement("McConnell");

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = STARTS_WITH_CORNWALL.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = COMMUNITY_SERVICE_.matcher(tripHeadsign).replaceAll(COMMUNITY_SERVICE_REPLACEMENT);
		tripHeadsign = MCCONNELL.matcher(tripHeadsign).replaceAll(MCCONNELL_REPLACEMENT);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		MTLog.logFatal("Unexpected trips to merge %s VS %s!", mTrip, mTripToMerge);
		return false;
	}

	private static final Pattern APARTMENTS = CleanUtils.cleanWords("apartments");
	private static final String APARTMENTS_REPLACEMENT = CleanUtils.cleanWordsReplacement("Apts");

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = APARTMENTS.matcher(gStopName).replaceAll(APARTMENTS_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.SAINT.matcher(gStopName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
