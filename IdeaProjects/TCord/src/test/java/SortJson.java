import plus.tcord.JTranslationPack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class SortJson {
    public static GroupComparator create(){
        GroupComparator comp = new GroupComparator();
        {
            //characters
            {
                comp.add("Makiatto", -33);
                comp.add("Andoris", -32);
                comp.add("Lotta", -31);
                comp.add("Belka", -30);
                comp.add("Helena", -29);
                comp.add("Lenna", -28);
                comp.add("Lumi", -27);
                comp.add("Ksenia", -26);
                comp.add("Mayling", -25);
                comp.add("Mosin-Nagant", -24);
                comp.add("Dushevnaya", -23);
                comp.add("Suomi", -22);
                comp.add("Vector", -21);
                comp.add("Krolik", -20);
                comp.add("Nemesis", -19);
                comp.add("Tololo", -18);
                comp.add("Yoohee", -17);
                comp.add("Daiyan", -16);
                comp.add("Vepley", -15);
                comp.add("Klukai", -14);
                comp.add("Cheeta", -13);
                comp.add("Nagant", -12);
                comp.add("Mechty", -11);
                comp.add("Colphne", -10);
                comp.add("Groza", -9);

                comp.add("Commander", -8);
                comp.add("Commander, ", -7);
                comp.add("Leader", -6);
            }

            //common lore
            {
                comp.addB("How can", 7);
                comp.add("Everyone,", 7);
                comp.addB("I mean", 7);
                comp.addB("Could you", 7);
                comp.addB("Big Sis", 7);
                comp.add("Sure.", 7);
                comp.addB("Well,", 7);
                comp.add("Why ", 7);
                comp.add("Once you", 7);
                comp.add("I believe", 7);
                comp.add("I could", 7);
                comp.add("It was", 7);
                comp.add("Perhaps", 7);
                comp.add("Anyway,", 7);
                comp.add("Hello", 7);
                comp.addB("I heard", 7);
                comp.add("Some ", 7);
                comp.addB("However,", 7);
                comp.addB("Of course", 7);
                comp.addB("Okay,", 7);
                comp.addB("Speaking of", 7);
                comp.addB("Look, ", 7);
                comp.addB("Looks ", 7);
                comp.addB("Ha,", 7);
                comp.addB("Hehe", 7);
                comp.addB("you to", 7);
                comp.addB("I just", 7);
                comp.add("There's", 7);
                comp.add("Don't ", 7);
                comp.addB("In any case", 7);
                comp.add("Is that", 7);
                comp.add("Miss ", 7);
                comp.add("My ", 7);
                comp.add("No,", 7);
                comp.add("Mmm", 7);
            }
            {
                comp.add("Did ", 7);
                comp.add("I've", 7);
                comp.add("Their", 7);
                comp.add("Our ", 7);
                comp.addB("So ", 7);
                comp.add("Sure, ", 7);
                comp.addB("This is ", 7);
                comp.add("Who ", 7);
                comp.add("Also", 7);
                comp.add("But ", 7);
                comp.add("A new ", 7);
                comp.add("After all", 7);
                comp.add("Good ", 7);
                comp.add("Will", 7);
                comp.add("Thank", 7);
                comp.addB("...", 7);
                comp.add("And ", 7);
                comp.add("We're", 7);
                comp.add("Well ", 7);
                comp.addB("See?", 7);
                comp.add("See", 7);
                comp.add("See,", 7);
                comp.add("Yup", 7);
                comp.add("Yes", 7);
                comp.add("Yeah", 7);
                comp.add("Although", 7);
                comp.addB("You", 7);
                comp.addB("I'm", 7);
                comp.add("They're", 7);
                comp.add("I was", 7);
                comp.add("Hm", 7);
                comp.add("Not ", 7);
                comp.add("A Doll", 7);
                comp.add("I will", 7);
                comp.add("Hah", 7);
                comp.add("That", 7);
                comp.add("They ", 7);
                comp.addB("Sorry", 7);
                comp.add("Hey", 7);
                comp.addB("I know", 7);
                comp.addB("I see", 7);
                comp.add("Wait", 7);
                comp.addB("Understood.", 7);
                comp.add("We ", 7);
                comp.add("What", 7);
                comp.add("Ah", 7);
                comp.add("Then", 7);
                comp.add("I can", 7);
                comp.add("Indeed", 7);
                comp.addB("Boss, ", 7);
                comp.add("Oh", 7);
                comp.add("Alright", 7);
                comp.add("Do you ", 7);
                comp.add("I did", 7);
                comp.add("Huh? ", 7);
                comp.add("I think", 7);
                comp.addB("I'll", 7);
                comp.add("It's", 7);
                comp.addB("So, ", 7);
                comp.add("Just ", 7);
                comp.add("Let's", 7);
                comp.add("(", 8);
            }
            //common lore end

            comp.add("Active /", 9);
            comp.add("Passive /", 10);
            comp.add("Ultimate /", 11);

            //special-2
            {
                comp.add("Level ", 13);
                comp.add("Cognitive Configuration - ", 14);
                comp.add("Badge - ", 19);
                comp.add("Common Key - ", 20);
                comp.add("Fixed Key - ", 21);
                comp.add("Expansion Key - ", 22);
                comp.add("Complete ", 30);
                comp.add("Story Campaign", 31);
                comp.add("Eliminate ", 40);
                comp.add("AR Advanced Attachment", 50);
                comp.add("SMG Advanced Attachment Box", 51);
                comp.add("RF Advanced Attachment Box", 52);
                comp.add("Trilogy ", 55);
                comp.add("Neural Helix - ", 69);
                comp.add("Imaginary Friend -", 60);
                comp.add("Imprinted Skill -", 70);
                comp.add("Weapon Blueprint - ", 71);
                comp.add("Portrait - ", 72);
                comp.add("Outfit - ", 73);
                comp.add("Trial - ", 74);
                comp.add("Skin - ", 75);
                comp.add("Target - ", 76);
                comp.add("Affinity Gift - ", 77);
                comp.add("Icon: ", 80);
                comp.add("Icon Frame: ", 90);
                comp.add("Portrait Frame -", 91);
                comp.add("An epic weapon", 100);
                comp.add("Attachment skin", 110);
                comp.add("Reinforced Frontal Hunter", 120);
                comp.add("Total Siege", 121);
                comp.add("Extreme ", 122);
                comp.add("A weapon skin", 123);
                comp.add("Achieve Excellent", 130);
                comp.add("Activate ", 131);
                comp.add("Expand ", 132);
                comp.add("Phase 1", 140);
                comp.add("Phase 2", 141);
                comp.add("Phase 3", 142);
                comp.add("Dark Zone ", 143);
                comp.add("Hazard Zone ", 144);
                comp.add("Season Scene ", 145);
                comp.add("Projection ", 146);
                comp.add("Score Reward", 147);
                comp.add("Description ", 148);
            }

            //skills
            {
                comp.add("Increase basic ", 149);
                comp.add("When there are", 150);
                comp.add("At the start", 151);
                comp.add("At the end", 152);
                comp.add("Regular Bullet", 153);
                comp.add("Damage is increased by", 154);
                comp.add("Damage dealt ", 155);
                comp.add("Enemy units take ", 156);
                comp.add("Enemy units evacuated", 157);
                comp.add("Defense ", 158);
                comp.add("Attack ", 159);
                comp.add("Select ", 160);
                comp.add("Increases damage dealt", 161);
                comp.add("Increase attack ", 162);
                comp.add("Increase mobility ", 163);
                comp.add("Active skill damage", 164);
                comp.add("Selects 1", 165);
                comp.add("Selects an", 165);
                comp.add("Place <", 166);
                comp.add("Places <", 166);
                comp.add("After killing", 167);
                comp.add("Increase <", 168);
                comp.add("Increases damage ", 169);
                comp.add("Increase damage ", 170);
                comp.add("Reduces damage ", 171);
                comp.add("Reduce damage ", 172);
                comp.add("Deals <", 173);
                comp.add("Burn damage ", 174);
                comp.add("Active healing ", 175);
                comp.add("Stability damage taken", 176);
                comp.add("Phase damage is", 177);
                comp.add("Critical damage ", 178);
                comp.add("Mobility increased by", 179);
                comp.add("Before attacking, there", 180);
                comp.add("Deals AoE", 181);
                comp.add("For every <", 182);
                comp.add("Increase targeted ", 183);
                comp.add("Increase healing ", 184);
                comp.add("When attacking", 185);
                comp.add("If no movement has been made", 186);
                comp.add("After dealing damage", 187);
                comp.add("Ultimate skill damage", 188);
                comp.add("Damage is increased", 189);
                comp.add("Restores 1 HP immediately", 190);
                comp.add("Increase AoE ", 191);
                comp.add("Increase critical ", 192);
                comp.add("Increase defense ", 193);
                comp.add("Increases attack ", 194);
                comp.add("For each <", 195);
                comp.add("When using ", 196);
                comp.add("After entering battle", 197);
            }


            //complete
            {
                comp.add("Daily ", 208);
                comp.add("Raise any ", 209);
                comp.add("Unlocked after completing", 210);
                comp.add("Unlocked after Overall", 211);
                comp.add("Unlock by completing ", 212);
                comp.add("Unlocked after obtaining", 213);
                comp.add("Unlocked when ", 194);
                comp.add("Unlocked ", 215);
                comp.add("Unlock by ", 216);
                comp.add("Use ", 217);
                comp.add("Clear Story ", 218);
                comp.add("Clear Supply ", 219);
                comp.add("Clear stage ", 220);

                comp.add("Deal ", 290);
                comp.add("Deal a total ", 291);
                comp.add("Accumulate ", 292);
                comp.add("Obtain ", 293);
                comp.add("Win ", 294);
                comp.add("Kill ", 295);
                comp.add("Deployment ", 296);
                comp.add("Click anywhere", 297);
                comp.add("Please click", 298);
                comp.add("Inflict ", 299);
                comp.add("Max-level ", 300);
                comp.add("Combat Exercise", 301);
                comp.add("[Beginner] ", 302);
                comp.add("Reach Commander Level ", 303);
                comp.add("Clear any ", 304);
                comp.add("Clear Simulation ", 305);
                comp.add("A Neural Stock Boost Bar", 306);
                comp.add("Exploration Supplies", 307);
                comp.add("A common weapon blueprint", 308);
                comp.add("Test description", 309);
                comp.add("Military Simulation Season: ", 310);
                comp.add("Perform ", 311);
                comp.add("Neural Survey", 312);
                comp.add("Mysterious Gear", 313);

                comp.add("SMG Advanced", 314);
                comp.add("AR Advanced", 315);
                comp.add("SG Advanced", 316);
                comp.add("RF Advanced", 317);

                comp.add("Blade Advanced", 319);
                comp.add("Total level ", 320);
                comp.add("Season ", 321);
                comp.add("Fixed Keys: ", 322);
                comp.add("Equipment Analysis", 323);
                comp.add("Exploration sup", 324);
                comp.add("Platoon ", 325);

                //in-combat
                comp.add("Capture the ", 474);
                comp.add("Capture all", 475);
                comp.add("Protect the allied ", 476);
                comp.add("[Draft Copy]", 477);
                comp.add("Lose no more than ", 478);
                comp.add("Destroy ", 479);
            }

            //weapon
            comp.add("[MG] ", 481);
            comp.add("[RF] ", 482);
            comp.add("[AR] ", 483);
            comp.add("[SMG] ", 484);
            comp.add("[HG] ", 485);
            comp.add("[SG] ", 486);
            comp.add("[Blade] ", 487);
        }
        //comp.complete();
        return comp;
    }


    public static void main(String[] args) throws IOException {
        GroupComparator comp = create();

        String all = "";
        for (Group gr:comp.groups){
            String line;
            if(gr.getClass()==Group.class){
                line = "s|";
            } else {
                line = "c|";
            }
            line += gr.sortIndex;
            line += "|";
            line += gr.prefix;
            line += "|\n";

            all += line;
        }

        Files.writeString(Path.of("rules.txt"), all);


//        TreeMap<String, String> map = new TreeMap<>(comp);

//        JTranslationPack.Type type = JTranslationPack.readFile("C:/Users/Te4hnoPlus/Desktop/GFLT/tableV2.json", map);

//        int line = 0;
//        TreeMap<String, String> warns = new TreeMap<>();
//
//        for (Map.Entry<String, String> entry : map.entrySet()) {
////            String prevV = entry.getValue();
////            String fixed = fixNames(prevV);
////            if(!fixed.equals(prevV)) entry.setValue(fixed);
//            ++line;
//            if (!entry.getKey().contains(">{0}<"))continue;
//
//            if(!entry.getValue().contains(">{0}<")){
//                String res = tryFix(entry.getKey(), entry.getValue());
//                if(res != null){
//                    map.put(entry.getKey(), res);
//                } else {
//                    warns.put(String.valueOf(line), entry.getKey());
//                }
//            }
//        }

//        Map<String, String> export = new TreeMap<>(comp);
//        Map<String, String> other = new TreeMap<>(comp);
//
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            Group group = comp.findGroup(entry.getKey());
//            if(group.sortIndex <= 8 && group.sortIndex != 0)
//                export.put(entry.getKey(), entry.getKey());
//            else
//                other.put(entry.getKey(), entry.getValue());
//        }

        //JTranslationPack.writeFile("C:/Users/Te4hnoPlus/Desktop/GFLT/export.json", other, type);
//        JTranslationPack.writeFile("C:/Users/Te4hnoPlus/Desktop/GFLT/export_src.json", export, type);

        //JTranslationPack.writeFile("C:/Users/Te4hnoPlus/Desktop/GFLT/tableV2.json", map, type);

        //JTranslationPack.writeFile("C:/Users/Te4hnoPlus/Desktop/GFLT/warns.json", warns, type);
    }


    private static final String[][] nameFixes =
            {
                    {"Makiatto", "Маккиатто"},
                    {"Andoris", "Андорис"},
                    {"Klukai", "Клюкай"},
                    {"Qiongjiu", "Цандзю"},
                    {"Vector", "Вектор"},
                    {"Suomi", "Суоми"},
                    {"Tololo", "Тололо"},
                    {"Ksenia", "Ксения"},
                    {"Daiyan", "Дайян"},
                    {"Lotta", "Лотта"},
                    {"Sharkry", "Шаркри"},
                    {"Ullrid", "Ульрид"},
                    {"Centaureissi", "Центауресси"},
                    {"Cheeta", "Чита"},
                    {"Groza", "Гроза"},
                    {"Sabrina", "Сабрина"},
                    {"Vepley", "Вепли"},
                    {"Mosin-Nagant", "Мосина"},
                    {"Dushevnaya", "Душевная"},
                    {"Krolik", "Кролик"},
                    {"Mechty", "Соня"},
                    {"Colphne", "Колфне"},
                    {"Nemesis", "Немезис"},
                    {"Papasha", "Папаша"},
                    {"Peritya", "Перития"},
                    {"Littara", "Литтара"},
                    {"Nagant", "Наган"},
            };


    public static String fixNames(String name){
        for (String[] fix : nameFixes)
            name = name.replace(fix[0], fix[1]);
        return name;
    }



    static class GroupComparator implements Comparator<String> {
        Group defaultGroup = new Group("", 0);
        Group[] groups = new Group[0];
        Group[][] mappingByFirst;
        Group[] noMapped = groups;

        public void add(String prefix, int sortIndex) {
            groups = add(groups, new Group(prefix, sortIndex));
        }

        public void addB(String prefix, int sortIndex) {
            groups = add(groups, new GroupB(prefix, sortIndex));
        }


        private static Group[] add(Group[] groups, Group group){
            if(groups == null)return new Group[]{group};
            groups = Arrays.copyOf(groups, groups.length + 1);
            groups[groups.length - 1] = group;
            return groups;
        }


        public void complete(){
            Arrays.sort(groups, (o1, o2) -> Integer.compare(o2.prefix.length(), o1.prefix.length()));
            Group[][] mpf = mappingByFirst = new Group[128][];

            for (Group group:groups){
                if(group.getClass() == Group.class){
                    int index = index(group.prefix);
                    mpf[index] = add(mpf[index], group);
                } else {
                    noMapped = add(noMapped, group);
                }
            }
        }


        private static int index(String str){
            if(str.isEmpty())return 0;
            return str.charAt(0) & 127;
        }


        public void setDefaultGroup(String prefix, int sortIndex) {
            defaultGroup = new Group(prefix, sortIndex);
        }


        public Group findGroup(String key) {
            Group[] byIndex = mappingByFirst[index(key)];
            if(byIndex != null){
                for (Group group : byIndex)
                    if (group.isSomeGroup(key)) return group;
            }
            for (Group group : noMapped) {
                if (group.isSomeGroup(key)) return group;
            }
            return defaultGroup;
        }


        @Override
        public int compare(String o1r, String o2r) {
            String o1 = remId(o1r);
            String o2 = remId(o2r);

            Group group1 = findGroup(o1);
            Group group2 = findGroup(o2);
            if(group1 != group2){
                int result = Integer.compare(group1.sortIndex, group2.sortIndex);
                if(result != 0) return result;
            }

            int res = Integer.compare(o1.length(), o2.length());
            if(res != 0) return res;
            if(o1 == o1r && o2 == o2r)
                return o1.compareTo(o2);
            else {
                int res2 = o1.compareTo(o2);
                if(res2 == 0){
                    int id1 = getId(o1r);
                    int id2 = getId(o2r);
                    return Integer.compare(id1, id2);
                }
                return res2;
            }
        }


        private static int getId(String src){
            if(src.startsWith("#ID:")){
                int index = src.indexOf("|");
                if (index == -1) return 0;
                int num = 0;
                for (int i = 4; i < index; i++){
                    num *= 10;
                    num += (src.charAt(i) - '0');
                }
                return num;
            }
            return 0;
        }


        private static String remId(String src){
            if(src.startsWith("#ID:")){
                int index = src.indexOf("|");
                if (index == -1) return src;
                return src.substring(index+1);
            }
            return src;
        }
    }


    static class Group{
        final String prefix;
        final int sortIndex;
        Group(String prefix, int sortIndex) {
            this.prefix = prefix;
            this.sortIndex = sortIndex;
        }
        public boolean isSomeGroup(String key) {
            return key.startsWith(prefix);
        }
    }


    static class GroupB extends Group{
        GroupB(String prefix, int sortIndex) {
            super(prefix, sortIndex);
        }

        @Override
        public boolean isSomeGroup(String key) {
            return key.contains(prefix);
        }
    }


    private static String tryFix(String key, String value) {
        int countK = key.length() - key.replace("{0}", "").length()/3;
        //if(countK != 3) return value;
        int count = value.length() - value.replace("0", "").length();
        if(count == countK){
            return value.replace("0", "{0}");
        }
        return null;
    }
}