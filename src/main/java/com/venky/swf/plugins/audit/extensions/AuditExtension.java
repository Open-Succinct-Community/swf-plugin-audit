package com.venky.swf.plugins.audit.extensions;

import com.venky.core.io.StringReader;
import com.venky.extension.Extension;
import com.venky.extension.Registry;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;

import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.path._IPath;
import com.venky.swf.plugins.audit.db.model.AUDITED;
import com.venky.swf.plugins.audit.db.model.ModelAudit;
import in.succinct.json.JSONAwareWrapper;
import in.succinct.json.JSONComm;
import org.json.simple.JSONObject;

public class AuditExtension implements Extension {
    static {
        Registry.instance().registerExtension("Model.before.update", new AuditExtension(Operation.UPDATE));
        Registry.instance().registerExtension("Model.after.create", new AuditExtension(Operation.CREATE));
    }
    public enum Operation {
        CREATE,
        UPDATE,
    }
    Operation operation;
    public AuditExtension(Operation operation){
        this.operation = operation;
    }


    @Override
    public void invoke(Object... context) {
        Model m = (Model)context[0];
        AUDITED audited = m.getReflector().getAnnotation(AUDITED.class);
        if (audited == null){
            return;
        }
        if (!audited.value()){
            return;
        }
        if (!m.isDirty() && operation != Operation.CREATE){
            return;
        }

        _IPath path = Database.getInstance().getContext(_IPath.class.getName());
        String remoteHost = path == null ? "" : path.getRequest().getHeader("X-Real-IP") ;
        ModelAudit modelAudit = Database.getTable(ModelAudit.class).newRecord();
        modelAudit.setModelId(m.getId());
        modelAudit.setIpAddress(remoteHost);
        modelAudit.setName(m.getReflector().getModelClass().getSimpleName());
        switch (operation){
            case CREATE:
                modelAudit.setComment(new StringReader("Created"));
                break;
            case UPDATE:
                JSONObject object = new JSONObject();
                for (String f: m.getRawRecord().getDirtyFields()){
                    JSONObject audit = new JSONObject();
                    object.put(f, audit);
                    
                    Object oldValue = m.getRawRecord().getOldValue(f);
                    if (oldValue != null){
                        oldValue = m.getReflector().getJdbcTypeHelper().getTypeRef(oldValue.getClass()).getTypeConverter().toString(oldValue);
                    }
                    Object newValue = m.getRawRecord().get(f);
                    if (newValue != null){
                        newValue = m.getReflector().getJdbcTypeHelper().getTypeRef(newValue.getClass()).getTypeConverter().toString(newValue);
                    }
                    if (oldValue == null || newValue == null ){
                        audit.put("old", oldValue);
                        audit.put("new", newValue);
                    }else {
                        try {
                            //Make trimmed difference for json objects.
                            JSONObject o = JSONAwareWrapper.parse((String)oldValue);
                            JSONObject n = JSONAwareWrapper.parse((String)newValue);
                            audit.put("old", JSONComm.getInstance().subtract(o, n));
                            audit.put("new", JSONComm.getInstance().subtract(n, o));
                        }catch (Exception ex) {
                            audit.put("old", oldValue);
                            audit.put("new", newValue);
                        }
                    }
                    
                    
                    
                }
                modelAudit.setComment(new StringReader(object.toString()));
                break;
        }
        modelAudit.save();
    }
}
