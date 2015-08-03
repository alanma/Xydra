package org.xydra.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;


/**
 * A utility class which provides simple methods for serializing XModels and
 * XRepositories into XML files.
 *
 * @author kaidel
 *
 */
@RunsInGWT(false)
public class XFile {

    public static final String XML_SUFFIX = ".xml";
    public static final String EVENTS_SUFFIX = ".xevents" + XML_SUFFIX;
    public static final String MODEL_SUFFIX = ".xmodel" + XML_SUFFIX;
    public static final String REPOSITORY_SUFFIX = ".xrepository" + XML_SUFFIX;

    private static File getFileForModel(final String path, final String name) {

        String filename = name;
        if(!filename.endsWith(MODEL_SUFFIX)) {
            filename += MODEL_SUFFIX;
        }

        return new File(path, filename);
    }

    private static File getFileForRepository(final String path, final String name) {

        String filename = name;
        if(!filename.endsWith(REPOSITORY_SUFFIX)) {
            filename += REPOSITORY_SUFFIX;
        }

        return new File(path, filename);
    }

    /**
     * Loads the given XModel.
     *
     * @param actorId actorId The initial session actor for the loaded mode.
     * @param passwordHash
     * @param file The file containing the model to be loaded.
     *
     * @return The model. null if the given filename doesn't exist or doesn't
     *         hold a correct XModel
     * @throws IOException If there was an error reading the file.
     */
    public static XModel loadModel(final XId actorId, final String passwordHash, final File file) throws IOException {
        final FileInputStream fin = new FileInputStream(file);
        return loadModel(actorId, passwordHash, fin);
    }

    /**
     * @param actorId
     * @param passwordHash
     * @param inputStream assuming UTF-8 encoded character data
     * @return the loaded XModel
     * @throws IOException If there was an error reading the stream.
     */
    public static XModel loadModel(final XId actorId, final String passwordHash, final InputStream inputStream)
            throws IOException {
        final String data = readAll(inputStream);
        final XydraElement element = new XmlParser().parse(data);

        return SerializedModel.toModel(actorId, passwordHash, element);
    }

    /**
     * Loads the given XModel.
     *
     * @param actorId actorId The initial session actor for the loaded mode.
     * @param passwordHash
     * @param filename The filename of the model to be loaded.
     *
     * @return The model. null if the given filename doesn't exist or doesn't
     *         hold a correct XModel
     * @throws IOException If there was an error reading the file.
     */
    public static XModel loadModel(final XId actorId, final String passwordHash, final String filename)
            throws IOException {
        return loadModel(actorId, passwordHash, null, filename);
    }

    /**
     * Loads the given XModel.
     *
     * @param actorId actorId The initial session actor for the loaded mode.
     * @param passwordHash
     * @param path The path of the file to be loaded.
     * @param name The filename of the model to be loaded.
     *
     * @return The model. null if the given filename doesn't exist or doesn't
     *         hold a correct XModel
     * @throws IOException If there was an error reading the file.
     */
    public static XModel loadModel(final XId actorId, final String passwordHash, final String path, final String name)
            throws IOException {
        return loadModel(actorId, passwordHash, getFileForModel(path, name));
    }

    /**
     * Loads the given XRepository.
     *
     * @param actorId actorId The initial session actor for the loaded mode.
     * @param passwordHash
     * @param file The file containing the repository to be loaded.
     *
     * @return The repository. null if the given file doesn't exist or doesn't
     *         hold a correct XRepository
     * @throws IOException If there was an error reading the file.
     */
    public static XRepository loadRepository(final XId actorId, final String passwordHash, final File file)
            throws IOException {

        final FileInputStream fin = new FileInputStream(file);

        final String data = readAll(fin);
        final XydraElement element = new XmlParser().parse(data);

        return SerializedModel.toRepository(actorId, passwordHash, element);
    }

    /**
     * Loads the given XRepository.
     *
     * @param actorId actorId The initial session actor for the loaded mode.
     * @param passwordHash
     * @param filename The filename of the repository to be loaded.
     *
     * @return The repository. null if the given filename doesn't exist or
     *         doesn't hold a correct XRepository
     * @throws IOException If there was an error reading the file.
     */
    public static XRepository loadRepository(final XId actorId, final String passwordHash, final String filename)
            throws IOException {
        return loadRepository(actorId, passwordHash, null, filename);
    }

    /**
     * Loads the given XRepository.
     *
     * @param actorId The initial session actor for the loaded mode.
     * @param passwordHash
     * @param path The path of the file to be loaded.
     * @param filename The filename of the repository to be loaded.
     *
     * @return The repository. null if the given filename doesn't exist or
     *         doesn't hold a correct XRepository
     * @throws IOException If there was an error reading the file.
     */
    public static XRepository loadRepository(final XId actorId, final String passwordHash, final String path,
            final String filename) throws IOException {
        return loadRepository(actorId, passwordHash, getFileForRepository(path, filename));
    }

    private static String readAll(final InputStream stream) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final char[] buf = new char[4096];
        final Reader reader = new InputStreamReader(stream, "UTF-8");
        int nRead;
        while((nRead = reader.read(buf)) != -1) {
			sb.append(buf, 0, nRead);
		}
        return sb.toString();
    }

    /**
     * Saves the given model in the given file.
     *
     * @param model The model which will be saved
     * @param file The file in which the model will be saved
     * @throws IOException If there was an error reading the file.
     */
    public static void saveModel(final XModel model, final File file) throws IOException {

        final FileOutputStream fos = new FileOutputStream(file);

        final XydraOut out = new XmlOut(new MiniStreamWriter(fos));
        SerializedModel.serialize(model, out, true, false, true);

        fos.close();
    }

    /**
     * Saves the given model in the directory of the program as filename
     * {@value #MODEL_SUFFIX}.
     *
     * @param model The model which will be saved
     * @param filename The name of the file in which the model will be saved
     * @throws IOException If there was an error reading the file.
     */
    public static void saveModel(final XModel model, final String filename) throws IOException {
        saveModel(model, null, filename);
    }

    /**
     * Saves the given model in the directory path/filename
     * {@value #MODEL_SUFFIX}.
     *
     * @param model The model which will be saved
     * @param path The directory in which the file will be saved
     * @param name The name of the file in which the model will be saved
     * @throws IOException If there was an error reading the file.
     */
    public static void saveModel(final XModel model, final String path, final String name) throws IOException {
        saveModel(model, getFileForModel(path, name));
    }

    /**
     * Saves the given repository in the given file.
     *
     * @param repository The repository which will be saved
     * @param file The file in which the repository will be saved
     * @throws IOException If there was an error reading the file.
     */
    public static void saveRepository(final XReadableRepository repository, final File file) throws IOException {

        final FileOutputStream fos = new FileOutputStream(file);

        final XydraOut out = new XmlOut(new MiniStreamWriter(fos));
        SerializedModel.serialize(repository, out, true, false, true);

        fos.close();
    }

    /**
     * Saves the given {@link XReadableRepository} in the directory of the
     * program as filename{@value #REPOSITORY_SUFFIX}.
     *
     * @param repository The repository which will be saved
     * @param filename The name of the file in which the repository will be
     *            saved
     * @throws IOException If there was an error reading the file.
     */
    public static void saveRepository(final XReadableRepository repository, final String filename)
            throws IOException {
        saveRepository(repository, null, filename);
    }

    /**
     * Saves the given repository in path/filename{@value #REPOSITORY_SUFFIX}.
     *
     * @param repository The repository which will be saved
     * @param path The directory in which the file will be saved
     * @param name The name of the file in which the repository will be saved
     * @throws IOException If there was an error reading the file.
     */
    public static void saveRepository(final XReadableRepository repository, final String path, final String name)
            throws IOException {
        saveRepository(repository, getFileForRepository(path, name));
    }

}
