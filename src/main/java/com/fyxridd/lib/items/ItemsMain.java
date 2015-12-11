package com.fyxridd.lib.items;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.hashList.ChanceHashList;
import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import com.fyxridd.lib.items.GetInfo.GetItem;
import com.fyxridd.lib.items.ItemInfo.InheritItem;
import com.fyxridd.lib.items.api.ItemsApi;
import com.fyxridd.lib.items.api.ItemsPlugin;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemsMain{
    //缓存
    public static ItemsMain instance;
    public static ItemsConfig itemsConfig;
    public static GetItemsManager getItemsManager;
    public static ItemsEdit itemsEdit;

	//插件名 获取类型名 获取类型
	private static HashMap<String, HashMap<String, GetInfo>> getHash = new HashMap<>();
	//插件名 文件名 物品类型名 物品类型
	private static HashMap<String, HashMap<String, HashMap<String, ItemInfo>>> itemHash = new HashMap<>();

    //临时缓存
	//'plugin:file:type'
	private static HashList<String> loading = new HashListImpl<>();

	public ItemsMain() {
        instance = this;

        //初始化配置
        initConfig();

        itemsConfig = new ItemsConfig();
        getItemsManager = new GetItemsManager();
        itemsEdit = new ItemsEdit();
	}

    /**
     * @see com.fyxridd.lib.items.api.ItemsApi#reloadItems(String, org.bukkit.configuration.MemorySection)
     */
    public void reloadItems(String plugin, MemorySection ms) {
        if (plugin == null || ms == null) return;

        //重新读取物品类型
        loadItemInfo(plugin);
        //重新读取获取类型
        loadGetInfo(plugin, ms);
    }

    /**
	 * 获取检测成功的物品列表
	 * @param plugin 插件名,不为null
	 * @param type 获取类型,不为null
	 * @return 检测成功的物品列表,出错返回空列表
	 */
	public List<ItemStack> getItems(String plugin, String type) {
		List<ItemStack> result = new ArrayList<>();
        if (plugin == null || type == null) return result;

        GetInfo getInfo;
        try {
            getInfo = getHash.get(plugin).get(type);
            if (getInfo == null) return result;
        } catch (Exception e) {
            return result;
        }
		for (GetItem getItem:getInfo.getList()) {
			ItemInfo itemInfo = getItem.itemInfo;
			if (itemInfo != null && !itemInfo.getItemList().isEmpty()) {//不为null且有内容
				if (getItem.method == 1) {//方式一
					int times = CoreApi.Random.nextInt(getItem.maxTimes-getItem.minTimes+1)+getItem.minTimes;
					if (getItem.all) {//可重复取
						for (int i=0;i<times;i++) {
							ItemStack is = itemInfo.getItemList().getRandom().getItem();
							if (is != null) result.add(is);
						}
					}else {//不重复取
                        ChanceHashList<ItemWrapper> list = itemInfo.getItemList().clone();
						for (int i=0;i<times;i++) {
							ItemWrapper iw = list.getRandom();
                            list.remove(iw);
                            ItemStack is = iw.getItem();
                            if (is != null) result.add(is);
                            //已经没有物品了
                            if (list.isEmpty()) break;
						}
					}
				}else {//方式二
					//已取到数
					int sum = 0;
					//剩余可取的物品数
					int left = itemInfo.getItemList().size();
					//必须取到
					boolean must = false;
					for (ItemWrapper iw:itemInfo.getItemList()) {
						if (!must && left <= getItem.minAmount-sum) must = true;
						left --;
						if (sum >= getItem.maxAmount) break;//已经达到最大数量
						int chance = itemInfo.getItemList().getChance(iw);
						if (must || (chance > 0 && CoreApi.Random.nextInt(getItem.maxChance) < chance)) {
							sum ++;
                            ItemStack is = iw.getItem();
                            if (is != null) result.add(is);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 获取指定的物品类型
	 * @param plugin 插件名,不为null
	 * @param file 文件名,不为null
	 * @param type 物品类型名,不为null
	 * @return 异常返回null
	 */
	public ItemInfo getItemInfo(String plugin, String file, String type) {
		try {
			return itemHash.get(plugin).get(file).get(type);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 重新读取此插件所有的物品类型
	 * @param plugin 插件名
	 */
	private void loadItemInfo(String plugin) {
		//物品类型保存的文件夹路径
		String itemPath = ItemsPlugin.pluginPath+File.separator+plugin+File.separator+"items";
		File dir = new File(itemPath);
		dir.mkdirs();
		//清空旧的
		itemHash.remove(plugin);
		//读取新的
		HashMap<String, HashMap<String, ItemInfo>> fileHash = new HashMap<>();
		for (File file:dir.listFiles()) {
			if (file.isFile() && file.canRead() && file.getName().endsWith(".yml")) {
				String fileName = file.getName().substring(0, file.getName().length()-4).trim();
				if (!fileName.isEmpty()) {
					HashMap<String,ItemInfo> typeHash = loadItemInfo(plugin, file);
					if (!typeHash.isEmpty()) fileHash.put(fileName, typeHash);
				}
			}
		}
		if (!fileHash.isEmpty()) {
			itemHash.put(plugin, fileHash);
			//更新生成ItemWrappers
            for (HashMap<String, ItemInfo> typeHash:fileHash.values()) {
                for (ItemInfo info:typeHash.values()) info.generateItemWrappers();
            }
		}
	}

	/**
	 * 从文件中读取"物品类型名 物品类型"列表
	 * @param plugin 插件名
	 * @param file 文件
	 * @return 异常返回空的hash
	 */
	private HashMap<String, ItemInfo> loadItemInfo(String plugin, File file) {
        String fileName = file.getName().substring(0, file.getName().length()-4).trim();
		HashMap<String,ItemInfo> typeHash = new HashMap<>();
		try {
			YamlConfiguration config = CoreApi.loadConfigByUTF8(file);
			for (String type:config.getValues(false).keySet()) {
				loading.clear();//根结点,清空loading
				ItemInfo info = loadItemInfo(plugin, fileName, type);
				if (info != null) typeHash.put(type, info);
			}
			return typeHash;
		} catch (Exception e) {
			return typeHash;
		}
	}

	/**
	 * 读取物品类型
	 * @param plugin 插件名
	 * @param file 文件名
	 * @param type 物品类型名
	 * @return 异常返回null
	 */
	private ItemInfo loadItemInfo(String plugin, String file, String type) {
		try {
			//已经读取过
			ItemInfo itemInfo = getItemInfo(plugin, file, type);
			if (itemInfo != null) return itemInfo;
			//没读取过
			String check = plugin+":"+file+":"+type;//检测死循环用
			if (loading.has(check)) return null;//死循环异常
			loading.add(check);
			//
			String itemPath = ItemsPlugin.pluginPath+File.separator+plugin+File.separator+"items"+File.separator+file+".yml";
			YamlConfiguration config = CoreApi.loadConfigByUTF8(new File(itemPath));
			//方式判断
			MemorySection ms = (MemorySection) config.get(type);
            if (ms.contains("inherits")) {//方式一
                List<InheritItem> inherits = new ArrayList<>();
                for (String s:ms.getStringList("inherits")) {
                    String[] s1 = s.split(" ");
                    if (s1.length > 2) continue;//异常
                    int tarChance;
                    if (s1.length == 1) {
                        tarChance = -1;
                    }else {
                        tarChance = Integer.parseInt(s1[1]);
                    }
                    String[] ss = s1[0].split("\\:");
                    String f,t;
                    if (ss.length == 1) {
                        f = file;
                        t = ss[0];
                    }else if (ss.length == 2) {
                        f = ss[0];
                        t = ss[1];
                    }else continue;//异常
                    ItemInfo info = loadItemInfo(plugin, f, t);
                    if (info == null) continue;
                    InheritItem ii = new InheritItem(plugin, f, t, tarChance);
                    inherits.add(ii);
                }
                return new ItemInfo(type, inherits);
            }else if (ms.contains("getType")) {//方式二
                int chance = ms.getInt("chance", 1);
                String[] args = ms.getString("getType").split(" ", 3);
                String getPlugin = args[0];
                String getType = args[1];
                String getArg = args[2];
                ItemWrapper iw = new ItemWrapper(getPlugin, getType, getArg);
                //返回
                return new ItemInfo(type, chance, iw);
            }else {//方式三
				ItemStack is = ItemsApi.loadItemStack(ms);
				if (is == null) return null;
				int chance = ms.getInt("chance", 1);
				ItemWrapper iw = new ItemWrapper(is);
                //返回
				return new ItemInfo(type, chance, iw);
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 重新读取此插件所有的获取类型
	 */
	private void loadGetInfo(String plugin, MemorySection ms) {
		//清空旧的
		getHash.remove(plugin);
		//读取新的
		HashMap<String, GetInfo> hash = new HashMap<>();
		if (ms != null) {
			for (String key:ms.getValues(false).keySet()) {
				List<String> list = ms.getStringList(key);
				if (!list.isEmpty()) {
					List<GetItem> itemList = new ArrayList<>();
					for (String s:list) {
						GetItem getItem = loadGetItem(plugin, s);
						if (getItem != null) itemList.add(getItem);
					}
					if (!itemList.isEmpty()) {//选取项列表不为空
						GetInfo getInfo = new GetInfo(key, itemList);
						hash.put(key, getInfo);
					}
				}
			}
		}
		if (!hash.isEmpty()) getHash.put(plugin, hash);
	}

	/**
	 * 读取获取项
	 * @param plugin 所属插件
	 * @param s 获取项字符串
	 * @return 异常返回null
	 */
	private GetItem loadGetItem(String plugin, String s) {
		String[] ss = s.split(" ");
		if (ss.length != 2) return null;//异常

		String[] sss = ss[0].split("\\:");
		String file, type;
		if (sss.length == 2) {
			file = sss[0];
			type = sss[1];
		}else return null;//异常
		//ItemInfo副本
		ItemInfo itemInfo = getItemInfo(plugin, file, type);
		if (itemInfo == null) return null;//异常
		itemInfo = itemInfo.clone();
		if (itemInfo == null) return null;//异常

		String[] s2 = ss[1].split("\\:");
		if (s2.length != 2) return null;//异常

		String[] s3 = s2[0].split("/");
		String[] s4 = s2[1].split("-");
		if (s4.length != 2) return null;//异常
		int method;
		boolean all = true;
		int minTimes = 0,maxTimes = 0;
		int tarChance = 0;
		int maxChance = 0;
		int minAmount = 0,maxAmount = 0;
		try {
			if (s3.length == 1) {
				method = 1;
				if (s2[0].equalsIgnoreCase("all")) all = true;
				else if (s2[0].equalsIgnoreCase("single")) all = false;
				else return null;//异常
				minTimes = Integer.parseInt(s4[0]);
				maxTimes = Integer.parseInt(s4[1]);
				if (minTimes < 0 || minTimes > maxTimes) return null;//异常
			}else {
				method = 2;
				tarChance = Integer.parseInt(s3[0]);
				if (tarChance < -1) return null;//异常
				maxChance = Integer.parseInt(s3[1]);
				if (maxChance <= 0) return null;//异常
				minAmount = Integer.parseInt(s4[0]);
				maxAmount = Integer.parseInt(s4[1]);
				if (minAmount < 0 || minAmount > maxAmount) return null;//异常
			}
		} catch (NumberFormatException e) {
			return null;//异常
		}
		return new GetItem(plugin, file, type, method, all, minTimes,
                maxTimes, tarChance, maxChance, minAmount, maxAmount, itemInfo);
	}

    private void initConfig() {
        ConfigApi.register(ItemsPlugin.file, ItemsPlugin.dataPath, ItemsPlugin.pn);
        ConfigApi.loadConfig(ItemsPlugin.pn);
    }
}
