package io.edurt.datacap.fs;

import io.edurt.datacap.plugin.Service;

public interface FsService
        extends Service
{
    FsResponse writer(FsRequest request);

    FsResponse reader(FsRequest request);

    /**
     * Delete file or directory
     *
     * @param request Request info
     * @return delete info
     */
    default FsResponse delete(FsRequest request)
    {
        throw new UnsupportedOperationException(request.getFileName() + " does not support delete");
    }
}
