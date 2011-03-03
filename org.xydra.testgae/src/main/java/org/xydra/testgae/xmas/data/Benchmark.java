package org.xydra.testgae.xmas.data;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.testgae.Stopwatch;


public class Benchmark {
	
	public static void restless(Restless r, String path) {
		r.addGet(path + "/benchmark", Benchmark.class, "run",

		new RestlessParameter("repo", null),

		new RestlessParameter("list", null),

		new RestlessParameter("createInitially", "1"),

		new RestlessParameter("delete", "1"),

		new RestlessParameter("createAgain", "1")

		);
	}
	
	public static void run(String repo, String list, String createInitially, String delete,
	        String createAgain, HttpServletResponse res) throws NumberFormatException, IOException {
		ServletUtils.headers(res, "text/html");
		runBenchmark(repo, list, Integer.parseInt(createInitially), Integer.parseInt(delete),
		        Integer.parseInt(createAgain), res.getWriter());
	}
	
	public static void runBenchmark(String repo, String list, int createInitially, int delete,
	        int createAgain, Writer w) throws IOException {
		Stopwatch s1 = new Stopwatch().start();
		
		// create list
		XWritableModel model = Xmas.getRepository(repo).createModel(XX.toId(list));
		WishList wishList = new WishList(model);
		s1.stop();
		w.write(s1.getFormattedResult("init repo, create model", 1) + "");
		
		// create 3 wishes
		List<XID> wishes = wishList.addDemoData(createInitially, w);
		
		// get wishes + properties
		wishList.toHtml(w);
		
		// delete 2 wishes
		s1.start();
		for(int i = 0; i < delete; i++) {
			int d = (int)(Math.random() * (wishes.size() - 1));
			wishList.removeWish(wishes.get(d));
		}
		s1.stop();
		w.write(s1.getFormattedResult("delete wish", delete) + "");
		
		// create 2 wishes
		wishList.addDemoData(createAgain, w);
		
		// get wishes + properties
		wishList.toHtml(w);
	}
}
