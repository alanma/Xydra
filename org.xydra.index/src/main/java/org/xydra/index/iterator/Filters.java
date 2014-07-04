package org.xydra.index.iterator;

public class Filters {
    
    /**
     * Combine filters
     * 
     * @param filters null values represent match-all filters
     * @return a filter representing the logical AND
     */
    @SafeVarargs
    public static <E> IFilter<E> and(final IFilter<E> ... filters) {
        assert filters != null && filters.length >= 1;
        
        return new IFilter<E>() {
            
            @Override
            public boolean matches(E entry) {
                for(IFilter<E> filter : filters) {
                    if(filter != null) {
                        if(!filter.matches(entry))
                            return false;
                    }
                }
                
                return true;
            }
        };
    }
    
}
