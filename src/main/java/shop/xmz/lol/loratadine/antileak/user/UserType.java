package shop.xmz.lol.loratadine.antileak.user;

public enum UserType {
    NORMAL("Normal", 15),
    VIP("VIP", 20),
    SVIP("SVIP", 25),
    MOD("Mod", 40),
    ADMIN("Admin", -1),
    CUSTOM("Custom", -1);

    public static UserType getFromName(String name) {
        for (UserType rank : values()) {
            if (rank.name.toLowerCase().equalsIgnoreCase(name))
                return rank;
        }

        return CUSTOM;
    }
    public String getDisplayName() {
        switch (this) {
            case NORMAL: {
                return "";
            }

            case VIP: {
                return "§a" + name;
            }

            case SVIP: {
                return "§6" + name;
            }

            case MOD: {
                return "§b" + name;
            }

            case ADMIN: {
                return "§4" + name;
            }
        }

        return name;
    }

    public final String name;
    public final int chatLimit;
    UserType(String name, int chatLimit) {
        this.name = name;
        this.chatLimit = chatLimit;
    }
}