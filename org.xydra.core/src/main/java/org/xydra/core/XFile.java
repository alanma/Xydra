package org.xydra.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.XmlOut;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStream;


/**
 * A utility class which provides simple methods for serializing XModels and
 * XRepositories into XML files.
 * 
 * @author Kaidel
 * 
 */
public class XFile {
	
	public static final String XML_SUFFIX = ".xml";
	public static final String MODEL_SUFFIX = ".xmodel" + XML_SUFFIX;
	public static final String REPOSITORY_SUFFIX = ".xrepository" + XML_SUFFIX;
	public static final String EVENTS_SUFFIX = ".xevents" + XML_SUFFIX;
	
	/**
	 * Saves the given {@link XBaseRepository} in the directory of the program
	 * as filename{@value #REPOSITORY_SUFFIX}.
	 * 
	 * @param repository The repository which will be saved
	 * @param filename The name of the file in which the repository will be
	 *            saved
	 * @throws IOException
	 */
	public static void saveRepository(XBaseRepository repository, String filename)
	        throws IOException {
		saveRepository(repository, null, filename);
	}
	
	/**
	 * Saves the given repository in path/filename{@value #REPOSITORY_SUFFIX}.
	 * 
	 * @param repository The repository which will be saved
	 * @param path The directory in which the file will be saved
	 * @param name The name of the file in which the repository will be saved
	 * @throws IOException
	 */
	public static void saveRepository(XBaseRepository repository, String path, String name)
	        throws IOException {
		saveRepository(repository, getFileForRepository(path, name));
	}
	
	private static File getFileForRepository(String path, String name) {
		
		String filename = name;
		if(!filename.endsWith(REPOSITORY_SUFFIX)) {
			filename += REPOSITORY_SUFFIX;
		}
		
		return new File(path, filename);
	}
	
	/**
	 * Saves the given repository in the given file.
	 * 
	 * @param repository The repository which will be saved
	 * @param file The file in which the repository will be saved
	 * @throws IOException
	 */
	public static void saveRepository(XBaseRepository repository, File file) throws IOException {
		
		FileOutputStream fos = new FileOutputStream(file);
		
		XmlOut out = new XmlOutStream(fos);
		XmlModel.toXml(repository, out, true, false, true);
		
		fos.close();
	}
	
	/**
	 * Saves the given model in the directory of the program as filename
	 * {@value #MODEL_SUFFIX}.
	 * 
	 * @param model The model which will be saved
	 * @param filename The name of the file in which the model will be saved
	 * @throws IOException
	 */
	public static void saveModel(XModel model, String filename) throws IOException {
		saveModel(model, null, filename);
	}
	
	/**
	 * Saves the given model in the directory path/filename
	 * {@value #MODEL_SUFFIX}.
	 * 
	 * @param model The model which will be saved
	 * @param path The directory in which the file will be saved
	 * @param name The name of the file in which the model will be saved
	 * @throws IOException
	 */
	public static void saveModel(XModel model, String path, String name) throws IOException {
		saveModel(model, getFileForModel(path, name));
	}
	
	private static File getFileForModel(String path, String name) {
		
		String filename = name;
		if(!filename.endsWith(MODEL_SUFFIX)) {
			filename += MODEL_SUFFIX;
		}
		
		return new File(path, filename);
	}
	
	/**
	 * Saves the given model in the given file.
	 * 
	 * @param model The model which will be saved
	 * @param name The name of the file in which the model will be saved
	 * @throws IOException
	 */
	public static void saveModel(XModel model, File file) throws IOException {
		
		FileOutputStream fos = new FileOutputStream(file);
		
		XmlOut out = new XmlOutStream(fos);
		XmlModel.toXml(model, out, true, false, true);
		
		fos.close();
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
		return loadRepository(null, filename);
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
	public static XRepository loadRepository(String path, String name) throws IOException {
		return loadRepository(getFileForRepository(path, name));
	}
	
	/**
	 * Loads the given XRepository.
	 * 
	 * @param file The file containing the repository to be loaded.
	 * @return The repository. null if the given file doesn't exist or doesn't
	 *         hold a correct XRepository
	 * @throws IOException
	 */
	public static XRepository loadRepository(File file) throws IOException {
		
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
		return loadModel(null, filename);
	}
	
	/**
	 * Loads the given XModel.
	 * 
	 * @param path The path of the file to be loaded.
	 * @param name The filename of the model to be loaded.
	 * 
	 * @return The model. null if the given filename doesn't exist or doesn't
	 *         hold a correct XModel
	 * @throws IOException
	 */
	public static XModel loadModel(String path, String name) throws IOException {
		return loadModel(getFileForModel(path, name));
	}
	
	/**
	 * Loads the given XModel.
	 * 
	 * @param file The file containing the model to be loaded.
	 * 
	 * @return The model. null if the given filename doesn't exist or doesn't
	 *         hold a correct XModel
	 * @throws IOException
	 */
	public static XModel loadModel(File file) throws IOException {
		
		FileInputStream fin = new FileInputStream(file);
		
		String data = readAll(fin);
		MiniElement element = new MiniXMLParserImpl().parseXml(data);
		
		return XmlModel.toModel(element);
	}
	
	// TODO load/save change log
	
}
