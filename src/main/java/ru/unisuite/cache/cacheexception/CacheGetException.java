package ru.unisuite.cache.cacheexception;

public class CacheGetException extends Exception{
	
	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public CacheGetException(final String message)
    {
        this(0, "PersistentCache can not get this object: " + message);
    }
 
    public CacheGetException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
    
	public CacheGetException(String message, Throwable cause) {
		super(message, cause);
	}
 
    public int getErrorCode()
    {
        return errorCode;
    }

}
