package com.fyxridd.lib.items;

import com.fyxridd.lib.items.api.ItemsApi;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 如果两个对象是同一个地址,equals方法返回true,否则返回false
 */
public class ItemWrapper {
    //方式,2或3
    int method;
    //方式二
    private String getPlugin, getType, getArg;
    //方式三
    private ItemStack item;

    /**
     * 方式二
     */
    public ItemWrapper(String getPlugin, String getType, String getArg) {
        this.method = 2;
        this.getPlugin = getPlugin;
        this.getType = getType;
        this.getArg = getArg;
    }

    /**
     * 方式三
     */
    public ItemWrapper(ItemStack item) {
        this.method = 3;
        this.item = item;
    }

    /**
     * 获取物品,
     * 可能每次取到的都不同,
     * 可能有时取到null有时不是null,
     * 可能是静态取的(效率较高)也可能是动态取的(效率较低)
     * @return 物品的副本,可能为null
     */
    public ItemStack getItem() {
        switch (this.method) {
            case 2:
                List<ItemStack> list = ItemsApi.getItems(getPlugin, getType, getArg);
                if (!list.isEmpty()) return list.get(0).clone();
                break;
            case 3:
                return item.clone();
        }
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public ItemWrapper clone() throws CloneNotSupportedException {
        switch (method) {
            case 2:
                return new ItemWrapper(getPlugin, getType, getArg);
            case 3:
                return new ItemWrapper(item.clone());
        }
        return null;
	}
}
