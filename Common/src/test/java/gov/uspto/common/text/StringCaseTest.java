package gov.uspto.common.text;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class StringCaseTest {

	// meta-analysis, anti-inflammatory, neo-orthodox, de-emphasize, re-enact, pre-election

	private static Map<String, String> TitleValidFromTo = new LinkedHashMap<String, String>();
	static {
		TitleValidFromTo.put(null, null); // null returns null.
		TitleValidFromTo.put("", ""); // empty returns empty.
		TitleValidFromTo.put(" Leading space ", "Leading Space"); // Test for Fixed index out of range when leading space.
		TitleValidFromTo.put("LED Lamp", "LED Lamp");
		TitleValidFromTo.put("Light emitting diode lamp", "Light Emitting Diode Lamp");
		TitleValidFromTo.put("LIGHT-EMITTING DIODE CIRCLE LAMP", "Light-emitting Diode Circle Lamp");
		TitleValidFromTo.put("PRICE-AFFIXING MACHINE", "Price-affixing Machine");
		TitleValidFromTo.put("IN-LINE MIXER", "In-line Mixer");
		TitleValidFromTo.put("Dual-Tip Marker", "Dual-tip Marker");
		TitleValidFromTo.put("LIRIOPE MUSCARI PLANT NAMED ‘LIRF’", "Liriope Muscari Plant Named 'LIRF'");
		TitleValidFromTo.put("CHESTNUT PLANT NAMED ‘AU BUCK IV’", "Chestnut Plant Named 'AU BUCK IV'");
		TitleValidFromTo.put("IN-WHEEL MOTOR WITH HIGH DURABILITY", "In-wheel Motor with High Durability");
		TitleValidFromTo.put("WIND-POWERED PNEUMATIC ENGINE AND A MOTOR VEHICLE EQUIPPED WITH THE ENGINE",
				"Wind-powered Pneumatic Engine and a Motor Vehicle Equipped with the Engine");
		TitleValidFromTo.put("MAGNETIC TILT AND RAISE/LOWER MECHANISMS FOR A VENETIAN BLIND", 
				"Magnetic Tilt and Raise/Lower Mechanisms for a Venetian Blind");
		TitleValidFromTo.put("DOUBLE OVER-DOOR HOOK", "Double Over-door Hook");
		TitleValidFromTo.put("SQUEEZE-AND-TURN CHILD RESISTANT CLOSURE", "Squeeze-and-Turn Child Resistant Closure");	
		TitleValidFromTo.put("Heating/cooling System for Indwelling Heat Exchange Catheter", "Heating/Cooling System for Indwelling Heat Exchange Catheter");
		TitleValidFromTo.put("Method and Apparatus for Satellite Positioning of Earth-Moving Equipment", 
				"Method and Apparatus for Satellite Positioning of Earth-moving Equipment");
		TitleValidFromTo.put("Attachment for Harvesting Stalk-Like Goods Where Each Cutting and Conveying Element Is Controlled Individually", 
				"Attachment for Harvesting Stalk-like Goods Where Each Cutting and Conveying Element Is Controlled Individually");		
		TitleValidFromTo.put("Method for Controlling the Feeding of a Web Substrate Into a Printing Press", 
				"Method for Controlling the Feeding of a Web Substrate into a Printing Press");
		TitleValidFromTo.put("Straddle-Type Vehicle", "Straddle-type Vehicle");
		TitleValidFromTo.put("Pressure-sensitive poly(N-vinyl lactam) adhesive composition and method for producing and using same", 
				"Pressure-Sensitive poly(N-vinyl lactam) Adhesive Composition and Method for Producing and Using Same");
		TitleValidFromTo.put("Triple-blade Ice Skating Footwear and Associated Method", "Triple-blade Ice Skating Footwear and Associated Method");
		TitleValidFromTo.put("RIDE-ON TURF MOWING MACHINE ROLLOVER PROTECTION ASSEMBLY", 
				"Ride-on Turf Mowing Machine Rollover Protection Assembly");
		TitleValidFromTo.put("Method and apparatus to increase the contrast ratio of the image produced by a LCoS based light engine", 
				"Method and Apparatus to Increase the Contrast Ratio of the Image Produced by a LCoS Based Light Engine");
		TitleValidFromTo.put("Method and apparatus for early diagnosis of Alzheimer's using non-invasive eye tomography by terahertz", 
				"Method and Apparatus for Early Diagnosis of Alzheimer's Using Non-invasive Eye Tomography by Terahertz");
		TitleValidFromTo.put("LED backlight device with deviated LED pitch", "LED Backlight Device with Deviated LED Pitch");
		TitleValidFromTo.put("Light-emitting module and light-emitting system", "Light-emitting Module and Light-emitting System");
		TitleValidFromTo.put("Emergency under-lighting systems for vehicles", "Emergency Under-lighting Systems for Vehicles");		
		TitleValidFromTo.put("Blender/food processor blade arrangement for small throated blender jars",
				"Blender/Food Processor Blade Arrangement for Small Throated Blender Jars");
		TitleValidFromTo.put("Liquid crystal display and backlight module thereof", "Liquid Crystal Display and Backlight Module Thereof");
		TitleValidFromTo.put("Ceiling mount for x-ray system", "Ceiling Mount for X-ray System");
		TitleValidFromTo.put("Method and apparatus for multi-directional fiber optic connection", 
				"Method and Apparatus for Multi-directional Fiber Optic Connection");
		
		// Motor-Driven Power Steering Unit Support Structure
		// Hand-Driven Wheelchair

		TitleValidFromTo.put("FOR THE PRESEDENT OF THE CLUB", "For the Presedent of the Club");
		TitleValidFromTo.put("PRESENT FOR TALKS", "Present for Talks");
		TitleValidFromTo.put("THE COMPUTER/LAPTOP FOR THE WORK GROUP", "The Computer/Laptop for the Work Group"); // slash
		TitleValidFromTo.put("CATCH ON FIRE DURING A HOT RE-ENTRY", "Catch on Fire During a Hot Re-entry"); // hyphen prefix repeat letter
		TitleValidFromTo.put("ATHLETES WHO RE-SIGN WITH THEIR TEAMS", "Athletes Who Re-sign with Their Teams"); // hyphen prefix no repeat letter
		TitleValidFromTo.put("COMPUTER E-MAIL SYSTEM", "Computer E-mail System"); // hyphen prefix first is not a word
		TitleValidFromTo.put("OVER-THE-COUNTER DRUGS", "Over-the-Counter Drugs");
		TitleValidFromTo.put("UP-TO-DATE SYSTEM", "Up-to-Date System");
		TitleValidFromTo.put("MID-SEPTEMBER", "Mid-september");
		TitleValidFromTo.put("DE-EMPHASIZE", "De-emphasize");
		TitleValidFromTo.put("RE-COVER", "Re-cover");
		TitleValidFromTo.put("CO-OCCUR", "Co-occur");
		TitleValidFromTo.put("SINGLE-CORE", "Single-core");
		TitleValidFromTo.put("MULTI-CORE", "Multi-core");
		TitleValidFromTo.put("DUAL-CORE", "Dual-core");
		TitleValidFromTo.put("TRIPLE-CORE", "Triple-core");
		TitleValidFromTo.put("QUAD-CORE", "Quad-core");
		TitleValidFromTo.put("CO\u2013OCCUR", "Co-occur"); // unicode \u2014 is en-dash which is transformed to dash.
		TitleValidFromTo.put("CO\u2014OCCUR", "Co-occur"); // unicode \u2014 is em-dash which is transformed to dash.
		TitleValidFromTo.put("LONG-TERM EFFECTS", "Long-Term Effects");
		TitleValidFromTo.put("WHY SUNLESS TANNING IS A HOT TREND", "Why Sunless Tanning Is a Hot Trend"); // short verb "is" should be uppercase.
	}

	@Test
	public void capitalizeFirstLetterTestString() {
		assertEquals("Computer", StringCaseUtil.capitalizeFirstLetter("computer"));
	}

	@Test
	public void capitalizeFirstLetterTestArray() {
		assertEquals(new String[]{"Computer", "Laptop"}, StringCaseUtil.capitalizeFirstLetter(new String[]{"computer", "laptop"}));
	}

	@Test
	public void TitleCaseTest(){
		for (Entry<String,String> validFromTo: TitleValidFromTo.entrySet()){
			assertEquals( validFromTo.getValue(), StringCaseUtil.toTitleCase(validFromTo.getKey()));
		}
	}

	@Test
	public void removeLowercaseTitleWords(){
		String name = "The Regents of the University of California";
		String[] words = StringCaseUtil.removeLowercaseTitleWords(name.split(" "));
		assertEquals("Regents", words[0]);
		assertEquals("University", words[1]);
		assertEquals("California", words[2]);		
	}

}
