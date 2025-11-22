package io.edurt.datacap.condor.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

@Slf4j
public class AppendableObjectInputStream
        extends ObjectInputStream
{
    private boolean firstObject = true;

    public AppendableObjectInputStream(InputStream in)
            throws IOException
    {
        super(in);
    }

    @Override
    protected void readStreamHeader()
            throws IOException
    {
        if (firstObject) {
            super.readStreamHeader();
            firstObject = false;
        }
    }
}
