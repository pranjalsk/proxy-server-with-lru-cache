package edu.asu.proxyserver;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.*;

public class CacheLRU {

	private static int size_utilized = 0;
	private static long cache_size;
	public static Map<String, String> map = new HashMap<String, String>();
	
	public CacheLRU(long cacheSizeKB){
		 this.cache_size = cacheSizeKB;
	}

	private static PriorityQueue<Node> timestamp_queue = new PriorityQueue<Node>(200, new Comparator() {
		public int compare(Object a, Object b) {
			if (!(a instanceof Node) || !(b instanceof Node))
				return 0;
			Node n1 = (Node) a;
			Node n2 = (Node) b;
			return n1.getTimestamp().compareTo(n2.getTimestamp());
		}
	});

	private String remove() {
		Node leastUsed = timestamp_queue.poll();
		if (leastUsed != null) {
			System.out.println("--------Cache Full, Removed from cache------------");
			return leastUsed.getValue();
		}
		return "";
	}

	private void update(String mostRecentEleKey) {
		Iterator<Node> pqIterator = timestamp_queue.iterator();
		while (pqIterator.hasNext()) {
			Node e = pqIterator.next();
			if (e.getValue().equals(mostRecentEleKey)) {
				pqIterator.remove();
				break;
			}
		}
		Node mostRecent = new Node();
		mostRecent.setTimestamp(new Date());
		mostRecent.setValue(mostRecentEleKey);
		timestamp_queue.offer(mostRecent);
	}

	public String get(String key) {
		String value = map.get(key);
		return value;
	}

	public boolean containsKey(String key){
		boolean flag = false;
		for (Map.Entry<String, String> entry : map.entrySet()) {
		    if (entry.getKey().equals(key)) {
				flag = true;
				update(key);
			}
		}
		return flag;
	}
	
	public String put(String key, String value) {
		if (map.containsKey(key)) {
			update(key);
			return key;
		} else {
			while ((size_utilized + value.length()) >= cache_size && timestamp_queue.size() > 0) {
				String leastUsedKey = remove();
//				System.out.println("^^^^^^Key to be removeed--->>"+ leastUsedKey);
				String leastRecentValue = map.get(leastUsedKey);
				map.remove(leastUsedKey);
				size_utilized -= leastRecentValue.length();
			}
				Node e = new Node();
				e.setValue(key);
				e.setTimestamp(new Date());
				timestamp_queue.offer(e);
				map.put(key, value);
				System.out.println("Inserted into Cache...");
				size_utilized += value.length();	
			
			System.out.println("Cache Metrics----------\nSize Utilized: "+size_utilized + " ; Size remaining: "+(cache_size-size_utilized));
			return key;
		}
	}
	
}

class Node {
	private String data_content;
	private Date timestamp;

	public String getValue() {
		return data_content;
	}

	public void setValue(String value) {
		this.data_content = value;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public boolean equals(Node e) {
		return data_content.equals(e.getValue());
	}

}
