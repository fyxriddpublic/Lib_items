package com.fyxridd.lib.items.api;

import com.fyxridd.lib.items.ItemsEdit;
import com.fyxridd.lib.items.ItemsMain;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemsApi{
    /**
     * 重新读取指定插件的物品配置,包括:<br>
     *   - 物品类型: 保存在plugins/<b>plugin</b>/items文件夹下,文件名xxx.yml的都是<br>
     *   - 获取类型: 保存在传进来的<b>ms</b>中
     * @param plugin 插件名,可为null(null时无效果)
     * @param ms 配置,可为null(null时无效果)
     */
    public static void reloadItems(String plugin, MemorySection ms) {
        ItemsMain.reloadItems(plugin, ms);
    }

    /**
     * 将物品保存为字符串(包括保存Attributes)
     * @param is 物品,可为null(null时返回null)
     * @return 异常返回null
     */
    public static String saveItem(ItemStack is) {
        return ItemsMain.saveItem(is);
    }

    /**
     * 从字符串中读取物品(包括读取Attributes)
     * @param data 字符串,可为null(null时返回null)
     * @return 异常返回null
     */
    public static ItemStack loadItem(String data) {
        return ItemsMain.loadItem(data);
    }

    /**
     * 获取检测成功的物品列表
     * @param plugin 插件名,可为null(null时返回空列表)
     * @param type 获取类型,可为null(null时返回空列表)
     * @return 检测成功的物品列表,出错返回空列表
     */
    public static List<ItemStack> getItems(String plugin, String type) {
        return ItemsMain.getItems(plugin, type);
    }


    /**
     * 保存物品信息(包括保存Attributes)
     * @param ms 物品信息将被保存在这里,可为null(null时无效果)
     * @param type 物品类型名,可为null(null时无效果)
     * @param is 物品,可为null(null时无效果)
     */
    public static void saveItemStack(MemorySection ms, String type, ItemStack is) {
        ItemsMain.saveItemStack(ms, type, is);
    }

    /**
     * 获取物品(包括读取Attributes)
     * @param ms 可为null(null时返回null)
     * @return 物品信息,异常返回null
     */
    public static ItemStack loadItemStack(MemorySection ms) {
        return ItemsMain.loadItemStack(ms);
    }

    /**
     * 获取玩家的物品编辑框
     * @param name 玩家名,可为null(null时返回null)
     * @param create 如果没有是否创建
     * @return 玩家的物品编辑框,没有则返回null
     */
    public static Inventory getInv(String name, boolean create) {
        return ItemsEdit.getInv(name, create);
    }
}
