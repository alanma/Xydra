package org.xydra.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface ICsvTable extends ISparseTable {

	/**
	 * Dump table to System.out
	 * 
	 * @throws IOException
	 *             from System.out
	 */
	void dump() throws IOException;

	/**
	 * Dump table to System.out in LaTeX syntax
	 * 
	 * @throws IOException
	 *             from System.out
	 */
	void dumpToLaTeX() throws IOException;

	/**
	 * Add the content given in the reader as CSV to this CvsTable. Multiple
	 * CvsTable can be read in, the result is a merge.
	 * 
	 * The first row of the CVS file is interpreted as header names. The first
	 * column is interpreted as row names.
	 * 
	 * @param f
	 *            from which to read
	 * @throws IOException
	 *             if file reading fails
	 */
	void readFrom(File f) throws IOException;

	/**
	 * Add the content given in the reader as CSV to this CvsTable. Multiple
	 * CvsTable can be read in, the result is a merge.
	 * 
	 * The first row of the CVS file is interpreted as header names. The first
	 * column is interpreted as row names.
	 * 
	 * @param r
	 *            from which to read
	 * @param create
	 *            default should be true = row keys and valued should be unique.
	 *            Set to false to read more data into an already filled table
	 *            and you want to silently overwrite exsting values.
	 * @throws IOException
	 *             from the underlying reader
	 */
	void readFrom(Reader r, boolean create) throws IOException;

	/**
	 * How many rows should maximally be read? Default is -1 = unlimited. Note
	 * that more rows can be added later.
	 * 
	 * @param readMaxRows
	 *            set to -1 for unlimited (default)
	 */
	void setParamReadMaxRows(int readMaxRows);

	/**
	 * @param b
	 *            Default is false. If true, file writing is split into files
	 *            with 65535 records each, so that Excel can handle it.
	 */
	void setParamSplitWhenWritingLargeFiles(boolean b);

	void toLaTeX(Writer w) throws IOException;

	void writeTo(File f) throws FileNotFoundException;

	/**
	 * Writes a non-sparse CSV version of this tables contents
	 * 
	 * @param w
	 *            to which to write the CSV
	 * @throws IOException
	 *             from the writer
	 */
	void writeTo(Writer w) throws IOException;

	void writeTo(IRowHandler rowHandler) throws IOException;

	/**
	 * Writes a non-sparse CSV version of this tables contents. Writes only the
	 * data in [startRow,endRow).
	 * 
	 * @param w
	 *            to which to write the CSV
	 * @param startRow
	 *            inclusive
	 * @param endRow
	 *            exclusive
	 * @throws IOException
	 *             from the writer
	 * @throws ExcelLimitException
	 *             if using more than 65535 rows or more than 255 columns.
	 */
	void writeTo(Writer w, int startRow, int endRow) throws IOException, ExcelLimitException;

	/**
	 * @param f
	 * @param encoding
	 *            e.g. 'utf-8', 'x-MacRoman' or Big5 Big5-HKSCS EUC-JP EUC-KR
	 *            GB18030 GB2312 GBK IBM-Thai IBM00858 IBM01140 IBM01141
	 *            IBM01142 IBM01143 IBM01144 IBM01145 IBM01146 IBM01147 IBM01148
	 *            IBM01149 IBM037 IBM1026 IBM1047 IBM273 IBM277 IBM278 IBM280
	 *            IBM284 IBM285 IBM297 IBM420 IBM424 IBM437 IBM500 IBM775 IBM850
	 *            IBM852 IBM855 IBM857 IBM860 IBM861 IBM862 IBM863 IBM864 IBM865
	 *            IBM866 IBM868 IBM869 IBM870 IBM871 IBM918 ISO-2022-CN
	 *            ISO-2022-JP ISO-2022-JP-2 ISO-2022-KR ISO-8859-1 ISO-8859-13
	 *            ISO-8859-15 ISO-8859-2 ISO-8859-3 ISO-8859-4 ISO-8859-5
	 *            ISO-8859-6 ISO-8859-7 ISO-8859-8 ISO-8859-9 JIS_X0201
	 *            JIS_X0212-1990 KOI8-R KOI8-U Shift_JIS TIS-620 US-ASCII UTF-16
	 *            UTF-16BE UTF-16LE UTF-32 UTF-32BE UTF-32LE UTF-8 windows-1250
	 *            windows-1251 windows-1252 windows-1253 windows-1254
	 *            windows-1255 windows-1256 windows-1257 windows-1258
	 *            windows-31j x-Big5-HKSCS-2001 x-Big5-Solaris x-COMPOUND_TEXT
	 *            x-euc-jp-linux x-EUC-TW x-eucJP-Open x-IBM1006 x-IBM1025
	 *            x-IBM1046 x-IBM1097 x-IBM1098 x-IBM1112 x-IBM1122 x-IBM1123
	 *            x-IBM1124 x-IBM1364 x-IBM1381 x-IBM1383 x-IBM33722 x-IBM737
	 *            x-IBM833 x-IBM834 x-IBM856 x-IBM874 x-IBM875 x-IBM921 x-IBM922
	 *            x-IBM930 x-IBM933 x-IBM935 x-IBM937 x-IBM939 x-IBM942
	 *            x-IBM942C x-IBM943 x-IBM943C x-IBM948 x-IBM949 x-IBM949C
	 *            x-IBM950 x-IBM964 x-IBM970 x-ISCII91 x-ISO-2022-CN-CNS
	 *            x-ISO-2022-CN-GB x-iso-8859-11 x-JIS0208 x-JISAutoDetect
	 *            x-Johab x-MacArabic x-MacCentralEurope x-MacCroatian
	 *            x-MacCyrillic x-MacDingbat x-MacGreek x-MacHebrew x-MacIceland
	 *            x-MacRoman x-MacRomania x-MacSymbol x-MacThai x-MacTurkish
	 *            x-MacUkraine x-MS932_0213 x-MS950-HKSCS x-MS950-HKSCS-XP
	 *            x-mswin-936 x-PCK x-SJIS_0213 x-UTF-16LE-BOM X-UTF-32BE-BOM
	 *            X-UTF-32LE-BOM x-windows-50220 x-windows-50221 x-windows-874
	 *            x-windows-949 x-windows-950 x-windows-iso2022jp UTF-8
	 * 
	 * @throws IOException
	 */
	void readFrom(File f, String encoding) throws IOException;
}
