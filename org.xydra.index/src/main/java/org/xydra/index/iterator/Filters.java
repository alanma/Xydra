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
        
        if(filters == null || filters.length == 0) {
            return matchAll();
        }
        
        if(filters.length == 1) {
            return filters[0];
        }
        
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
    
    @SuppressWarnings("unchecked")
    public static <E> IFilter<E> matchAll() {
        return (IFilter<E>)MATCH_ALL;
    }
    
    private static IFilter<Object> MATCH_ALL = new IFilter<Object>() {
        
        @Override
        public boolean matches(Object entry) {
            return true;
        }
        
    };
    
    @SuppressWarnings("unchecked")
    public static <E> IFilter<E> matchNone() {
        return (IFilter<E>)MATCH_NONE;
    }
    
    private static IFilter<Object> MATCH_NONE = new IFilter<Object>() {
        
        @Override
        public boolean matches(Object entry) {
            return false;
        }
        
    };
}
