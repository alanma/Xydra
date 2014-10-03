package org.xydra.annotations;

@Since("2012-10")
@RunsInGWT(true)
@RunsInAppEngine(false)
public class Demo {

	@Indexed({ "server", "client" })
	@NeverNull
	String name;

	@CanBeNull
	@Feature("security")
	String password;

	@ModificationOperation
	void setName(String name) {
		this.name = name;
	}

}
