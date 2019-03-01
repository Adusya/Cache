package ru.unisuite.cache.cacheexception;

public class CacheMetadataStoreConnectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public CacheMetadataStoreConnectionException(final String message)
    {
        this(0, "PersistentCache cannot be created. " + message);
    }
 
    public CacheMetadataStoreConnectionException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
    
    public CacheMetadataStoreConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
 
    public int getErrorCode()
    {
        return errorCode;
    }
	
}
