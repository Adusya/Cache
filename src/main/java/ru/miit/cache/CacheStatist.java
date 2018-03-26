package ru.miit.cache;

public class CacheStatist {
	
	public long cacheHits;
	public long cacheMisses;

	public CacheStatist(long cacheHits, long cacheMisses) {

		this.cacheHits = cacheHits;
		this.cacheMisses = cacheMisses;
		
	}

	public long getCacheHits() {
		return cacheHits;
	}

	public long getCacheMisses() {
		return cacheMisses;
	}

	public float getCacheHitRatio() {

		float ratio;

		if (cacheHits == 0 || cacheMisses == 0) {

			ratio = 0;

		} else {

			float cacheHitsFloat = cacheHits;
			float cacheMissesFloat = cacheMisses;
			ratio = cacheHitsFloat / (cacheHitsFloat + cacheMissesFloat);

		}

		return ratio;
	}

}
