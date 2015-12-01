package com.fyxridd.lib.items;

import org.bukkit.inventory.ItemStack;

/**
 * 如果两个对象是同一个地址,equals方法返回true,否则返回false
 */
public class ItemWrapper {
	private ItemStack item;

    public ItemWrapper(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public ItemWrapper clone() throws CloneNotSupportedException {
		return new ItemWrapper(item.clone());
	}
}
