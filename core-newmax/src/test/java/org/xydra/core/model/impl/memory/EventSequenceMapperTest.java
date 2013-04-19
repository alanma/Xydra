package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.id.MemoryStringIDProvider;
import org.xydra.core.XX;
import org.xydra.core.model.impl.memory.EventSequenceMapper.Result;
import org.xydra.index.query.Pair;


public class EventSequenceMapperTest {
	XId repo = XX.toId("repo");
	XId model = XX.toId("model");
	XId object = XX.toId("object");
	private static XId actor = XX.toId("actor");
	
	private static XEvent createFieldAddedEvent(XAddress objectAddress, String fieldId) {
		return MemoryObjectEvent.createAddEvent(actor, objectAddress, XX.toId(fieldId), 1, false);
	}
	
	@Test
	public void testAllServerMappedAllLocalMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, "A");
		XEvent addFieldB = createFieldAddedEvent(objectAddress, "B");
		XEvent addFieldC = createFieldAddedEvent(objectAddress, "C");
		XEvent addFieldD = createFieldAddedEvent(objectAddress, "D");
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC, addFieldD };
		
		LocalChange localChangeA = new LocalChange(null, addFieldA);
		LocalChange localChangeB = new LocalChange(null, addFieldB);
		LocalChange localChangeC = new LocalChange(null, addFieldC);
		LocalChange localChangeD = new LocalChange(null, addFieldD);
		
		List<LocalChange> localChangesList = new ArrayList<LocalChange>();
		localChangesList.add(localChangeA);
		localChangesList.add(localChangeB);
		localChangesList.add(localChangeC);
		localChangesList.add(localChangeD);
		
		Result result = EventSequenceMapper.map(serverEvents, new LocalChanges(localChangesList));
		
		assert result.nonMappedLocalEvents.isEmpty();
		assert result.nonMappedServerEvents.isEmpty();
		assert result.mapped.size() == serverEvents.length
		        && result.mapped.size() == localChangesList.size();
		
	}
	
	@Test
	public void testAllServerMappedSomeLocalNotMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, "A");
		XEvent addFieldB = createFieldAddedEvent(objectAddress, "B");
		XEvent addFieldC = createFieldAddedEvent(objectAddress, "C");
		XEvent addFieldD = createFieldAddedEvent(objectAddress, "D");
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC, addFieldD };
		
		XEvent addFieldE = createFieldAddedEvent(objectAddress, "E");
		XEvent addFieldF = createFieldAddedEvent(objectAddress, "F");
		
		LocalChange localChangeA = new LocalChange(null, addFieldA);
		LocalChange localChangeB = new LocalChange(null, addFieldB);
		LocalChange localChangeC = new LocalChange(null, addFieldC);
		LocalChange localChangeD = new LocalChange(null, addFieldD);
		LocalChange localChangeE = new LocalChange(null, addFieldE);
		LocalChange localChangeF = new LocalChange(null, addFieldF);
		
		List<LocalChange> localChangesList = new ArrayList<LocalChange>();
		localChangesList.add(localChangeA);
		localChangesList.add(localChangeB);
		localChangesList.add(localChangeC);
		localChangesList.add(localChangeD);
		localChangesList.add(localChangeE);
		localChangesList.add(localChangeF);
		
		Result result = EventSequenceMapper.map(serverEvents, new LocalChanges(localChangesList));
		
		assert result.nonMappedLocalEvents.size() == (localChangesList.size() - result.mapped
		        .size());
		assert result.nonMappedServerEvents.isEmpty();
		assert result.mapped.size() == serverEvents.length;
	}
	
	@Test
	public void testSomeServerNotMappedSomeLocalNotMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, "A");
		XEvent addFieldB = createFieldAddedEvent(objectAddress, "B");
		XEvent addFieldC = createFieldAddedEvent(objectAddress, "C");
		XEvent addFieldD = createFieldAddedEvent(objectAddress, "D");
		XEvent addFieldX = createFieldAddedEvent(objectAddress, "X");
		XEvent addFieldY = createFieldAddedEvent(objectAddress, "Y");
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC, addFieldD, addFieldX, addFieldY };
		
		XEvent addFieldE = createFieldAddedEvent(objectAddress, "E");
		XEvent addFieldF = createFieldAddedEvent(objectAddress, "F");
		
		LocalChange localChangeA = new LocalChange(null, addFieldA);
		LocalChange localChangeB = new LocalChange(null, addFieldB);
		LocalChange localChangeC = new LocalChange(null, addFieldC);
		LocalChange localChangeD = new LocalChange(null, addFieldD);
		LocalChange localChangeE = new LocalChange(null, addFieldE);
		LocalChange localChangeF = new LocalChange(null, addFieldF);
		
		List<LocalChange> localChangesList = new ArrayList<LocalChange>();
		localChangesList.add(localChangeA);
		localChangesList.add(localChangeB);
		localChangesList.add(localChangeC);
		localChangesList.add(localChangeD);
		localChangesList.add(localChangeE);
		localChangesList.add(localChangeF);
		
		Result result = EventSequenceMapper.map(serverEvents, new LocalChanges(localChangesList));
		
		assert result.nonMappedServerEvents.contains(addFieldX);
		assert result.nonMappedServerEvents.contains(addFieldY);
		assert result.nonMappedLocalEvents.contains(localChangeE);
		assert result.nonMappedLocalEvents.contains(localChangeF);
		
		Pair<XEvent,LocalChange> mappedA = new Pair<XEvent,LocalChange>(addFieldA, localChangeA);
		Pair<XEvent,LocalChange> mappedB = new Pair<XEvent,LocalChange>(addFieldB, localChangeB);
		Pair<XEvent,LocalChange> mappedC = new Pair<XEvent,LocalChange>(addFieldC, localChangeC);
		Pair<XEvent,LocalChange> mappedD = new Pair<XEvent,LocalChange>(addFieldD, localChangeD);
		
		assert result.mapped.indexOf(mappedA) == 0;
		assert result.mapped.indexOf(mappedB) == 1;
		assert result.mapped.indexOf(mappedC) == 2;
		assert result.mapped.indexOf(mappedD) == 3;
		
		assert result.nonMappedLocalEvents.size() == (localChangesList.size() - result.mapped
		        .size());
		assert result.nonMappedServerEvents.size() == (serverEvents.length - result.mapped.size());
	}
	
	@Test
	public void testAllServerNotMappedAllLocalNotMapped() {
		MemoryStringIDProvider idProvider = new MemoryStringIDProvider();
		XAddress objectAddress = idProvider
		        .fromComponents(this.repo, this.model, this.object, null);
		
		XEvent addFieldA = createFieldAddedEvent(objectAddress, "A");
		XEvent addFieldB = createFieldAddedEvent(objectAddress, "B");
		XEvent addFieldC = createFieldAddedEvent(objectAddress, "C");
		XEvent addFieldD = createFieldAddedEvent(objectAddress, "D");
		
		XEvent[] serverEvents = { addFieldA, addFieldB, addFieldC, addFieldD };
		
		XEvent addFieldE = createFieldAddedEvent(objectAddress, "E");
		XEvent addFieldF = createFieldAddedEvent(objectAddress, "F");
		XEvent addFieldX = createFieldAddedEvent(objectAddress, "X");
		XEvent addFieldY = createFieldAddedEvent(objectAddress, "Y");
		
		LocalChange localChangeE = new LocalChange(null, addFieldE);
		LocalChange localChangeF = new LocalChange(null, addFieldF);
		LocalChange localChangeX = new LocalChange(null, addFieldX);
		LocalChange localChangeY = new LocalChange(null, addFieldY);
		
		List<LocalChange> localChangesList = new ArrayList<LocalChange>();
		localChangesList.add(localChangeE);
		localChangesList.add(localChangeF);
		localChangesList.add(localChangeX);
		localChangesList.add(localChangeY);
		
		Result result = EventSequenceMapper.map(serverEvents, new LocalChanges(localChangesList));
		
		assert result.nonMappedLocalEvents.size() == localChangesList.size();
		assert result.nonMappedServerEvents.size() == serverEvents.length;
		assert result.mapped.isEmpty();
	}
}
