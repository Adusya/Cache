package ru.unisuite.cache.cacheexception;

public class CachePropertiesException extends CacheStartFailedException {

	private static final long serialVersionUID = 1L;

	private int errorCode;
	
    public CachePropertiesException(final String message)
    {
        this(0, "See configuration file: " + message);
    }
 
    public CachePropertiesException(final int errorCode, final String message)
    {
        super(message);

        this.errorCode = errorCode;
    }
    
    public CachePropertiesException(String message, Throwable cause) {
  		super(message, cause);
  	}
 
    public int getErrorCode()
    {
        return errorCode;
    }
	
}
