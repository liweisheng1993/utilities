package com.liws.eventchannel;

import java.io.OutputStream;

/**
 * Created by liweisheng on 16/9/7.
 */
public interface Record {
    public void serialize(OutputStream os);
    public Object deserialize(OutputStream os);
}
