package asm;

import net.minecraftforge.fml.common.Mod;
import plus.fserver.api.ElcaServerUtils;
import plus.fserver.api.inject.Field;
import plus.fserver.api.inject.FieldAccessor;
import plus.fserver.api.inject.LoadInjector;
import plus.fserver.server.AsyWorldTickerV2;

//Обьявляем о том, что перед загрузкой класса нужно запустить инжектор
@LoadInjector(fields = {
        //Указываем целевой класс, поле и его тип
        @Field(target = "plus.fserver.server.AsyWorldTickerV2", name = "exampleField", type = String.class)
        //Можно написать несколько полей и классов
})
@Mod(modid = "example")
public class ExampleMod {
    //Компилируем двухсторонний интерфейс доступа к полю
    public static final FieldAccessor<AsyWorldTickerV2,String> access =
            ElcaServerUtils.compileAccess(AsyWorldTickerV2.class, String.class, "exampleField");

    //Тестируем
    public static void test(AsyWorldTickerV2 inst){
        //Получаем значение (по умолчанию null)
        String value = access.get(inst);
        //Записываем значение
        access.set(inst, "new-value");
    }
}
