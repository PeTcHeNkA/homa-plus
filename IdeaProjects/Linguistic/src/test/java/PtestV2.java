import plus.lyn.Word;
import plus.lyn.WordPartIndex;
import plus.lyn.unit.Proposal;
import plus.lyn.utl.PropMapExtended;


public class PtestV2 {
    //Ассоциативный-словарь типа "Наибольшая фраза из предложения" -> Object
    private static final PropMapExtended propMap = new PropMapExtended();
    //Синхронный канал чтения/анализа предложений/слов и сопоставления
    //Указание типа позволяет приводить тип значения автоматически
    private static final PropMapExtended.Chanel<String> chan = propMap.makeChan();

    public static void main(String[] args) {
        //сюда можно записывать что угодно в любых количествах
        chan.put("красивая весна", "+"); {
        chan.put("багровая луна", "-");
        chan.put("хочешь печеньки", "да");
        chan.put("физика", "сложка ...");
        chan.put("физика интересная", "не соглашусь");
        chan.put("закон", "??");}
        chan.put("Этот прекрасный мир!", "Давай сожгем его?)");

        //скорость работы этого "Словаря" зависит только от длинны запроса
        //~0.5м слов в запросах в секунду
        //каждый из приведенных ниже запросов будет выполнен успешно
        String[] requests = {
                "Ты серьезно считаешь, что физика интересна??!",
                "Сегодня красивая весна, не так ли?",
                "Хочешь печеньку?",
                "Я хочу печеньки nyaaa",
                "Я хочу жрать печеньки под багровой луной ..."
        };
        //К черту инкапсуляцию и проверки длинны массива (в примере можно)
        System.out.println(chan.read(requests[0]).first());

        //Провести базовый анализ слов в кеше, построить связи
        Proposal proposal = chan.proposal();
        //строим отдельное слово если нужно
        Word word = chan.builder().build("печеньки");

        //если окончание у первого существительного и у нового слова равны ...
        if(proposal.subjects[0].getWord().ending() == word.ending());             //todo
        //есло первое слово в найденном словосочетании - частица ...
        if(proposal.words[chan.getStart()].getPart() == WordPartIndex.PARTICIPLE);//todo

        for (int i=0;i<5;i++) testRequests(requests);
    }


    private static void testRequests(String[] requests){
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20_0000; i++){
            test(requests);
        }
        System.out.println("Spend time: "+(System.currentTimeMillis()-start)+"ms");
    }


    private static Object last;
    public static void test(String[] requests){
        for (String req:requests){
            last = chan.read(req).proposal();
        }
    }
}