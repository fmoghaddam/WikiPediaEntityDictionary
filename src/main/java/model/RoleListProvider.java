package model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import model.Category;
import model.DataSourceType;
import model.Order;
import util.MapUtil;

public abstract class RoleListProvider {
	private static Logger LOG = Logger.getLogger(RoleListProvider.class);
	protected Map<String, Set<Category>> roleMap = new LinkedHashMap<>();

	/**
	 * This function should first load the roles into the map, then sort the map
	 * in a descending mode regards to the length of the text and then fill the
	 * colorutil
	 */
	public void loadRoles(DataSourceType dataSourceType) {
	};

	public void print() {
		for (String s : roleMap.keySet()) {
			LOG.info(s);
		}
	}

	public Map<String, Set<Category>> getData() {
		return roleMap;
	}

	protected void sortBasedOnLenghth(Order order) {
		switch (order) {
		case ASC:
			roleMap = MapUtil.sortByKeyAscending(roleMap);
			break;
		case DESC:
			roleMap = MapUtil.sortByKeyDescending(roleMap);
			break;
		default:
			break;
		}
	}
}
