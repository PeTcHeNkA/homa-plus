namespace GF2_LaunchHX.lib
{
    public class TryCopyTranslate : ITranslateEngine
    {
        private LangTable table;
        bool ITranslateEngine.Finish()
        {
            if(table != null)
            {
                table = null;
            }
            return true;
        }


        bool ITranslateEngine.Setup(MainWindow window, LangTable tb, TranslationCallback cb)
        {
            table = tb;
            return true;
        }


        bool ITranslateEngine.TranslateLines(List<ELine> lines, TranslationCallback cb)
        {
            if (table == null) return false;
            ELine temp = new ELine();
            foreach (ELine line in lines)
            {
                temp.Original = line.Original;

                var el = table.GetLines(temp);

                if (el != null) 
                {
                    foreach (ELine tline in el)
                    {
                        if (tline.ActualValue() != line.Original)
                        {
                            line.Value = tline.ActualValue();
                            break;
                        }
                    }
                }
                cb.Add(1);
            }
            return true;
        }
    }
}
