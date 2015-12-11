package com.fyxridd.lib.items;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.TransactionApi;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.items.api.ItemsApi;
import com.fyxridd.lib.items.api.ItemsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ItemsConfig implements Listener {
    //配置
    public static String editPer;

    public ItemsConfig() {
        //读取配置
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, ItemsPlugin.instance);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(ItemsPlugin.pn)) loadConfig();
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(ItemsPlugin.pn);

        editPer = config.getString("editPer");

        //重新读取物品配置
        ItemsApi.reloadItems(ItemsPlugin.pn, config);
        //重新读取提示
        TransactionApi.reloadTips(ItemsPlugin.pn);
    }
}
