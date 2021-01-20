package org.mtransit.parser.ca_cornwall_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// http://metrolinx.tmix.se/gtfs/gtfs-cornwall.zip
public class CornwallTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-cornwall-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CornwallTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Cornwall Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Cornwall Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (!Utils.isDigitsOnly(routeId)) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				switch (rsn) {
				case 1:
					if ("MCCONNELL".equals(routeId)) {
						return 1_001L;
					} else if ("PITT".equals(routeId)) {
						return 1_002L;
					}
					break;
				case 2:
					if ("CUMBERLAND".equals(routeId)) {
						return 2_001L;
					} else if ("SUNRISE".equals(routeId)) {
						return 2_002L;
					}
					break;
				case 3:
					if ("BROOKDALE".equals(routeId)) {
						return 3_001L;
					} else if ("MONTREAL".equals(routeId)) {
						return 3_002L;
					}
					break;
				case 4:
					if ("RIVERDALE".equals(routeId)) {
						return 4_001L;
					}
					break;
				case 12:
					if ("BUSINESS PARK 2".equals(routeId)) {
						return 12_002L;
					}
					if ("BUSINESS PARK 3".equals(routeId)) {
						return 12_003L;
					}
					break;
				case 14:
					if ("BUSINESS PARK 2".equals(routeId)) {
						return 14_002L;
					}
					break;
				case 17:
					return 17L;
				case 18:
					if ("BUSINESS PARK".equals(routeId)) {
						return 18_000L;
					}
					break;
				case 19:
					return 19L;
				case 61:
					if ("CS-EAST".equals(routeId)) {
						return 61_001L;
					} else if ("CS-WEST".equals(routeId)) {
						return 61_002L;
					}
					break;
				case 71:
					if ("EXPRESS EAST".equals(routeId)) {
						return 71_001L;
					} else if ("EXPRESS WEST".equals(routeId)) {
						return 71_002L;
					}
					break;
				case 88:
					return 88L;
				case 99:
					return 99L;
				}
			}
			throw new MTLog.Fatal("%s: Unexpected route ID for %s!", gRoute.getRouteShortName(), gRoute.toStringPlus());

		}
		return super.getRouteId(gRoute);
	}

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		final String rsnS = gRoute.getRouteShortName();
		if (Utils.isDigitsOnly(rsnS)) {
			int rsn = Integer.parseInt(rsnS);
			//noinspection deprecation
			final String routeId = gRoute.getRouteId();
			switch (rsn) {
			case 1:
				if ("MCCONNELL".equals(routeId)) {
					return "1 MC";
				} else if ("PITT".equals(routeId)) {
					return "1 PT";
				}
				break;
			case 2:
				if ("CUMBERLAND".equals(routeId)) {
					return "2 CB";
				} else if ("SUNRISE".equals(routeId)) {
					return "2 SR";
				}
				break;
			case 3:
				if ("BROOKDALE".equals(routeId)) {
					return "3 BD";
				} else if ("MONTREAL".equals(routeId)) {
					return "3 MT";
				}
				break;
			case 4:
				if ("RIVERDALE".equals(routeId)) {
					return "4 RV";
				}
				break;
			case 12:
				if ("BUSINESS PARK 2".equals(routeId)) {
					return "12 BP2";
				}
				if ("BUSINESS PARK 3".equals(routeId)) {
					return "12 BP3";
				}
				break;
			case 14:
				if ("BUSINESS PARK 2".equals(routeId)) {
					return "14 BP";
				}
				break;
			case 17:
				return "17 S3";
			case 18:
				if ("BUSINESS PARK".equals(routeId)) {
					return "18 BP";
				}
				break;
			case 19:
				return "19 BP";
			case 61:
				if ("CS-EAST".equals(routeId)) {
					return "61 E";
				} else if ("CS-WEST".equals(routeId)) {
					return "61 W";
				}
				break;
			case 71:
				if ("EXPRESS EAST".equals(routeId)) {
					return "71 E";
				} else if ("EXPRESS WEST".equals(routeId)) {
					return "71 W";
				}
				break;
			case 88:
				return "88 CA";
			case 99:
				return "99 BP";
			}
		}
		throw new MTLog.Fatal("Unexpected route short name %s!", gRoute.toStringPlus());
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^[0-9]+-)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName);
		routeLongName = CleanUtils.fixMcXCase(routeLongName);
		routeLongName = STARTS_WITH_RSN.matcher(routeLongName).replaceAll(EMPTY);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_BLUE = "0072BC"; // BLUE (from PDF map)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	// https://www.cornwall.ca/en/live-here/transit-routes.aspx
	// https://www.cornwall.ca/en/live-here/resources/Transit/Transit-MAP-Nov-2019.pdf
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
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
			throw new MTLog.Fatal("Unexpected route color %s!", gRoute);
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		final int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsign()),
				directionId
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_CORNWALL = Pattern.compile("(^Cornwall )", Pattern.CASE_INSENSITIVE);

	private static final Pattern COMMUNITY_SERVICE_ = CleanUtils.cleanWords("community service");
	private static final String COMMUNITY_SERVICE_REPLACEMENT = CleanUtils.cleanWordsReplacement("CS");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign);
		tripHeadsign = STARTS_WITH_CORNWALL.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = COMMUNITY_SERVICE_.matcher(tripHeadsign).replaceAll(COMMUNITY_SERVICE_REPLACEMENT);
		tripHeadsign = CleanUtils.fixMcXCase(tripHeadsign);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge %s VS %s!", mTrip, mTripToMerge);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.SAINT.matcher(gStopName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		gStopName = CleanUtils.fixMcXCase(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
