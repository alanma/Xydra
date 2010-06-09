package org.xydra.textsearch.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SubstringIndex<V> extends AbstractTokenIndex<V> {

	private int minSubstringLength;
	private int maxSubStringLength;

	public SubstringIndex(int minSubstringLength, int maxSubStringLength) {
		super();
		this.minSubstringLength = minSubstringLength;
		this.maxSubStringLength = maxSubStringLength;
	}

	@Override
	protected Collection<String> generateTokenFragments(String token) {
		return generateSubstrings(token, this.minSubstringLength,
				this.maxSubStringLength);
	}
	
	private Set<String> generateSubstrings(String t,
			int minimalSubStringLength, int maximalSubstringLength) {
		Set<String> result = new HashSet<String>();
		if (t.length() < minimalSubStringLength) {
			return result;
		} else {
			int maxSubStringLen = Math.min(t.length(), maximalSubstringLength);

			for (int subStringLen = minimalSubStringLength; subStringLen <= maxSubStringLen; subStringLen++) {
				// how often can we walk t for a given subStringLen?
				int slicesMax = t.length() - subStringLen + 1;
				for (int i = 0; i < slicesMax; i++) {
					String subString = t.substring(i, i + subStringLen);
					result.add(subString);
				}
			}
			return result;
		}
	}

}
