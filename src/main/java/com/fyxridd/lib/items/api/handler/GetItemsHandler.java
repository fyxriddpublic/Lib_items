package com.fyxridd.lib.items.api.handler;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 物品获取器
 * 适合动态获取物品
 */
public interface GetItemsHandler {
    /**
     * 获取物品时调用
     * @param arg 变量,可为null
     * @return 不为null
     */
    public List<ItemStack> get(String arg);
}
