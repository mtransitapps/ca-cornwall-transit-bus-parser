package org.mtransit.parser.ca_cornwall_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Pattern;

// [OLD] http://maps.cornwall.ca/
// https://www.cornwall.ca/en/city-hall/open-data.aspx
public class CornwallTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CornwallTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Cornwall Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return false; // too complex
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // too complex
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (!CharUtils.isDigitsOnly(routeId)) {
			final String rsnS = gRoute.getRouteShortName();
			if (CharUtils.isDigitsOnly(rsnS)) {
				final int rsn = Integer.parseInt(rsnS);
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
				case 5:
					if ("MCCONNELL".equals(routeId)) {
						return 5_001L;
					}
					break;
				case 6:
					if ("CUMBERLAND".equals(routeId)) {
						return 6_001L;
					}
					break;
				case 7:
					if ("MONTREAL".equals(routeId)) {
						return 7_001L;
					}
					break;
				case 8:
					if ("BUSINESS PARK".equals(routeId)
							|| "BUSINESS PARK 8".equals(routeId)) {
						return 8_001L;
					}
					break;
				case 9:
					if ("BUSINESS PARK".equals(routeId)
							|| "BUSINESS PARK 9".equals(routeId)) {
						return 9_001L;
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
				case 20:
					if ("BUSINESS PARK 2".equals(routeId)) {
						return 20_002L;
					}
					break;
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
			if (routeId.startsWith("CANADA DAY")) {
				if ("CANADA DAY 1".equals(routeId)) {
					return 917_001L;
				} else if ("CANADA DAY 2".equals(routeId)) {
					return 917_002L;
				}
			}
			throw new MTLog.Fatal("%s: Unexpected route ID for %s!", rsnS, gRoute.toStringPlus());

		}
		return super.getRouteId(gRoute);
	}

	@NotNull
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		final String rsnS = gRoute.getRouteShortName();
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (CharUtils.isDigitsOnly(rsnS)) {
			int rsn = Integer.parseInt(rsnS);
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
			case 5:
				if ("MCCONNELL".equals(routeId)) {
					return "5 MC";
				}
				break;
			case 6:
				if ("CUMBERLAND".equals(routeId)) {
					return "4 CB";
				}
				break;
			case 7:
				if ("MONTREAL".equals(routeId)) {
					return "7 MT";
				}
				break;
			case 8:
				if ("BUSINESS PARK".equals(routeId)
						|| "BUSINESS PARK 8".equals(routeId)) {
					return "8 BP";
				}
				break;
			case 9:
				if ("BUSINESS PARK".equals(routeId)
						|| "BUSINESS PARK 9".equals(routeId)) {
					return "9 BP";
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
			case 20:
				return "20 BP";
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
		if (routeId.startsWith("CANADA DAY")) {
			if ("CANADA DAY 1".equals(routeId)) {
				return "CA 1";
			} else if ("CANADA DAY 2".equals(routeId)) {
				return "CA 2";
			}
		}
		throw new MTLog.Fatal("Unexpected route short name %s!", gRoute.toStringPlus());
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^[0-9]+-)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName);
		routeLongName = CleanUtils.fixMcXCase(routeLongName);
		routeLongName = STARTS_WITH_RSN.matcher(routeLongName).replaceAll(EMPTY);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_BLUE = "0072BC"; // BLUE (from PDF map)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
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
