package com.joytank.net;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public final class ClinetInfoMap {

	private final ConcurrentMap<Integer, ClientInfo> map = Maps.newConcurrentMap();
	
	public void put(Integer clientId, ClientInfo value) {
		map.putIfAbsent(clientId, value);
	}
	
	public ClientInfo get(Integer clientId) {
		return map.get(clientId);
	}
}
