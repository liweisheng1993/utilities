package com.liws.utilities.eventchannel;

import java.io.OutputStream;

/**
 * Created by liweisheng on 16/8/28.
 */
public interface Record {
    public void serialize(OutputStream os);
    public Object deserialize(OutputStream os);
}
