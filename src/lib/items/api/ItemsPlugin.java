package lib.items.api;

import lib.core.api.CoreApi;
import lib.core.api.FormatApi;
import lib.items.ItemsMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ItemsPlugin extends JavaPlugin{
    public static ItemsPlugin instance;
    public static String pn;
    public static File file;
    public static String pluginPath;
    public static String dataPath;
    public static String ver;

    @Override
    public void onEnable() {
        instance = this;
        pn = getName();
        file = getFile();
        pluginPath = file.getParentFile().getAbsolutePath();
        dataPath = pluginPath+ File.separator+pn;
        ver = CoreApi.getPluginVersion(file);

        new ItemsMain();

        //成功启动
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 25, pn, ver).getText());
    }

    @Override
    public void onDisable() {
        //显示插件成功停止信息
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 30, pn, ver).getText());
    }
}
