package plus.ds.msg.utl;


public enum Emojis {

    TEA ("☕"),
    MAIL_E ("\uD83D\uDCE7"),
    MAIL_N ("✉"),
    MAIL_H ("\uD83D\uDC8C"),
    CRYSTAL("\uD83D\uDC8E"),
    RUBY ("♦"),
    TRUE_G ("✅"),
    TRUE_B ("☑"),
    FALSE_G ("❎"),
    FALSE_R ("❌"),
    UP ("⬆"),
    DOWN ("⬇"),
    UP_DOWN ("↕"),
    RIGHT ("➡"),
    LEFT("⬅"),
    RIGHT_LEFT ("↔"),
    FLEEP ("\uD83D\uDD04"),
    WARN ("⚠"),
    ANGER ("\uD83D\uDCA2"),
    TOP_1 ("\uD83E\uDD47"),
    TOP_2 ("\uD83E\uDD48"),
    TOP_3 ("\uD83E\uDD49"),
    BOOK_1 ("\uD83D\uDCD5"),
    BOOK_2 ("\uD83D\uDCD2"),
    BOOK_3 ("\uD83D\uDCD7"),
    BOOK_4 ("\uD83D\uDCD4"),
    BOOK_5 ("\uD83D\uDCD9"),
    BOOK_6 ("\uD83D\uDCD8"),
    BOOK_7 ("\uD83D\uDCD3"),
    STAR_1 ("⭐"),
    STAR_2 ("\uD83D\uDCAB"),
    STAR_3 ("☄"),
    STAR_4 ("\uD83C\uDF1F"),
    LUCK ("\uD83C\uDF40"),
    BOOM ("\uD83D\uDCA5"),
    FIRE ("\uD83D\uDD25"),
    WATER("\uD83D\uDCA7"),
    ENERGY ("⚡"),
    RADIATION_1 ("☢"),
    RADIATION_2 ("☣"),
    TNT ("\uD83E\uDDE8"),
    HAT ("\uD83C\uDFA9"),
    ANCHOR ("⚓"),
    BOW ("\uD83C\uDFF9"),
    SWORDS ("⚔"),
    MACBOOOK ("\uD83D\uDCBB"),
    EARTH ("\uD83C\uDF0F"),
    GLOBAL ("\uD83C\uDF10"),
    VOLCANO("\uD83C\uDF0B"),
    ISLAND ("\uD83C\uDFDD"),
    MOUNT ("⛰"),
    SNOW_MOUNT ("\uD83C\uDFD4"),
    DESERT ("\uD83C\uDFDC"),
    ICO_1 ("\uD83C\uDF07"),
    ICO_2 ("\uD83C\uDF09"),
    ICO_3 ("\uD83C\uDF05"),
    ICO_4 ("\uD83C\uDF06"),
    ICO_5 ("\uD83C\uDF03"),
    ICO_6 ("\uD83C\uDF04"),
    PAIMON ("✨");

    public final String E;
    
    Emojis(final String value) {
        E = value;
    }
    
    
    public String toString(){
        return E;
    }
}
