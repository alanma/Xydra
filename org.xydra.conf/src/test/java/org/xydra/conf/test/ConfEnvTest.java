package org.xydra.conf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.xydra.conf.ConfigException;
import org.xydra.conf.IConfig;
import org.xydra.conf.IResolver;
import org.xydra.env.Env;

public class ConfEnvTest {

	@Test
	public void testBasics() {
		Env.reset();
		assertNotNull(Env.get());
		assertFalse(Env.get().conf().getDefinedKeys().iterator().hasNext());
	}

	@Test
	public void testResolver() {
		final IConfig conf = Env.get().conf();

		try {
			conf.getResolver("foo");
			fail();
		} catch (final ConfigException e) {
		}

		final IResolver<Integer> resolver1 = new IResolver<Integer>() {

			@Override
			public Integer resolve() {
				return new Integer(42);
			}

			@Override
			public boolean canResolve() {
				return true;
			}
		};

		conf.setResolver("foo", resolver1);

		final IResolver<Integer> res2 = conf.getResolver("foo");
		assertNotNull(res2);
		assertEquals(resolver1, res2);
		assertEquals(42, (int) res2.resolve());
	}
}
