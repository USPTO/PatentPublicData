package gov.uspto.patent.model.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class PatentClassificationTest {

	@Test
	public void depth() throws ParseException {
		CpcClassification cpc = new CpcClassification("D07B22012051", false);
		cpc.parseText("D07B22012051");

		int depth = cpc.getTree().getDepth();
		int expect = 5;
	
		assertEquals(expect, depth);
	}

	@Test
	public void filerByType_ClassificationType() throws ParseException {
		List<PatentClassification> claz = new ArrayList<PatentClassification>();

		CpcClassification cpcClass = new CpcClassification("D21", false);
		cpcClass.parseText("D21");
		claz.add(cpcClass);

		IpcClassification ipcClass = new IpcClassification("D22", false);
		ipcClass.parseText("D22");
		claz.add(ipcClass);

		UspcClassification uspc = new UspcClassification("PLT101", false);
		uspc.parseText("PLT101");
		claz.add(uspc);

		Set<PatentClassification> ret = PatentClassification.filterByType(claz, ClassificationType.CPC);
		assertTrue(ret.size() == 1);
		assertEquals(cpcClass, ret.iterator().next());
		
		Set<PatentClassification> ret2 = PatentClassification.filterByType(claz, ClassificationType.IPC);
		assertTrue(ret2.size() == 1);
		assertEquals(ipcClass, ret2.iterator().next());

		Set<PatentClassification> ret3 = PatentClassification.filterByType(claz, ClassificationType.USPC);
		assertTrue(ret3.size() == 1);
		assertEquals(uspc, ret3.iterator().next());
	}

	@Test
	public void filerByType_Class() throws ParseException {
		List<PatentClassification> claz = new ArrayList<PatentClassification>();

		CpcClassification cpcClass = new CpcClassification("D21", false);
		cpcClass.parseText("D21");
		claz.add(cpcClass);

		IpcClassification ipcClass = new IpcClassification("D22", false);
		ipcClass.parseText("D22");
		claz.add(ipcClass);

		UspcClassification uspc = new UspcClassification("PLT101", false);
		uspc.parseText("PLT101");
		claz.add(uspc);

		Set<CpcClassification> ret = PatentClassification.filterByType(claz, CpcClassification.class);
		assertTrue(ret.size() == 1);
		assertEquals(cpcClass, ret.iterator().next());
		
		Set<IpcClassification> ret2 = PatentClassification.filterByType(claz, IpcClassification.class);
		assertTrue(ret2.size() == 1);
		assertEquals(ipcClass, ret2.iterator().next());

		Set<UspcClassification> ret3 = PatentClassification.filterByType(claz, UspcClassification.class);
		assertTrue(ret3.size() == 1);
		assertEquals(uspc, ret3.iterator().next());
	}

	@Test
	public void groupByType() throws ParseException {
		List<PatentClassification> claz = new ArrayList<PatentClassification>();

		CpcClassification cpcClass = new CpcClassification("D23", false);
		cpcClass.parseText("D23");
		claz.add(cpcClass);
		
		CpcClassification cpcClass2 = new CpcClassification("D21", false);
		cpcClass2.parseText("D21");
		claz.add(cpcClass2);

		IpcClassification ipcClass = new IpcClassification("D22", false);
		ipcClass.parseText("D22");
		claz.add(ipcClass);

		UspcClassification uspc = new UspcClassification("PLT101", false);
		uspc.parseText("PLT101");
		claz.add(uspc);

		Map<ClassificationType, Set<PatentClassification>> classes = PatentClassification.groupByType(claz);
		//classes.get(ClassificationType.CPC).forEach(System.out::println);
		//classes.get(ClassificationType.IPC).forEach(System.out::println);
		//classes.get(ClassificationType.USPC).forEach(System.out::println);

		assertEquals(classes.get(ClassificationType.CPC).size(), 2);
		CpcClassification ret1 = (CpcClassification) classes.get(ClassificationType.CPC).iterator().next();
		assertEquals(cpcClass2, ret1);
		
		assertEquals(classes.get(ClassificationType.IPC).size(), 1);
		assertEquals(classes.get(ClassificationType.USPC).size(), 1);
	}

}
