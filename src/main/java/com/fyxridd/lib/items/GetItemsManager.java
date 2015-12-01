package com.fyxridd.lib.items;

import com.fyxridd.lib.items.api.handler.GetItemsHandler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 物品获取管理(动态物品获取)
 */
public class GetItemsManager {
    //缓存
    private HashMap<String, HashMap<String, GetItemsHandler>> handlers = new HashMap<>();

    /**
     * @see com.fyxridd.lib.items.api.ItemsApi#register(String, String, com.fyxridd.lib.items.api.handler.GetItemsHandler)
     */
    public void register(String plugin, String type, GetItemsHandler getItemsHandler) {
        if (plugin == null || type == null) return;

        HashMap<String, GetItemsHandler> getHandlers = handlers.get(plugin);
        if (getHandlers == null) {
            getHandlers = new HashMap<>();
            handlers.put(plugin, getHandlers);
        }
        getHandlers.put(type, getItemsHandler);
    }

    /**
     * @see com.fyxridd.lib.items.api.ItemsApi#getItems(String, String, String)
     */
    public List<ItemStack> getItems(String plugin, String type, String arg) {
        HashMap<String, GetItemsHandler> getHandlers = handlers.get(plugin);
        if (getHandlers != null) {
            GetItemsHandler getItemsHandler = getHandlers.get(type);
            if (getItemsHandler != null) return getItemsHandler.get(arg);
        }
        return new ArrayList<>();
    }
}
