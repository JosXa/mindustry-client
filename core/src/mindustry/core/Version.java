package mindustry.core;

import arc.*;
import arc.Files.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

public class Version{
    /** Build type. 'official' for official releases; 'custom' or 'bleeding edge' are also used. */
    public static String type = "unknown";
    /** Build modifier, e.g. 'alpha' or 'release' */
    public static String modifier = "unknown";
    /** Number specifying the major version, e.g. '4' */
    public static int number;
    /** Build number, e.g. '43'. set to '-1' for custom builds. */
    public static int build = 0;
    /** Revision number. Used for hotfixes. Does not affect server compatibility. */
    public static int revision = 0;
    /** Whether version loading is enabled. */
    public static boolean enabled = true;
    /** Foo's update url used for... updating */
    public static String updateUrl = "";
    /** Foo's asset repo, commit ref used for downloading muic on the go */
    public static String assetUrl = "", assetRef = "";
    /** Foo's version string */
    public static String clientVersion = "v1.0.0, Jan. 1, 1970";

    public static String path(){
        return Version.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
    public static void init(){
        if(!enabled) return;

        Fi file = OS.isAndroid || OS.isIos ? Core.files.internal("version.properties") : new Fi("version.properties", FileType.internal);

        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, file.reader());

        updateUrl = map.get("updateUrl");
        assetUrl = map.get("assetUrl");
        assetRef = map.get("assetRef");
        clientVersion = map.get("clientVersion");
        type = map.get("type");
        number = Integer.parseInt(map.get("number", "4"));
        modifier = map.get("modifier");
        String filepath = Version.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (filepath.contains("/steamapps/common/Mindustry/")) modifier += " steam";
        if(map.get("build").contains(".")){
            String[] split = map.get("build").split("\\.");
            try{
                build = Integer.parseInt(split[0]);
                revision = Integer.parseInt(split[1]);
            }catch(Throwable e){
                e.printStackTrace();
                build = -1;
            }
        }else{
            build = Strings.canParseInt(map.get("build")) ? Integer.parseInt(map.get("build")) : -1;
        }
    }

    /** @return whether the current game version is greater than the specified version string, e.g. "120.1"*/
    public static boolean isAtLeast(String str){
        return isAtLeast(build, revision, str);
    }

    /** @return whether the version numbers are greater than the specified version string, e.g. "120.1"*/
    public static boolean isAtLeast(int build, int revision, String str){
        if(build <= 0 || str == null || str.isEmpty()) return true;

        int dot = str.indexOf('.');
        if(dot != -1){
            int major = Strings.parseInt(str.substring(0, dot), 0), minor = Strings.parseInt(str.substring(dot + 1), 0);
            return build > major || (build == major && revision >= minor);
        }else{
            return build >= Strings.parseInt(str, 0);
        }
    }

    public static String buildString(){
        return build < 0 ? "custom" : build + (revision == 0 ? "" : "." + revision);
    }

    /** get menu version without colors */
    public static String combined(){
        if(build == -1){
            return "custom build";
        }
        return (type.equals("official") ? modifier : type) + " build " + build + (revision == 0 ? "" : "." + revision) + "\n(SByte Foo Fork Client Version: " + (clientVersion.equals("v0.0.0") ? "Dev" : clientVersion) + ")";
    }
}
