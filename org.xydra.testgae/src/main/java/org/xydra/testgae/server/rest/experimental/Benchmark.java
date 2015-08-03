package org.xydra.testgae.server.rest.experimental;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.common.NanoClock;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.testgae.server.model.xmas.WishList;
import org.xydra.testgae.server.model.xmas.Xmas;

public class Benchmark {

	public static void restless(final Restless r, final String path) {
		r.addGet(path + "/benchmark", Benchmark.class, "run",

		new RestlessParameter("repo", null),

		new RestlessParameter("list", null),

		new RestlessParameter("createInitially", "1"),

		new RestlessParameter("delete", "1"),

		new RestlessParameter("createAgain", "1")

		);
	}

	public static void run(final String repo, final String list, final String createInitially, final String delete,
			final String createAgain, final HttpServletResponse res) throws NumberFormatException, IOException {
		ServletUtils.headers(res, "text/html");
		runBenchmark(repo, list, Integer.parseInt(createInitially), Integer.parseInt(delete),
				Integer.parseInt(createAgain), new OutputStreamWriter(res.getOutputStream(),
						"utf-8"));
	}

	public static void runBenchmark(final String repo, final String list, final int createInitially, final int delete,
			final int createAgain, final Writer w) throws IOException {
		final NanoClock s1 = new NanoClock().start();

		// create list
		final XWritableModel model = Xmas.getRepository(repo).createModel(Base.toId(list));
		final WishList wishList = new WishList(model);
		s1.stop("init repo, create model");
		w.write(s1.getStats());

		// create 3 wishes
		final List<XId> wishes = wishList.addDemoData(createInitially, w);

		// get wishes + properties
		wishList.toHtml();

		// delete 2 wishes
		s1.start();
		for (int i = 0; i < delete; i++) {
			final int d = (int) (Math.random() * (wishes.size() - 1));
			wishList.removeWish(wishes.get(d));
		}
		s1.stop("delete wish");
		w.write(s1.getStats());

		// create 2 wishes
		wishList.addDemoData(createAgain, w);

		// get wishes + properties
		wishList.toHtml();
	}
}
