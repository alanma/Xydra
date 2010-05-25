package org.xydra.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;



/**
 * A utility class which provides methods for serializing XModels and
 * XRepositories.
 * 
 * @author Kaidel
 * 
 */

public class XY {
	public static final String fileSuffix = ".xmd"; // the file suffix for
	
	// saving
	// XModels/Repositories
	
	/**
	 * Saves the given repository in the directory of the program as
	 * filename.xmd
	 * 
	 * @param repository The repository which will be saved
	 * @param filename The name of the file in which the repository will be
	 *            saved
	 * @throws IOException
	 */
	public static void saveRepository(XRepository repository, String filename) throws IOException {
		XY.saveRepository(repository, null, filename);
	}
	
	/**
	 * Saves the given repository in path/filename.xmd
	 * 
	 * @param repository The repository which will be saved
	 * @param path The directory in which the file will be saved
	 * @param name The name of the file in which the repository will be saved
	 * @throws IOException
	 */
	public static void saveRepository(XRepository repository, String path, String name)
	        throws IOException {
		String filename = name;
		if(!filename.endsWith(fileSuffix)) {
			filename += fileSuffix;
		}
		File file = new File(path, filename);
		
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(repository, out, true, false);
		writer.write(out.getXml());
		
		writer.close();
	}
	
	/**
	 * Saves the given model in the directory of the program as filename.xmd
	 * 
	 * @param model The model which will be saved
	 * @param filename The name of the file in which the model will be saved
	 * @throws IOException
	 */
	public static void saveModel(XModel model, String filename) throws IOException {
		XY.saveModel(model, null, filename);
	}
	
	/**
	 * Saves the given model in the directory path/filename.xmd
	 * 
	 * @param model The model which will be saved
	 * @param name The name of the file in which the model will be saved
	 * @throws IOException
	 */
	public static void saveModel(XModel model, String path, String name) throws IOException {
		String filename = name;
		if(!filename.endsWith(fileSuffix)) {
			filename += fileSuffix;
		}
		File file = new File(path, filename);
		
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(model, out, true, false);
		writer.write(out.getXml());
		
		writer.close();
		
	}
	
	/**
	 * Loads the given XRepository.
	 * 
	 * @param filename The filename of the repository to be loaded.
	 * @return The repository. null if the given filename doesn't exist or
	 *         doesn't hold a correct XRepository
	 * @throws IOException
	 */
	public static XRepository loadRepository(String filename) throws IOException {
		return XY.loadRepository(null, filename);
	}
	
	private static String readAll(InputStream stream) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[4096];
		Reader reader = new InputStreamReader(stream, "UTF-8");
		int nRead;
		while((nRead = reader.read(buf)) != -1)
			sb.append(buf, 0, nRead);
		return sb.toString();
	}
	
	/**
	 * Loads the given XRepository.
	 * 
	 * @param path The path of the file to be loaded.
	 * @param filename The filename of the repository to be loaded.
	 * @return The repository. null if the given filename doesn't exist or
	 *         doesn't hold a correct XRepository
	 * @throws IOException
	 */
	public static XRepository loadRepository(String path, String filename) throws IOException {
		File file = new File(path, filename);
		
		FileInputStream fin = new FileInputStream(file);
		
		String data = readAll(fin);
		MiniElement element = new MiniXMLParserImpl().parseXml(data);
		
		return XmlModel.toRepository(element);
	}
	
	/**
	 * Loads the given XModel.
	 * 
	 * @param filename The filename of the model to be loaded.
	 * 
	 * @return The model. null if the given filename doesn't exist or doesn't
	 *         hold a correct XModel
	 * @throws IOException
	 */
	public static XModel loadModel(String filename) throws IOException {
		return XY.loadModel(null, filename);
	}
	
	/**
	 * Loads the given XModel.
	 * 
	 * @param path The path of the file to be loaded.
	 * @param filename The filename of the model to be loaded.
	 * 
	 * @return The model. null if the given filename doesn't exist or doesn't
	 *         hold a correct XModel
	 * @throws IOException
	 */
	public static XModel loadModel(String path, String filename) throws IOException {
		File file = new File(path, filename);
		
		FileInputStream fin = new FileInputStream(file);
		
		String data = readAll(fin);
		MiniElement element = new MiniXMLParserImpl().parseXml(data);
		
		return XmlModel.toModel(element);
	}
	
}
