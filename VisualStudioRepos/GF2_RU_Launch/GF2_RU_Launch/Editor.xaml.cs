
using System.Collections;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using SourceChord.FluentWPF;
using LangEditAPI;
using System.Text.Json;
using System.Text.Encodings.Web;
using Microsoft.VisualBasic;

namespace GF2_RU_Launch
{

    public partial class Editor : AcrylicWindow
    {
        private ArrayList data = new ArrayList();
        private ArrayList viewData = new ArrayList();
        private MainWindow parent;

        private LangEditAPI.Table? table1, table2;
        private ELine? filter;

        public Editor(MainWindow main)
        {
            parent = main;
            InitializeComponent();
            SetExtendViewIntoTitleBar(this, true);

            TTable.ItemsSource = viewData;
        }


        protected override void OnClosed(EventArgs e)
        {
            parent.onEditorClosed();
        }


        private void setMainData(ArrayList newData)
        {
            data = newData;
            updateView();
        }

        private void updateView()
        {
            viewData.Clear();

            var fLtr = filter;

            if (fLtr == null)
            {
                viewData.AddRange(data);
            }
            else
            {
                foreach (var item in data)
                {
                    if (item != null)
                    {
                        ELine cur = item as ELine;

                        if (fLtr.Value != null && cur.Value != null)
                        {
                            if (cur.Value.Contains(fLtr.Value))
                            {
                                viewData.Add(cur);
                            }
                            
                        }
                        else if (fLtr.Original != null && cur.Original != null)
                        {
                            if (cur.Original.Contains(fLtr.Original))
                            {
                                viewData.Add(cur);
                            }
                        }
                    }
                }
            }
            TTable.Items.Refresh();
        }


        private void onClickLoad(object sender, RoutedEventArgs e)
        {
            var dataBytes = open("Выберите шаблон Lang.bytes", ".bytes", "Lang.bytes|*.bytes");
            if (dataBytes == null)
            {
                return;
            }

            if (table1 != null)
            {
                table1.free();
            }

            table1 = new LangEditAPI.Table(1);
            table1.load(dataBytes);

            onCompleteImport();
        }


        private void clickKeyFilter(object sender, RoutedEventArgs e)
        {
            var res = Interaction.InputBox("Подстрока", "Введите фильтр", "");
            if (res != null && res.Length > 0) {
                if (filter == null) {
                    filter = new ELine();
                }
                filter.Original = res;
            }
            updateView();
        }


        private void clickValueFilter(object sender, RoutedEventArgs e)
        {
            var res = Interaction.InputBox("Подстрока", "Введите фильтр", "");
            if (res != null && res.Length > 0)
            {
                if (filter == null)
                {
                    filter = new ELine();
                }
                filter.Value = res;
            }
            updateView();
        }


        private void clickResetFilter(object sender, RoutedEventArgs e)
        {
            if (filter != null)
            {
                filter = null;
                updateView();
            }
        }


        private void wanEmptyTable()
        {
            MessageBox.Show("Главная таблица пуста!");
        }


        private void onCompleteImport()
        {
            if(table1 == null)
            {
                wanEmptyTable();
                return;
            }
            var jData = table1.exportMappings(table2, 0xf);

            if (jData != null)
            {
                try
                {
                    var arr = fromJsonBytes(jData);
                    setMainData(arr);
                }
                catch (Exception err)
                {
                    MessageBox.Show(err.Message + "\n" + err.StackTrace);
                    return;
                }
            }
            if (table2 != null)
            {
                table2.free();
                table2 = null;
            }
        }


        private void onClickLoadSecond(object sender, RoutedEventArgs e)
        {
            var dataBytes = open("Выберите релиз Lang.bytes", ".bytes", "Lang.bytes|*.bytes");
            if (dataBytes == null)
            {
                return;
            }

            table2 = new LangEditAPI.Table(2);
            table2.load(dataBytes);

            onCompleteImport();
        }


        private void onClickClose(object sender, RoutedEventArgs e)
        {
            if(table1 != null)
            {
                table1.free();
                table1 = null;
                setMainData(new ArrayList());
            }
        }


        private void onClickLoadLang(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }
            var dataBytes = open("Выберите карту переводов mappings.json", ".json", "mappings.bytes|*.json");
            if (dataBytes == null)
            {
                return;
            }
            table1.applyMappngs(dataBytes);

            onCompleteImport();
        }


        private void onClickExportLangJson(object sender, RoutedEventArgs e)
        {
            exportLang(false);
        }


        private void onClickExportLangJsonID(object sender, RoutedEventArgs e)
        {
            exportLang(true);
        }


        private void onClickExportLangBytes(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }
            var res = getSaveFile("Выберите релиз Lang.bytes", ".bytes", "Lang.bytes|*.bytes");

            if (res == null)
            {
                return;
            }

            var bData = toJsonBytes(data, true);
            table1.applyMappngs(bData);

            bData = table1.save();

            try
            {
                File.WriteAllBytes(res, bData);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка сохранения!");
            }
        }


        private void exportLang(bool useId)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }
            var res = getSaveFile("Выберите карту переводов mappings.json", ".json", "mappings.bytes|*.json");

            if (res == null) {
                return;
            }

            var data2 = toJsonBytes(data, useId);

            try
            {
                File.WriteAllBytes(res, data2);
            }
            catch (Exception ex) {
                MessageBox.Show("Ошибка сохранения!");
            }
        }


        public static string? getSaveFile(string title, string ext, string filter)
        {
            Microsoft.Win32.SaveFileDialog dlg = new Microsoft.Win32.SaveFileDialog();

            dlg.Title = title;
            dlg.DefaultExt = ext;
            dlg.Filter = filter;
            dlg.CheckPathExists = true;

            Nullable<bool> result = dlg.ShowDialog();

            if (result == true)
            {
                return dlg.FileName;
            }
            return null;
        }


        public static byte[]? open(string title, string ext, string filter)
        {
            Microsoft.Win32.OpenFileDialog dlg = new Microsoft.Win32.OpenFileDialog();

            dlg.Title = title;
            dlg.DefaultExt = ext;
            dlg.Filter = filter;

            Nullable<bool> result = dlg.ShowDialog();

            if (result == true) {
                return File.ReadAllBytes(dlg.FileName);
            }
            return null;
        }


        public static ArrayList fromJsonBytes(byte[] data)
        {
            ArrayList list = new ArrayList();
            var map = JsonSerializer.Deserialize<Dictionary<string, string>>(UTF8Encoding.UTF8.GetString(data));

            foreach (var item in map)
            {
                var id = getId(item.Key);
                var orig = remId(item.Key);
                ELine eLine = new ELine(id, orig);
                eLine.Value = item.Value;
                list.Add(eLine);
            }
            return list;
        }


        public static byte[] toJsonBytes(ArrayList list, bool useId)
        {
            Dictionary<string, string> map = new Dictionary<string, string>();
            foreach (var item in list) { 
                ELine? line = item as ELine;
                if (line != null)
                {
                    map[line.getSaveKey(useId)] = line.actualValue();
                }
            }

            var options = new JsonSerializerOptions
            {
                Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
                WriteIndented = true,
            };

            var result = JsonSerializer.Serialize(map, options);
            return UTF8Encoding.UTF8.GetBytes(result);
        }


        public static int getId(string str)
        {
            if(str == null || str.Length <= 5)return 0;
            if (str.StartsWith("#ID:"))
            {
                int index = str.IndexOf('|', 4);
                if (index == -1)return 0;
                int num = 0;
                for (int i = 4; i < index; i++)
                {
                    num *= 10;
                    num += (str[i] - '0');
                }
                return num;
            }
            return 0;
        }


        public static string remId(string src)
        {
            if (src.StartsWith("#ID:"))
            {
                int index = src.IndexOf("|");
                if (index == -1) return src;
                return src[(index + 1)..];
            }
            return src;
        }


    }


    public class ELine
    {
        public int Id { get; set; }
        public string? Value { get; set; }
        public string Original {  get; set; }

        public ELine() {
            Original = "";
        }
        public ELine(int id, string value)
        {
            Id = id;
            Value = value;
            Original = value;
        }


        public string getSaveKey(bool useId)
        {
            if (!useId) return Value;

            return "#ID:" + Id + "|" + Value;
        }

        public string actualValue()
        {
            if (Value != null) return Value;
            return Original;
        }

        public ELine copy()
        {
            ELine copy = new ELine();
            copy.Id = Id;
            copy.Value = Value;
            copy.Original = Original;
            return copy;
        }
    }
}
