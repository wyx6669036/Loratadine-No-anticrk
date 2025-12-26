package shop.xmz.lol.loratadine.antileak.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import shop.xmz.lol.loratadine.antileak.utils.JsonUtil;

public class UserTime {
    public long amiri;
    public long loratadine;
    public long sigma;
    public long rainytime;

    public JsonObject toObject(){
        final JsonObject time = new JsonObject();
        time.addProperty("amiri", amiri);
        time.addProperty("loratadine", loratadine);
        time.addProperty("sigma", sigma);
        time.addProperty("rainytime", rainytime);
        return time;
    }

    public static UserTime fromObject(String time){
        final JsonElement element = JsonParser.parseString(time);
        if (!element.isJsonObject()) return new UserTime();
        final JsonUtil jsonUtil = new JsonUtil((JsonObject) element);
        final UserTime userTime = new UserTime();

        userTime.amiri = jsonUtil.getLong("amiri", 0);
        userTime.loratadine = jsonUtil.getLong("loratadine", 0);
        userTime.sigma = jsonUtil.getLong("sigma", 0);
        userTime.rainytime = jsonUtil.getLong("rainytime", 0);

        return userTime;
    }
}
