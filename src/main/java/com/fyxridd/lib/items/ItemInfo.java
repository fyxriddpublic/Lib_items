package com.fyxridd.lib.items;

import com.fyxridd.lib.core.api.hashList.ChanceHashList;
import com.fyxridd.lib.core.api.hashList.ChanceHashListImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemInfo {
	public static class InheritItem {
		//插件名
		String plugin;
		//文件名
		String file;
		//类型名
		String type;
		//目标缩放值,可选,-1表示不缩放
		int tarChance = -1;
		
		public InheritItem(String plugin, String file, String type, int tarChance) {
			super();
			this.plugin = plugin;
			this.file = file;
			this.type = type;
			this.tarChance = tarChance;
		}

		@Override
		public InheritItem clone() throws CloneNotSupportedException {
			InheritItem inheritItem = new InheritItem(plugin, file, type, tarChance);
			return inheritItem;
		}
	}
	//物品类型名
	private String name;
	//继承列表,不为null可为空(方式一时不为空,方式二时为空)
	private List<InheritItem> inherits = new ArrayList<>();
	//几率
	private int chance;
	//物品(方式一时为null,方式二时不为null)
	private ItemWrapper iw;
	//物品列表,不为null,为空说明未生成物品列表
	private ChanceHashList<ItemWrapper> itemList = new ChanceHashListImpl<>();
	//其它属性,不为null
	private HashMap<String, Object> properties = new HashMap<>();

	public ItemInfo(String name, List<InheritItem> inherits) {
		super();
		this.name = name;
		this.inherits = inherits;
	}

	public ItemInfo(String name, int chance, ItemWrapper iw) {
		this.name = name;
		this.chance = chance;
		this.iw = iw;
	}

	public ItemInfo(String name, List<InheritItem> inherits, int chance,
			ItemWrapper iw, ChanceHashList<ItemWrapper> itemList,
			HashMap<String, Object> properties) {
		this.name = name;
		this.inherits = inherits;
		this.chance = chance;
		this.iw = iw;
		this.itemList = itemList;
		this.properties = properties;
	}

	public String getName() {
		return name;
	}

	public List<InheritItem> getInherits() {
		return inherits;
	}

	public int getChance() {
		return chance;
	}

	public ItemWrapper getIw() {
		return iw;
	}

	public ChanceHashList<ItemWrapper> getItemList() {
		return itemList;
	}

	public HashMap<String, Object> getProperties() {
		return properties;
	}
	
	/**
	 * 由当前物品配置生成物品列表<br>
	 * 读取物品配置时调用一次就可以了
	 */
	public void generateItemWrappers() {
		//继承
		for (InheritItem ii:inherits) {
			ItemInfo info = ItemsMain.getItemInfo(ii.plugin, ii.file, ii.type);
			if (info.getItemList().isEmpty()) info.generateItemWrappers();
			ChanceHashList<ItemWrapper> list = info.getItemList().clone();
            if (ii.tarChance != -1) list.updateTotalChance(ii.tarChance);
			this.itemList.convert(list, false);
		}
		//本身
		if (iw != null) this.itemList.addChance(iw, chance);
	}
	
	/**
	 * 复制的仅有itemList
	 * @return 异常返回null
	 */
	@Override
	public ItemInfo clone() {
		try {
			ChanceHashList<ItemWrapper> list = new ChanceHashListImpl<>();
			for (ItemWrapper i:itemList) list.addChance(i.clone(), itemList.getChance(i));
			return new ItemInfo(name, inherits, chance, iw, list, properties);
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
