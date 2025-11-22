package io.edurt.datacap.condor.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class AppendableObjectOutputStream
        extends ObjectOutputStream
{
    private boolean firstObject = true;

    public AppendableObjectOutputStream(OutputStream out)
            throws IOException
    {
        super(out);
    }

    @Override
    protected void writeStreamHeader()
            throws IOException
    {
        if (firstObject) {
            super.writeStreamHeader();
            firstObject = false;
        }
    }
}
