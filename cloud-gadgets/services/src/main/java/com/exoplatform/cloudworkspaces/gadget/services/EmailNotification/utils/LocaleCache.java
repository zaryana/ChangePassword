package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class LocaleCache<T> {
	protected Map<String, T> cache = new HashMap<String, T>();

	protected abstract T getFromSource(String locale) throws Exception;
	
	public T get(String locale) throws Exception {
		if(cache.containsKey(locale)) {
			return cache.get(locale);
		}
		return getFromSource(locale);
	}
	
	public T getDefault() throws Exception {
		return get("default");
	}
	
	public void clear(){
		cache.clear();
	}
}
