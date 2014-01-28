package org.xydra.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;


/**
 * Some generic tools for working with java.util.Map
 * 
 * @author xamde
 */
public class TransformerTool {
    
    /**
     * This is not fast.
     * 
     * @param mapKW
     * @param transformer
     * @return a new {@link HashMap} with all values transformed
     */
    public static <K, V, W> Map<K,V> transformMapValues(Map<K,W> mapKW,
            ITransformer<W,V> transformer) {
        if(mapKW == null)
            return null;
        
        Map<K,V> mapKV = new HashMap<K,V>(mapKW.size());
        for(Entry<K,W> e : mapKW.entrySet()) {
            K key = e.getKey();
            V value = transformer.transform(e.getValue());
            
            mapKV.put(key, value);
        }
        
        return mapKV;
    }
    
    /**
     * This is not fast.
     * 
     * @param mapIn
     * @param keyTransformer
     * @param valueTransformer
     * @return a new {@link HashMap} with all values transformed
     */
    public static <KI, KO, VI, VO> Map<KO,VO> transformMapKeyAndValues(Map<KI,VI> mapIn,
            ITransformer<KI,KO> keyTransformer, ITransformer<VI,VO> valueTransformer) {
        if(mapIn == null)
            return null;
        
        Map<KO,VO> mapKV = new HashMap<KO,VO>(mapIn.size());
        for(Entry<KI,VI> e : mapIn.entrySet()) {
            KI keyIn = e.getKey();
            KO keyOut = keyTransformer.transform(keyIn);
            VI valueIn = e.getValue();
            VO valueOut = valueTransformer.transform(valueIn);
            
            mapKV.put(keyOut, valueOut);
        }
        
        return mapKV;
    }
    
    /**
     * This is not fast.
     * 
     * @param listIn
     * @param transformer
     * @return a new {@link ArrayList}
     */
    public static <I, O> List<O> transformListEntries(List<I> listIn, ITransformer<I,O> transformer) {
        if(listIn == null)
            return null;
        
        List<O> listOut = new ArrayList<O>(listIn.size());
        for(I in : listIn) {
            O out = transformer.transform(in);
            listOut.add(out);
        }
        
        return listOut;
    }
    
    public static <I, O> Iterator<O> transformIterator(Iterator<I> iteratorIn,
            ITransformer<I,O> transformer) {
        return new TransformingIterator<I,O>(iteratorIn, transformer);
    }
    
    public static <I, O> Iterable<O> transformIterable(Iterable<I> iterableIn,
            ITransformer<I,O> transformer) {
        return new TransformingIterable<I,O>(iterableIn, transformer);
    }
    
    public static class TransformingIterable<I, O> implements Iterable<O> {
        
        private ITransformer<I,O> transformer;
        private Iterable<I> iterable;
        
        public TransformingIterable(Iterable<I> iterableIn, ITransformer<I,O> transformer) {
            this.iterable = iterableIn;
            this.transformer = transformer;
        }
        
        @Override
        public Iterator<O> iterator() {
            return new TransformingIterator<I,O>(this.iterable.iterator(), this.transformer);
        }
    }
    
}
