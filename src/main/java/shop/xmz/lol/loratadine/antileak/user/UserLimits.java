package shop.xmz.lol.loratadine.antileak.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import shop.xmz.lol.loratadine.antileak.utils.JsonUtil;

import java.util.Arrays;

public class UserLimits {
    public String tag;
    public boolean chatColor = false;
    public boolean muteLimit = false;
    public boolean kickLimit = false;
    public long muteTime = 0;
    public String inviter = "";
    public String[] invitedUser = new String[]{};

    public JsonObject toObject() {
        JsonObject json = new JsonObject();

        json.addProperty("tag", tag);
        json.addProperty("chatColor", chatColor);
        json.addProperty("muteLimit", muteLimit);
        json.addProperty("kickLimit", kickLimit);
        json.addProperty("muteTime", muteTime);
        json.addProperty("inviter", inviter);
        json.addProperty("Invited", Arrays.toString(invitedUser).substring(1, invitedUser.length - 1));

        return json;
    }

    public static UserLimits fromObject(String json) {
        final JsonElement element = JsonParser.parseString(json);
        if (!element.isJsonObject()) return new UserLimits();
        final JsonUtil jsonUtil = new JsonUtil((JsonObject) element);
        final UserLimits userLimits = new UserLimits();

        userLimits.tag = jsonUtil.getString("tag", "");
        userLimits.chatColor = jsonUtil.getBoolean("chatColor", false);
        userLimits.muteLimit = jsonUtil.getBoolean("muteLimit", false);
        userLimits.kickLimit = jsonUtil.getBoolean("kickLimit", false);
        userLimits.muteTime = jsonUtil.getLong("muteTime", 0);
        userLimits.inviter = jsonUtil.getString("inviter", "");
        userLimits.invitedUser = jsonUtil.getString("invited", "").split(",");

        return userLimits;
    }
}