package ru.unisuite.cache.cacheexception;

public class CacheStartFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public CacheStartFailedException(final String message)
    {
        this(0, "PersistentCache cannot be created. " + message);
    }
 
    public CacheStartFailedException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
    
    public CacheStartFailedException(String message, Throwable cause) {
  		super(message, cause);
  	}
 
    public int getErrorCode()
    {
        return errorCode;
    }
	
	
}
