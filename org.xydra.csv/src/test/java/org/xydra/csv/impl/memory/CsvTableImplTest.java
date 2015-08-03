package org.xydra.csv.impl.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.xydra.csv.ICsvTable;

public class CsvTableImplTest {

	static byte[] toBytes(final ICsvTable table) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final OutputStreamWriter osw = new OutputStreamWriter(bos);
		table.writeTo(osw);
		osw.flush();
		return bos.toByteArray();
	}

	static Reader toReader(final byte[] csvContent) {
		final ByteArrayInputStream bin = new ByteArrayInputStream(csvContent);
		final InputStreamReader isr = new InputStreamReader(bin);
		return isr;
	}

	@Test
	public void testBasicUsage() {
		final CsvTable ct = new CsvTable();
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
	public void testReadMany() throws IOException {
		final CsvTable ctA = new CsvTable();
		ctA.appendString("a1", "x", "a1x", 1000);
		ctA.appendString("a1", "y", "a1y", 1000);
		ctA.appendString("a2", "x", "a2x", 1000);
		ctA.appendString("a2", "y", "a2y", 1000);
		final byte[] contentA = toBytes(ctA);

		final CsvTable ctB = new CsvTable();
		ctB.appendString("b1", "x", "b1x", 1000);
		ctB.appendString("b1", "y", "b1y", 1000);
		ctB.appendString("b2", "x", "b2x", 1000);
		ctB.appendString("b2", "y", "b2y", 1000);
		final byte[] contentB = toBytes(ctB);

		final ICsvTable ctAB = new CsvTable();
		final Reader readerA = toReader(contentA);
		ctAB.readFrom(readerA, true);
		readerA.close();
		final Reader readerB = toReader(contentB);
		ctAB.readFrom(readerB, true);
		readerB.close();

		System.out.println("ct2:");
		final Writer w = new PrintWriter(System.out);
		ctAB.writeTo(w);
		w.flush();
	}

	@Test
	public void testWriting() throws IOException {
		final CsvTable ct = new CsvTable();
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
		final CsvTable ct = new CsvTable();
		ct.appendString("b", "1", "fil\"ex", 1000);
		ct.appendString("b", "2", "awerfasd", 1000);
		ct.appendString("b", "3", "waeraer,asdfsadf,asdfsadf,asdfsafd", 1000);
		ct.appendString("a", "1", "filex", 1000);
		ct.appendString("a", "2", "awerfasd", 1000);
		ct.appendString("a", "3", "waeraer,asdfsadf,asdfsadf,asdfsafd", 1000);

		final byte[] content = toBytes(ct);
		final ByteArrayInputStream bin = new ByteArrayInputStream(content);
		final InputStreamReader isr = new InputStreamReader(bin);
		final ICsvTable ct2 = new CsvTable();
		ct2.readFrom(isr, true);

		System.out.println("ct2:");
		final Writer w = new PrintWriter(System.out);
		ct2.writeTo(w);
		w.flush();
	}

}
