package org.xydra.testgae.xmas.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.testgae.Stopwatch;
import org.xydra.testgae.xmas.HtmlUtils;
import org.xydra.testgae.xmas.rest.WishlistResource;
import org.xydra.testgae.xmas.rest.XmasResource;


/**
 * The xmas application. Exposed to REST via {@link XmasResource}.
 * 
 * @author xamde
 * 
 */
public class Xmas {
	
	static {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}
	
	private static final Logger log = LoggerFactory.getLogger(Xmas.class);
	
	private static WritableRepositoryOnPersistence repo;
	
	public static XWritableRepository getRepository(String repoIdStr) {
		/* if there is no repo or the wrong one: init */
		if(repo == null || (repoIdStr != null && !repo.getID().toString().equals(repoIdStr))) {
			XID repoId = repoIdStr == null ? XX.toId("benchmark-" + System.currentTimeMillis())
			        : XX.toId(repoIdStr);
			log.info("Initialising repository '" + repoId + "' at " + System.currentTimeMillis());
			XydraPersistence persistence = new GaePersistence(repoId);
			repo = new WritableRepositoryOnPersistence(persistence, XX.toId("benchmark-user"));
			log.info("Init done at " + System.currentTimeMillis());
		}
		return repo;
	}
	
	public static void addData(String repoStr, int listCount, int wishesCount, Writer writer)
	        throws IOException {
		writer.write("Adding to repository '" + getRepository(repoStr).getID() + "'<br />\n");
		Stopwatch s2 = new Stopwatch();
		s2.start();
		for(int l = 1; l <= listCount; l++) {
			writer.write("Creating/loading model <br />\n");
			XWritableModel model = getRepository(repoStr).createModel(XX.toId("list" + l));
			WishList wishList = WishlistResource.load(getRepository(repoStr), model.getID());
			wishList.addDemoData(wishesCount, writer);
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "/" + model.getID(), "See list '"
			        + model.getID() + "'")
			        + "<br />\n");
		}
		s2.stop();
		writer.write(s2
		        .getFormattedResult("create list with " + wishesCount + " wishes", listCount)
		        + "<br />\n");
	}
	
	public static void get(String repoStr, String view, Writer writer) throws IOException {
		Stopwatch s1 = new Stopwatch();
		s1.start();
		writer.write("<h2>All wishlists in repository '" + Xmas.getRepository(repoStr).getID()
		        + "'</h2>\n");
		if(view.equals("expanded")) {
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "?view=collapsed", "collapse all")
			        + "<br />\n");
		} else {
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "?view=expanded", "expand all")
			        + "<br />\n");
		}
		int count = 0;
		for(XID modelId : getRepository(repoStr)) {
			count++;
			XWritableModel model = Xmas.getRepository(repoStr).getModel(modelId);
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "/" + modelId + "", "Wish list '"
			        + modelId + "'<br />\n"));
			if(view.equals("expanded")) {
				WishList list = new WishList(model);
				writer.write(list.toHtml());
			}
		}
		s1.stop();
		writer.write(s1.getFormattedResult("read list", count) + "<br />\n");
	}
	
	public static void main(String[] args) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		Xmas.addData("test", 10, 10, writer);
		Xmas.get("test", "collapsed", writer);
		System.out.println("Done. You must terminate the VM now.");
	}
	
}
