package org.xydra.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.xydra.csv.CsvTable;
import org.xydra.csv.TableTools;


public class CsvTableTest {

	@Test
	public void testBasicUsage() {
		CsvTable ct = new CsvTable();
		ct.incrementValue("a", "1", 1);
		ct.incrementValue("a", "1", 1);
		Assert.assertEquals("2", ct.getValue("a", "1"));
		Assert.assertEquals(null, ct.getValue("a", "2"));

		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		ct.appendString("a", "2", "hello sunshine... ", 100);
		Assert.assertEquals(100, ct.getValue("a", "2").length());
	}

	@Test
	public void testGroupBy() throws IOException {
		CsvTable ct = new CsvTable();
		ct.setValueInitial("1", "name", "Jana");
		ct.setValueInitial("1", "profession", "computer science");
		ct.setValueInitial("1", "age", "21");
		ct.setValueInitial("1", "sex", "female");
		ct.setValueInitial("1", "shoesize", "5.5");

		ct.setValueInitial("2", "name", "Jim");
		ct.setValueInitial("2", "profession", "computer science");
		ct.setValueInitial("2", "age", "25");
		ct.setValueInitial("2", "sex", "male");
		ct.setValueInitial("2", "shoesize", "7.5");

		ct.setValueInitial("3", "name", "John");
		ct.setValueInitial("3", "profession", "firefighter");
		ct.setValueInitial("3", "age", "29");
		ct.setValueInitial("3", "sex", "male");
		ct.setValueInitial("3", "shoesize", "8");

		ct.setValueInitial("4", "name", "Jorge");
		ct.setValueInitial("4", "profession", "computer science");
		ct.setValueInitial("4", "age", "35");
		ct.setValueInitial("4", "sex", "male");
		ct.setValueInitial("4", "shoesize", "7");

		CsvTable result = new CsvTable();
		TableTools.groupBy(ct, Arrays.asList("profession", "sex"), Arrays
				.asList("age", "shoesize"), Arrays.asList("age", "shoesize"),
				Arrays.asList("age", "shoesize"), result);
		result.dump();
	}

	@Test
	public void testWriting() throws IOException {
		CsvTable ct = new CsvTable();
		ct.appendString("b", "1", "fil\"ex", 1000);
		ct.appendString("b", "2", "awerfasd", 1000);
		ct.appendString("b", "3", "waeraer,asdfsadf,asdfsadf,asdfsafd", 1000);
		ct.appendString("a", "1", "filex", 1000);
		ct.appendString("a", "2", "awerfasd", 1000);
		ct.appendString("a", "3", "waeraer,asdfsadf,asdfsadf,asdfsafd", 1000);
		ct.writeTo(new PrintWriter(System.out));
	}

	@Test
	public void testWritingAndRead() throws IOException {
		CsvTable ct = new CsvTable();
		ct.appendString("b", "1", "fil\"ex", 1000);
		ct.appendString("b", "2", "awerfasd", 1000);
		ct.appendString("b", "3", "waeraer,asdfsadf,asdfsadf,asdfsafd", 1000);
		ct.appendString("a", "1", "filex", 1000);
		ct.appendString("a", "2", "awerfasd", 1000);
		ct.appendString("a", "3", "waeraer,asdfsadf,asdfsadf,asdfsafd", 1000);

		byte[] content = toBytes(ct);
		ByteArrayInputStream bin = new ByteArrayInputStream(content);
		InputStreamReader isr = new InputStreamReader(bin);
		CsvTable ct2 = new CsvTable();
		ct2.readFrom(isr);

		System.out.println("ct2:");
		Writer w = new PrintWriter(System.out);
		ct2.writeTo(w);
		w.flush();
	}

	static byte[] toBytes(CsvTable table) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bos);
		table.writeTo(osw);
		osw.flush();
		return bos.toByteArray();
	}

	@Test
	public void testReadMany() throws IOException {
		CsvTable ctA = new CsvTable();
		ctA.appendString("a1", "x", "a1x", 1000);
		ctA.appendString("a1", "y", "a1y", 1000);
		ctA.appendString("a2", "x", "a2x", 1000);
		ctA.appendString("a2", "y", "a2y", 1000);
		byte[] contentA = toBytes(ctA);

		CsvTable ctB = new CsvTable();
		ctB.appendString("b1", "x", "b1x", 1000);
		ctB.appendString("b1", "y", "b1y", 1000);
		ctB.appendString("b2", "x", "b2x", 1000);
		ctB.appendString("b2", "y", "b2y", 1000);
		byte[] contentB = toBytes(ctB);

		CsvTable ctAB = new CsvTable();
		Reader readerA = toReader(contentA);
		ctAB.readFrom(readerA);
		readerA.close();
		Reader readerB = toReader(contentB);
		ctAB.readFrom(readerB);
		readerB.close();

		System.out.println("ct2:");
		Writer w = new PrintWriter(System.out);
		ctAB.writeTo(w);
		w.flush();
	}

	static Reader toReader(byte[] csvContent) {
		ByteArrayInputStream bin = new ByteArrayInputStream(csvContent);
		InputStreamReader isr = new InputStreamReader(bin);
		return isr;
	}
}
