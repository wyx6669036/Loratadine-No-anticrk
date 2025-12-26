package shop.xmz.lol.loratadine.antileak;

public class GirlFriend {
    public static Object girlFriend1 = new Object();
    public static Object girlFriend2 = new Object();
    public static Object girlFriend3 = new Object();
    public static Object girlFriend4 = new Object();
    public static Object girlFriend5 = new Object();
    public static Object girlFriend6 = new Object();
    public static Object girlFriend7 = new Object();
    public static Object girlFriend8 = new Object();
    public static Object guizuFriend = new Object();

    public GirlFriend() {
        girlFriend1 = girlFriend2;
        girlFriend2 = girlFriend3;
        girlFriend3 = girlFriend4;
        girlFriend4 = girlFriend5;
        girlFriend5 = girlFriend6;
        girlFriend6 = girlFriend7;
        girlFriend7 = girlFriend8;

        System.out.println("GirlFriend");
        Object girlFriend = new Object();
        System.out.println(girlFriend);
        System.out.println(girlFriend.hashCode());
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");

        guizuFriend = "LuoDaYou TeYiRenShi.";

        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
        System.out.println("omg!!! object !!!! girlFriend!!!!!!");
    }
}