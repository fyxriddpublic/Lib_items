package lib.items;

import lib.core.api.*;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.api.inter.FunctionInterface;
import lib.items.api.ItemsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;

public class ItemsEdit implements Listener,FunctionInterface {
    private static final String FUNC_NAME = "ItemsEdit";
    //物品编辑框大小
    private static final int SLOT = 36;

    private static String savePath;

    //配置
    private static String editPer;

    //缓存
    //玩家名 物品编辑框
	private static HashMap<String, Inventory> invHash = new HashMap<String, Inventory>();

	public ItemsEdit() {
		savePath = ItemsPlugin.dataPath+File.separator+"saveItems";
		//读取配置文件
		loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, ItemsPlugin.instance);
        //注册功能
        FuncApi.register(this);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(ItemsPlugin.pn)) loadConfig();
	}

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String subFunc) {
        return PerApi.has(name, editPer);
    }

    @Override
    public void setOn(String name, String subFunc, boolean on) {
        if (on) PerApi.add(name, editPer);
        else PerApi.del(name, editPer);
    }

    /**
     * 'a true/false' 打开物品编辑框,true/false表示是否重置
     * 'b 类型名 true/false' 保存类型,true/false表示是否强制保存
     * @param p 操作的玩家,不为null
     * @param data 操作的数据,可为null
     */
    @Override
    public void onOperate(Player p, String data) {
        if (data != null) {
            String[] args = data.split(" ");
            switch (args.length) {
                case 2:
                    if (args[0].equalsIgnoreCase("a")) {
                        boolean newInv = Boolean.parseBoolean(args[1]);
                        open(p, newInv);
                    }
                    break;
                case 3:
                    if (args[0].equalsIgnoreCase("b")) {
                        String type = args[1];
                        boolean force = Boolean.parseBoolean(args[2]);
                        save(p, type, force);
                    }
                    break;
            }
        }
    }

	/**
     * @see lib.items.api.ItemsApi#getInv(String, boolean)
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
                ItemsMain.saveItemStack(saveConfig, ""+i, is);
			}
		}
        if (CoreApi.saveConfigByUTF8(saveConfig, file)) ShowApi.tip(p, FormatApi.get(ItemsPlugin.pn, 60), true);
        else ShowApi.tip(p, FormatApi.get(ItemsPlugin.pn, 65), true);
	}

	private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(ItemsPlugin.pn);

        //editPer
        editPer = config.getString("editPer");
	}
}