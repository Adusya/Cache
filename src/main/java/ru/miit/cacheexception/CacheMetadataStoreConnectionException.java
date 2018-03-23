package ru.miit.cacheexception;

public class CacheMetadataStoreConnectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public CacheMetadataStoreConnectionException(final String message)
    {
        this(0, "Cache cannot be created. " + message);
    }
 
    public CacheMetadataStoreConnectionException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
 
    public int getErrorCode()
    {
        return errorCode;
    }
	
}
