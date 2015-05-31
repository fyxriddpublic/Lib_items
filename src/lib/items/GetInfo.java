package lib.items;

import java.util.List;

public class GetInfo {
	public static class GetItem {
		//插件名
		String plugin;
		//文件名
		String file;
		//物品类型名
		String type;
		//ItemInfo的一个副本
		//目的是提高检测效率
		//缺点是加大内存使用
		ItemInfo itemInfo;
		
		//1表示<all/single>:<min次数>-<max次数>
		//2表示<几率缩放目标值>/<最大几率>:<min数量>-<max数量>
		int method;
		
		//method 1
		//true表示all,false表示single
		boolean all;
		//min次数 max次数
		int minTimes,maxTimes;
		
		//method 2
		//几率缩放目标值
		int tarChance;
		//最大几率
		int maxChance;
		//min数量 max数量
		int minAmount,maxAmount;
		
		public GetItem(String plugin, String file, String type, int method,
				boolean all, int minTimes, int maxTimes, int tarChance,
				int maxChance, int minAmount, int maxAmount, ItemInfo itemInfo) {
			super();
			this.plugin = plugin;
			this.file = file;
			this.type = type;
			this.method = method;
			this.all = all;
			this.minTimes = minTimes;
			this.maxTimes = maxTimes;
			this.tarChance = tarChance;
			this.maxChance = maxChance;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			this.itemInfo = itemInfo;
			//计算更新几率
			itemInfo.getItemList().updateTotalChance(tarChance);
		}
		
	}
	
	//获取类型名
	private String name;
	private List<GetItem> list;
	public GetInfo(String name, List<GetItem> list) {
		super();
		this.name = name;
		this.list = list;
	}
	public String getName() {
		return name;
	}
	public List<GetItem> getList() {
		return list;
	}
}
