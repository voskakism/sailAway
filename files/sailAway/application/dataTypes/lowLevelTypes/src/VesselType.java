// AIS Vessel Types as specified by IMO SOLAS, 46 U.S.C 2101 or 33 CFR 140.10
// Summary taken from the U.S. Coast Guard:
// http://www.uscg.mil/hq/cg5/TVNCOE/Documents/links/AIS.EncodingGuide.pdf
/*
0: Not available - DO NOT USE
1: Reserved for future use - DO NOT USE
2: Wing in Ground (WIG) or other vessels operating in U.S. waters, including the U.S. EEZ
	20: Wing in Ground effect vessels
	21: Engaged in towing other than barges by pushing ahead or hauling alongside (i.e. articulated tug-barges, push-boats, workboats); whose dimensions (ABCD values) solely represent the overall dimensions of the vessel
	22: Engaged in towing barges by pushing ahead or hauling alongside (i.e. articulated tug-barges, push-boats, workboats); whose dimensions (ABCD values) represent the overall rectangular dimensions of the vessel and its tow
	23: Light boats (i.e. push-boats or work boats not engaged in towing; whose dimensions (ABCD values) solely represent the dimensions of the vessel
	24: Mobile Offshore Drilling Units (MODUs), Lifeboats, Floating Production Systems (FPS), Floating Production Storage and Offloading Vessels (FPSO)
	25: Offshore Supply Vessels (OSV)
	26: Processing vessels (i.e. fish)
	27: School, scientific, research or training ships
	28: U.S. public or governmental vessels
	29: Autonomous or remotely-operated craft
3: Other vessels
	30: Engaged in fishing
	31: Engaged in towing by pulling (not pushing or hauling)
	32: Engaged in towing by pulling (not pushing or hauling) and length of the tow exceeds 200 meters (656 ft.)
	33: Engaged in dredging, or underwater operations (e.g. salvaging, surveying, but not diving)
	34: Engaged in diving operations
	35: Engaged in military operations
	36: Sailing vessels
	37: Pleasure craft
	38: Reserved for future use - DO NOT USE
	39: Reserved for future use - DO NOT USE
4: High speed craft (HSC) or passenger vessels of gross tonnage less than 100, including tenders
5: Special craft
	50: Pilot vessel
	51: Search and Rescue (SAR) vessels, i.e. USCG boats, USCG Auxiliary, assistance towers
	52: Harbor tugs
	53: Fish, offshore or port tenders
	54: Commercial response vessels with anti-pollution facilities or equipment
	55: Law enforcement vessels, i.e. USCG cutters, marine police
	56: Spare, for assignment to local vessels as designated by the USCG Captain of Port
	57: Spare, for assignment to local vessels involved in a marine event
	58: Medical transports (as defined in the 1949 Geneva Convention and Additional Protocols) or similar public safety vessels
	59: Ships according to RR Resolution No 18 (Mob-83)
6: Passenger ships of gross tonnage greater than 100
7: Cargo (freight) ships, including Integrated Tug-Barge (ITB) vessels
8: Tankers
9: Other types of ship
*/

// Merely a selection of AIS vessel types are included in the enum
public enum VesselType
{
	FISHING(30), //30
	TOWING(31), //31
	DREDGING(33), //33
	DIVING(34), //34
	MILITARY(35), //35
	SAILING(36), //36
	PLEASURE(37), //37
	HIGHSPEED(4), //4
	PILOT(50), //50
	SEARCHANDRESCUE(51), //51
	HARBORTUG(52), //52	
	ANTIPOLLUTION(54), //54
	LAWENFORCEMENT(55), //55
	MEDICAL(58), //58
	PASSENGER(6), //6
	CARGO(7), // 7
	TANKER(8), //8
	OTHER(9); //9
	
	private final int code; //will this work if declared final???
	
	public int getCode()
	{
		return code;
	}
	
	//Codes are uniquely assigned
	public static VesselType getVesselTypeForCode(int code)
	{
		VesselType result = null;
		for (VesselType vt : VesselType.values()){
			if(vt.getCode() == code){
				result = vt;
			}
		}
		return result;
	}
	
	private VesselType(int code)
	{
		this.code = code;
	}
}