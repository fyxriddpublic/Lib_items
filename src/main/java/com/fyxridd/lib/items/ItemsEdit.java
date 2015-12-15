package com.fyxridd.lib.items;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.inter.FunctionInterface;
import com.fyxridd.lib.items.api.ItemsApi;
import com.fyxridd.lib.items.api.ItemsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemsEdit implements FunctionInterface {
    private static final String FUNC_NAME = "ItemsEdit";
    //物品编辑框大小
    private static final int SLOT = 36;

    private static String savePath;

    //缓存
    //玩家名 物品编辑框
	private static HashMap<String, Inventory> invHash = new HashMap<>();

	public ItemsEdit() {
		savePath = ItemsPlugin.dataPath+File.separator+"saveItems";
        //注册功能
        FuncApi.register(this);
	}
	
    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String subFunc) {
        return PerApi.has(name, ItemsConfig.editPer);
    }

    /**
     * 'a true/false' 打开物品编辑框,true/false表示是否重置
     * 'b 类型名 true/false' 保存类型,true/false表示是否强制保存
     * 'c 插件名 获取类型名' 获取物品
     * 'd 插件名 文件名 物品类型名' 获取静态物品
     * 'e 插件名 类型名 [变量]' 获取动态物品
     */
    @Override
    public void onOperate(Player p, String... args) {
        if (args.length > 0) {
            try {
                if (args.length >= 3) {
                    if (args[0].equalsIgnoreCase("e")) {
                        getDynamicItems(p, args[1], args[2], args.length > 3 ? CoreApi.combine(args, " ", 3, args.length):null);
                        return;
                    }
                }

                switch (args.length) {
                    case 2:
                        if (args[0].equalsIgnoreCase("a")) {
                            boolean newInv = Boolean.parseBoolean(args[1]);
                            open(p, newInv);
                            return;
                        }
                        break;
                    case 3:
                        if (args[0].equalsIgnoreCase("b")) {
                            String type = args[1];
                            boolean force = Boolean.parseBoolean(args[2]);
                            save(p, type, force);
                            return;
                        }else if (args[0].equalsIgnoreCase("c")) {
                            getItems(p, args[1], args[2]);
                            return;
                        }
                        break;
                    case 4:
                        if (args[0].equalsIgnoreCase("d")) {
                            getStaticItems(p, args[1], args[2], args[3]);
                            return;
                        }
                        break;
                }
            } catch (Exception e) {//操作异常
                ShowApi.tip(p, get(5), true);
                return;
            }
            //输入格式错误
            ShowApi.tip(p, get(10), true);
        }
    }

    /**
     * @see com.fyxridd.lib.items.api.ItemsApi#getInv(String, boolean)
	 */
	public static Inventory getInv(String name, boolean create) {
        if (name == null) return null;
        //目标玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return null;
        //
        if (!invHash.containsKey(name) && create)
            invHash.put(name, Bukkit.createInventory(null, SLOT, FormatApi.get(ItemsPlugin.pn, 40, name).getText()));
		return invHash.get(name);
	}

	/**
	 * 玩家请求打开物品编辑框
	 * @param p 玩家,不为null
	 * @param newInv true表示重置物品编辑框
	 */
	private static void open(Player p, boolean newInv) {
        //检测权限
        if (!PerApi.checkPer(p, ItemsConfig.editPer)) return;
		//检测新建物品编辑框
		String name = p.getName();
		if (!invHash.containsKey(name) || newInv)
			invHash.put(name, Bukkit.createInventory(null, SLOT, FormatApi.get(ItemsPlugin.pn, 40, name).getText()));
		//打开
		Inventory inv = invHash.get(name);
		p.openInventory(inv);
	}

	/**
	 * 玩家请求保存类型
	 * @param p 玩家,不为null
	 * @param type 类型名,不为null
	 * @param force true表示强制保存(如果原来有文件则覆盖)
	 */
	private static void save(Player p, String type, boolean force) {
        //检测权限
        if (!PerApi.checkPer(p, ItemsConfig.editPer)) return;
		//物品编辑框里没有物品
		String name = p.getName();
		if (!invHash.containsKey(name) || ItemApi.getEmptySlots(invHash.get(name)) == invHash.get(name).getSize()) {
            ShowApi.tip(p, FormatApi.get(ItemsPlugin.pn, 50), true);
			return;
		}
		//类型已经存在
		String saveFilePath = savePath+File.separator+type+".yml";
		if (!force && new File(saveFilePath).exists()) {
            ShowApi.tip(p, FormatApi.get(ItemsPlugin.pn, 55, type), true);
			return;
		}
		//保存
        File file = new File(saveFilePath);
        file.getParentFile().mkdirs();
		YamlConfiguration saveConfig = new YamlConfiguration();
		Inventory inv = invHash.get(name);
		for (int i=0;i<inv.getSize();i++) {
            ItemStack is = inv.getItem(i);
			if (is != null &&
                    !is.getType().equals(Material.AIR) &&
                    is.getAmount() > 0) {//保存物品信息
                ItemsApi.saveItemStack(saveConfig, "" + i, is);
			}
		}
        if (CoreApi.saveConfigByUTF8(saveConfig, file)) ShowApi.tip(p, FormatApi.get(ItemsPlugin.pn, 60), true);
        else ShowApi.tip(p, FormatApi.get(ItemsPlugin.pn, 65), true);
	}

    private void getItems(Player p, String plugin, String getType) {
        //检测权限
        if (!PerApi.checkPer(p, ItemsConfig.editPer)) return;

        //获取物品列表
        List<ItemStack> list = ItemsApi.getItems(plugin, getType);

        //添加物品
        p.getInventory().addItem(list.toArray(new ItemStack[list.size()]));

        //延时更新背包
        CoreApi.updateInventoryDelay(p);

        //提示
        ShowApi.tip(p, get(120), true);
    }

    private void getDynamicItems(Player p, String plugin, String type, String arg) {
        //检测权限
        if (!PerApi.checkPer(p, ItemsConfig.editPer)) return;

        //获取物品列表
        List<ItemStack> list = ItemsApi.getItems(plugin, type, arg);

        //添加物品
        p.getInventory().addItem(list.toArray(new ItemStack[list.size()]));

        //延时更新背包
        CoreApi.updateInventoryDelay(p);

        //提示
        ShowApi.tip(p, get(120), true);
    }

    private void getStaticItems(Player p, String plugin, String file, String type) {
        //检测权限
        if (!PerApi.checkPer(p, ItemsConfig.editPer)) return;

        //获取物品列表
        List<ItemStack> list = new ArrayList<>();
        ItemInfo itemInfo = ItemsMain.instance.getItemInfo(plugin, file, type);
        if (itemInfo != null) {
            for (ItemWrapper iw:itemInfo.getItemList()) {
                ItemStack is = iw.getItem();
                if (is != null) list.add(is);
            }
        }

        //添加物品
        p.getInventory().addItem(list.toArray(new ItemStack[list.size()]));

        //延时更新背包
        CoreApi.updateInventoryDelay(p);

        //提示
        ShowApi.tip(p, get(120), true);
    }

    private FancyMessage get(int id, Object... args) {
        return FormatApi.get(ItemsPlugin.pn, id, args);
    }
}
