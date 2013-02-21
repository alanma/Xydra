package org.xydra.oo.faked.client;

import java.util.Collections;
import java.util.List;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValueJavaUtils;
import org.xydra.oo.Field;
import org.xydra.oo.runtime.client.GwtXydraMapped;
import org.xydra.oo.runtime.shared.CollectionProxy;
import org.xydra.oo.runtime.shared.CollectionProxy.ITransformer;
import org.xydra.oo.runtime.shared.ListProxy;
import org.xydra.oo.testgen.tasks.shared.ITask;


public class GwtTask extends GwtXydraMapped implements ITask {
    
    public GwtTask(XWritableModel model, XID id) {
        super(model, id);
    }
    
    @Override
    @Field("rECENTLY_COMPLETED")
    public long getRECENTLY_COMPLETED() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    @Field("rECENTLY_COMPLETED")
    public void setRECENTLY_COMPLETED(long rECENTLY_COMPLETED) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    @Field("checked")
    public boolean getChecked() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    @Field("checked")
    public void setChecked(boolean checked) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean isRecentlyCompleted() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    @Field("note")
    public String getNote() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    @Field("note")
    public void setNote(String note) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    @Field("starred")
    public boolean getStarred() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    @Field("starred")
    public void setStarred(boolean starred) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    @Field("subTasks")
    public List<ITask> subTasks() {
        ITransformer<XIDListValue,XID,List<ITask>,ITask> t = new CollectionProxy.ITransformer<XIDListValue,XID,List<ITask>,ITask>() {
            
            @Override
            public ITask toJavaComponent(XID xid) {
                return new GwtTask(GwtTask.this.oop.getXModel(), xid);
            }
            
            @Override
            public XID toXydraComponent(ITask javaType) {
                return javaType.getId();
            }
            
            @Override
            public XIDListValue createCollection() {
                return XV.toIDListValue(Collections.EMPTY_LIST);
            }
            
        };
        
        return new ListProxy<XIDListValue,XID,List<ITask>,ITask>(
        
        this.oop.getXObject(), XX.toId("subTasks"), t);
    }
    
    @Override
    public String getTitle() {
        return XValueJavaUtils.getString(this.oop.getXObject(), XX.toId("title"));
    }
    
    @Override
    public void setTitle(String title) {
        XValueJavaUtils.setString(this.oop.getXObject(), XX.toId("title"), title);
    }
    
    @Override
    @Field("completionDate")
    public long getCompletionDate() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    @Field("completionDate")
    public void setCompletionDate(long completionDate) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    @Field("dueDate")
    public long getDueDate() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    @Field("dueDate")
    public void setDueDate(long dueDate) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    @Field("remindDate")
    public long getRemindDate() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    @Field("remindDate")
    public void setRemindDate(long remindDate) {
        // TODO Auto-generated method stub
        
    }
    
}
