package com.venky.swf.plugins.audit.extensions;

import com.venky.extension.Extension;
import com.venky.extension.Registry;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;

import com.venky.swf.path._IPath;
import com.venky.swf.plugins.audit.db.model.AUDITED;
import com.venky.swf.plugins.audit.db.model.ModelAudit;
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
        if (!m.isDirty()){
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
                modelAudit.setComment("Created");
                break;
            case UPDATE:
                JSONObject object = new JSONObject();
                for (String f: m.getRawRecord().getDirtyFields()){
                    JSONObject audit = new JSONObject();
                    object.put(f, audit);
                    audit.put("old",m.getRawRecord().getOldValue(f));
                    audit.put("new",m.getReflector().get(m,f));
                }
                modelAudit.setComment(object.toString());
                break;
        }
        modelAudit.save();
    }
}
