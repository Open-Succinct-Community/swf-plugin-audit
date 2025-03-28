package com.venky.swf.plugins.audit.db.model;

import com.venky.swf.db.annotations.column.COLUMN_SIZE;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.model.Model;

import java.io.Reader;


public interface ModelAudit extends Model {
    @Index
    public String getName();
    public void setName(String name);

    @Index
    public long getModelId();
    public void setModelId(long id);

    public String getIpAddress();
    public void setIpAddress(String address);

    public Reader getComment();
    public void setComment(Reader comment);
}
